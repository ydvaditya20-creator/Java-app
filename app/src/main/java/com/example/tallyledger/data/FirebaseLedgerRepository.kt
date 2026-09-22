package com.example.tallyledger.data

import android.content.Context
import android.content.SharedPreferences
import com.example.tallyledger.model.JournalEntry
import com.example.tallyledger.model.VoucherRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class FirebaseLedgerRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tally_ledger_cache", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://shemacc-3ccac-default-rtdb.asia-southeast1.firebasedatabase.app"

    suspend fun getTransactions(
        branch: String,
        fromDate: String,
        toDate: String
    ): Result<List<VoucherRecord>> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/transactions/$branch.json"
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                // Fall back to cached data
                val cached = getCachedTransactions(branch)
                val filtered = filterByDate(cached, fromDate, toDate)
                return@withContext Result.success(filtered)
            }

            val bodyString = response.body?.string()
            if (bodyString.isNullOrBlank() || bodyString == "null") {
                saveCache(branch, emptyList())
                return@withContext Result.success(emptyList())
            }

            val parsedRecords = mutableListOf<VoucherRecord>()
            val rootElement = json.parseToJsonElement(bodyString)

            if (rootElement is JsonObject) {
                for ((key, value) in rootElement) {
                    if (value is JsonObject) {
                        val txnId = value["txn_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        val voucherNo = value["voucher_no"]?.jsonPrimitive?.contentOrNull ?: ""
                        val date = value["date"]?.jsonPrimitive?.contentOrNull ?: ""
                        val narration = value["narration"]?.jsonPrimitive?.contentOrNull ?: ""
                        val timestamp = value["timestamp"]?.jsonPrimitive?.longOrNull ?: 0L

                        val entriesList = mutableListOf<JournalEntry>()
                        val entriesArray = value["entries"]?.run {
                            if (this is kotlinx.serialization.json.JsonArray) this else null
                        }
                        entriesArray?.forEach { entryElement ->
                            if (entryElement is JsonObject) {
                                val type = entryElement["type"]?.jsonPrimitive?.contentOrNull ?: "Dr"
                                val account = entryElement["account"]?.jsonPrimitive?.contentOrNull ?: ""
                                val amount = entryElement["amount"]?.jsonPrimitive?.contentOrNull ?: "0.00"
                                entriesList.add(JournalEntry(type = type, account = account, amount = amount))
                            }
                        }

                        val billLinksList = mutableListOf<String>()
                        val billArray = value["billLinks"]?.run {
                            if (this is kotlinx.serialization.json.JsonArray) this else null
                        }
                        billArray?.forEach { linkElem ->
                            linkElem.jsonPrimitive.contentOrNull?.let { billLinksList.add(it) }
                        }

                        parsedRecords.add(
                            VoucherRecord(
                                key = key,
                                txnId = txnId,
                                voucherNo = voucherNo,
                                date = date,
                                entries = entriesList,
                                narration = narration,
                                billLinks = billLinksList,
                                timestamp = timestamp
                            )
                        )
                    }
                }
            }

            // Sort descending by date and voucher number
            parsedRecords.sortByDescending { it.date + "_" + it.voucherNo }
            saveCache(branch, parsedRecords)

            val filtered = filterByDate(parsedRecords, fromDate, toDate)
            Result.success(filtered)
        } catch (e: Exception) {
            val cached = getCachedTransactions(branch)
            val filtered = filterByDate(cached, fromDate, toDate)
            if (filtered.isNotEmpty()) {
                Result.success(filtered)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun saveVoucher(
        branch: String,
        voucher: VoucherRecord
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/transactions/$branch.json"
            val payload = buildPayload(voucher)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to save: HTTP ${response.code}"))
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = json.parseToJsonElement(responseBody)
            val generatedKey = (jsonResponse as? JsonObject)?.get("name")?.jsonPrimitive?.contentOrNull ?: ""

            // Update local cache
            val newRecord = voucher.copy(key = generatedKey)
            val cached = getCachedTransactions(branch).toMutableList()
            cached.add(0, newRecord)
            saveCache(branch, cached)

            Result.success(generatedKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVoucher(
        branch: String,
        key: String,
        voucher: VoucherRecord
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/transactions/$branch/$key.json"
            val payload = buildPayload(voucher)
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to update: HTTP ${response.code}"))
            }

            // Update local cache
            val cached = getCachedTransactions(branch).toMutableList()
            val index = cached.indexOfFirst { it.key == key }
            if (index >= 0) {
                cached[index] = voucher.copy(key = key)
                saveCache(branch, cached)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVoucher(
        branch: String,
        key: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/transactions/$branch/$key.json"
            val request = Request.Builder()
                .url(url)
                .delete()
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to delete: HTTP ${response.code}"))
            }

            // Update local cache
            val cached = getCachedTransactions(branch).toMutableList()
            cached.removeAll { it.key == key }
            saveCache(branch, cached)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun filterByDate(records: List<VoucherRecord>, fromDate: String, toDate: String): List<VoucherRecord> {
        if (fromDate.isBlank() && toDate.isBlank()) return records
        return records.filter { record ->
            val d = record.date
            if (d.isBlank()) return@filter true
            val afterFrom = if (fromDate.isNotBlank()) d >= fromDate else true
            val beforeTo = if (toDate.isNotBlank()) d <= toDate else true
            afterFrom && beforeTo
        }
    }

    private fun buildPayload(v: VoucherRecord): String {
        val entriesJson = v.entries.joinToString(prefix = "[", postfix = "]") { e ->
            """{"type":"${e.type}","account":"${escape(e.account)}","amount":"${escape(e.amount)}"}"""
        }
        val billLinksJson = v.billLinks.joinToString(prefix = "[", postfix = "]") { link ->
            "\"${escape(link)}\""
        }
        return """
            {
                "txn_id": "${escape(v.txnId)}",
                "voucher_no": "${escape(v.voucherNo)}",
                "date": "${escape(v.date)}",
                "entries": $entriesJson,
                "narration": "${escape(v.narration)}",
                "billLinks": $billLinksJson,
                "timestamp": ${if (v.timestamp > 0) v.timestamp else System.currentTimeMillis()}
            }
        """.trimIndent()
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun getCachedTransactions(branch: String): List<VoucherRecord> {
        val raw = prefs.getString("cache_$branch", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<VoucherRecord>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveCache(branch: String, records: List<VoucherRecord>) {
        try {
            val raw = json.encodeToString(records)
            prefs.edit().putString("cache_$branch", raw).apply()
        } catch (e: Exception) {
            // ignore cache write failures
        }
    }
}
