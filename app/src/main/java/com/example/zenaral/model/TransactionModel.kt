package com.example.zenaral.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalEntry(
    val type: String = "Dr", // "Dr" or "Cr"
    val account: String = "",
    val amount: String = "0.00"
) {
    val amountDouble: Double
        get() = amount.toDoubleOrNull() ?: 0.0
}

@Serializable
data class VoucherRecord(
    val key: String = "",
    @SerialName("txn_id") val txnId: String = "",
    @SerialName("voucher_no") val voucherNo: String = "",
    @SerialName("voucher_type") val voucherType: String = "Journal",
    val date: String = "", // dd-MM-yyyy or yyyy-MM-dd
    val entries: List<JournalEntry> = emptyList(),
    val narration: String = "",
    @SerialName("billLinks") val billLinks: List<String> = emptyList(),
    val timestamp: Long = 0L
) {
    val totalDebit: Double
        get() = entries.filter { it.type.equals("Dr", ignoreCase = true) }
            .sumOf { it.amountDouble }

    val totalCredit: Double
        get() = entries.filter { it.type.equals("Cr", ignoreCase = true) }
            .sumOf { it.amountDouble }

    val isBalanced: Boolean
        get() = Math.abs(totalDebit - totalCredit) < 0.001 && totalDebit > 0

    val displayDate: String
        get() {
            val d = date.trim()
            if (d.isBlank()) return ""
            if (d.contains("-")) {
                val parts = d.split("-")
                if (parts.size == 3) {
                    if (parts[0].length == 4) {
                        // yyyy-MM-dd -> dd-MM-yyyy
                        return "${parts[2]}-${parts[1]}-${parts[0]}"
                    } else if (parts[2].length == 4) {
                        // already dd-MM-yyyy
                        return d
                    }
                }
            } else if (d.contains("/")) {
                val parts = d.split("/")
                if (parts.size == 3) {
                    if (parts[0].length == 4) {
                        return "${parts[2]}-${parts[1]}-${parts[0]}"
                    } else {
                        return "${parts[0]}-${parts[1]}-${parts[2]}"
                    }
                }
            }
            return d
        }
}

enum class LedgerBranch(
    val id: String,
    val displayName: String,
    val symbol: String,
    val prefix: String
) {
    NATWA("natwa", "Natwa", "🏠", "N"),
    BASAHI("basahi", "Basahi", "🏢", "B")
}

enum class DateFilterMode(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val days: Int?
) {
    LAST_10_DAYS("LAST_10_DAYS", "Last 10 Days", "पिछले 10 दिन", 10),
    LAST_20_DAYS("LAST_20_DAYS", "Last 20 Days", "पिछले 20 दिन", 20),
    LAST_30_DAYS("LAST_30_DAYS", "Last 30 Days", "पिछले 30 दिन", 30),
    CUSTOM("CUSTOM", "Custom Date Range", "कस्टम तारीख़", null),
    ALL_TIME("ALL_TIME", "All Time", "सभी रिकॉर्ड", null);

    companion object {
        fun fromId(id: String?): DateFilterMode {
            return entries.firstOrNull { it.id == id } ?: LAST_10_DAYS
        }
    }
}
