package com.example.zenaral.data

import android.content.Context
import android.content.SharedPreferences
import com.example.zenaral.model.JournalEntry
import com.example.zenaral.model.VoucherRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        context.getSharedPreferences("zenaral_ledger_cache", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://shemacc-3ccac-default-rtdb.asia-southeast1.firebasedatabase.app"

    var isLiveConnected: Boolean = false
        private set

    suspend fun pingFirebase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/.json?shallow=true"
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
            val response = client.newCall(request).execute()
            isLiveConnected = response.isSuccessful
            isLiveConnected
        } catch (_: Exception) {
            isLiveConnected = false
            false
        }
    }

    private fun timestampToPushIdPrefix(timestamp: Long): String {
        val pushChars = "-0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz"
        var time = timestamp
        val chars = CharArray(8)
        for (i in 7 downTo 0) {
            chars[i] = pushChars[(time % 64).toInt()]
            time /= 64
        }
        return String(chars)
    }

    private fun toIsoDate(dateStr: String): String {
        val clean = dateStr.trim()
        if (clean.isBlank()) return ""
        val parts = clean.split("-", "/")
        if (parts.size == 3) {
            if (parts[0].length == 4) return "${parts[0]}-${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}"
            if (parts[2].length == 4) return "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
        }
        return clean
    }

    private fun parseRecordsFromJson(bodyString: String?): List<VoucherRecord> {
        if (bodyString.isNullOrBlank() || bodyString == "null") return emptyList()
        val parsedRecords = mutableListOf<VoucherRecord>()
        try {
            val rootElement = json.parseToJsonElement(bodyString)
            if (rootElement is JsonObject) {
                for ((key, value) in rootElement) {
                    if (value is JsonObject) {
                        val txnId = value["txn_id"]?.jsonPrimitive?.contentOrNull ?: ""
                        val voucherNo = value["voucher_no"]?.jsonPrimitive?.contentOrNull ?: ""
                        val voucherType = value["voucher_type"]?.jsonPrimitive?.contentOrNull
                            ?: value["voucherType"]?.jsonPrimitive?.contentOrNull
                            ?: "Journal"
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
                                voucherType = voucherType,
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
        } catch (_: Exception) {}
        return parsedRecords
    }

    suspend fun getTransactions(
        branch: String,
        fromDate: String,
        toDate: String
    ): Result<List<VoucherRecord>> = withContext(Dispatchers.IO) {
        try {
            val recordMap = mutableMapOf<String, VoucherRecord>()
            var anyQuerySucceeded = false
            val hasDateFilter = fromDate.isNotBlank() || toDate.isNotBlank()

            if (hasDateFilter) {
                val fromMillis = if (fromDate.isNotBlank()) normalizeDateForComparison(fromDate) else 0L
                val toMillis = if (toDate.isNotBlank()) {
                    val t = normalizeDateForComparison(toDate)
                    if (t > 0) t + (24 * 60 * 60 * 1000L - 1) else Long.MAX_VALUE
                } else Long.MAX_VALUE

                // Server-Side Range Query using Push ID timestamps ($key is always indexed in Firebase)
                val startKey = if (fromMillis > 0) timestampToPushIdPrefix(fromMillis) else ""
                val endKey = if (toMillis < Long.MAX_VALUE) timestampToPushIdPrefix(toMillis) + "\uf8ff" else ""

                val keyQueryUrl = buildString {
                    append("$baseUrl/transactions/$branch.json?orderBy=\"\$key\"")
                    if (startKey.isNotBlank()) append("&startAt=\"$startKey\"")
                    if (endKey.isNotBlank()) append("&endAt=\"$endKey\"")
                }

                // Server-Side Range Query using indexed "date" property
                val isoFrom = if (fromDate.isNotBlank()) toIsoDate(fromDate) else ""
                val isoTo = if (toDate.isNotBlank()) toIsoDate(toDate) else ""
                val dateQueryUrl = if (isoFrom.isNotBlank() || isoTo.isNotBlank()) {
                    buildString {
                        append("$baseUrl/transactions/$branch.json?orderBy=\"date\"")
                        if (isoFrom.isNotBlank()) append("&startAt=\"$isoFrom\"")
                        if (isoTo.isNotBlank()) append("&endAt=\"$isoTo\"")
                    }
                } else null

                // Run queries concurrently to drastically cut latency
                coroutineScope {
                    val job1 = async {
                        try {
                            val req = Request.Builder().url(keyQueryUrl).get().build()
                            val resp = client.newCall(req).execute()
                            if (resp.isSuccessful) parseRecordsFromJson(resp.body?.string()) else emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                    }
                    val job2 = async {
                        if (dateQueryUrl != null) {
                            try {
                                val req = Request.Builder().url(dateQueryUrl).get().build()
                                val resp = client.newCall(req).execute()
                                if (resp.isSuccessful) parseRecordsFromJson(resp.body?.string()) else emptyList()
                            } catch (_: Exception) {
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    }
                    val res1 = job1.await()
                    val res2 = job2.await()
                    if (res1.isNotEmpty() || res2.isNotEmpty()) {
                        anyQuerySucceeded = true
                        res1.forEach { recordMap[it.key] = it }
                        res2.forEach { recordMap[it.key] = it }
                    }
                }
            } else {
                // If All Time is selected, limit server response to the latest 300 records to prevent network slowdown
                val limitUrl = "$baseUrl/transactions/$branch.json?orderBy=\"\$key\"&limitToLast=300"
                try {
                    val req = Request.Builder().url(limitUrl).get().build()
                    val resp = client.newCall(req).execute()
                    if (resp.isSuccessful) {
                        anyQuerySucceeded = true
                        val records = parseRecordsFromJson(resp.body?.string())
                        records.forEach { recordMap[it.key] = it }
                    }
                } catch (_: Exception) {}
            }

            if (!anyQuerySucceeded && recordMap.isEmpty()) {
                isLiveConnected = false
                val cached = getCachedTransactions(branch)
                val filtered = filterByDate(cached, fromDate, toDate)
                return@withContext Result.success(filtered)
            }

            isLiveConnected = true
            val fetchedList = recordMap.values.toList()

            // Merge server-filtered results into local persistent cache for offline resilience
            val currentCache = getCachedTransactions(branch).toMutableList()
            val cacheMap = currentCache.associateBy { it.key }.toMutableMap()
            fetchedList.forEach { cacheMap[it.key] = it }
            val mergedCache = cacheMap.values.sortedByDescending {
                val d = normalizeDateForComparison(it.date)
                if (d > 0) d else it.timestamp
            }
            saveCache(branch, mergedCache)

            val filtered = filterByDate(fetchedList, fromDate, toDate).sortedByDescending {
                val dMillis = normalizeDateForComparison(it.date)
                if (dMillis > 0) dMillis else it.timestamp
            }
            Result.success(filtered)
        } catch (e: Exception) {
            isLiveConnected = false
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

    private fun normalizeDateForComparison(dateStr: String): Long {
        val clean = dateStr.trim()
        if (clean.isBlank()) return 0L
        val formats = listOf("dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "yyyy/MM/dd", "ddMMyyyy")
        for (fmt in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(fmt, java.util.Locale.getDefault())
                sdf.isLenient = false
                val parsed = sdf.parse(clean)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    fun filterByDate(records: List<VoucherRecord>, fromDate: String, toDate: String): List<VoucherRecord> {
        if (fromDate.isBlank() && toDate.isBlank()) return records
        val fromMillis = if (fromDate.isNotBlank()) normalizeDateForComparison(fromDate) else 0L
        val toMillis = if (toDate.isNotBlank()) {
            val t = normalizeDateForComparison(toDate)
            if (t > 0) t + (24 * 60 * 60 * 1000L - 1) else Long.MAX_VALUE
        } else Long.MAX_VALUE

        return records.filter { record ->
            val recordMillis = normalizeDateForComparison(record.date)
            if (recordMillis == 0L) return@filter true
            val afterFrom = if (fromMillis > 0) recordMillis >= fromMillis else true
            val beforeTo = if (toMillis < Long.MAX_VALUE) recordMillis <= toMillis else true
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
                "voucher_type": "${escape(v.voucherType)}",
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
