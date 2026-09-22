package com.example.tallyledger.model

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
    val date: String = "", // YYYY-MM-DD
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
            if (date.contains("-")) {
                val parts = date.split("-")
                if (parts.size == 3) {
                    return "${parts[2]}-${parts[1]}-${parts[0]}"
                }
            }
            return date
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
