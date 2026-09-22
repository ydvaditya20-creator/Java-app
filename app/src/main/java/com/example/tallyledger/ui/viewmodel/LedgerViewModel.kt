package com.example.tallyledger.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tallyledger.data.FirebaseLedgerRepository
import com.example.tallyledger.model.JournalEntry
import com.example.tallyledger.model.LedgerBranch
import com.example.tallyledger.model.VoucherRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FirebaseLedgerRepository(application.applicationContext)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HHmmss-SSS", Locale.getDefault())
    private val yearShortFormat = SimpleDateFormat("yy", Locale.getDefault())

    // Branch selection
    private val _selectedBranch = MutableStateFlow(LedgerBranch.NATWA)
    val selectedBranch: StateFlow<LedgerBranch> = _selectedBranch.asStateFlow()

    // Date filters
    private val _filterFromDate = MutableStateFlow("")
    val filterFromDate: StateFlow<String> = _filterFromDate.asStateFlow()

    private val _filterToDate = MutableStateFlow("")
    val filterToDate: StateFlow<String> = _filterToDate.asStateFlow()

    // Records
    private val _transactions = MutableStateFlow<List<VoucherRecord>>(emptyList())
    val transactions: StateFlow<List<VoucherRecord>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("🔒 Secure Tally Multi-Line Engine Active")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Autocomplete sets
    private val _uniqueAccounts = MutableStateFlow<Set<String>>(emptySet())
    val uniqueAccounts: StateFlow<Set<String>> = _uniqueAccounts.asStateFlow()

    private val _uniqueNarrations = MutableStateFlow<Set<String>>(emptySet())
    val uniqueNarrations: StateFlow<Set<String>> = _uniqueNarrations.asStateFlow()

    // Form fields
    private val _formDate = MutableStateFlow(dateFormat.format(Date()))
    val formDate: StateFlow<String> = _formDate.asStateFlow()

    private val _formVoucherNo = MutableStateFlow("")
    val formVoucherNo: StateFlow<String> = _formVoucherNo.asStateFlow()

    private val _formTxnId = MutableStateFlow("")
    val formTxnId: StateFlow<String> = _formTxnId.asStateFlow()

    private val _formEntries = MutableStateFlow<List<JournalEntry>>(
        listOf(
            JournalEntry("Dr", "", ""),
            JournalEntry("Cr", "", "")
        )
    )
    val formEntries: StateFlow<List<JournalEntry>> = _formEntries.asStateFlow()

    private val _formNarration = MutableStateFlow("")
    val formNarration: StateFlow<String> = _formNarration.asStateFlow()

    private val _formBillLinks = MutableStateFlow<List<String>>(emptyList())
    val formBillLinks: StateFlow<List<String>> = _formBillLinks.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Dialog states
    private val _editingVoucher = MutableStateFlow<VoucherRecord?>(null)
    val editingVoucher: StateFlow<VoucherRecord?> = _editingVoucher.asStateFlow()

    private val _receiptVoucher = MutableStateFlow<VoucherRecord?>(null)
    val receiptVoucher: StateFlow<VoucherRecord?> = _receiptVoucher.asStateFlow()

    init {
        // Default date range: yesterday to today
        val cal = Calendar.getInstance()
        val todayStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        _filterFromDate.value = yesterdayStr
        _filterToDate.value = todayStr

        refreshVoucherNumbers()
        loadTransactions()
    }

    fun selectBranch(branch: LedgerBranch) {
        if (_selectedBranch.value != branch) {
            _selectedBranch.value = branch
            refreshVoucherNumbers()
            loadTransactions()
        }
    }

    fun setFilterDates(from: String, to: String) {
        _filterFromDate.value = from
        _filterToDate.value = to
        loadTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            val branch = _selectedBranch.value.id
            val result = repository.getTransactions(
                branch = branch,
                fromDate = _filterFromDate.value,
                toDate = _filterToDate.value
            )

            result.onSuccess { list ->
                _transactions.value = list
                updateAutocompleteSets(list)
                refreshVoucherNumbers()
                _isLoading.value = false
            }.onFailure { err ->
                _isLoading.value = false
                _toastMessage.value = "Notice: ${err.localizedMessage ?: "Using cached ledger"}"
            }
        }
    }

    private fun updateAutocompleteSets(list: List<VoucherRecord>) {
        val accounts = mutableSetOf<String>()
        val narrations = mutableSetOf<String>()

        list.forEach { r ->
            r.entries.forEach { e ->
                if (e.account.isNotBlank()) accounts.add(e.account.trim())
            }
            if (r.narration.isNotBlank()) narrations.add(r.narration.trim())
        }

        _uniqueAccounts.value = accounts
        _uniqueNarrations.value = narrations
    }

    fun refreshVoucherNumbers() {
        val now = Date()
        val yy = yearShortFormat.format(now)
        val prefix = _selectedBranch.value.prefix
        val timePart = timeFormat.format(now)

        // Find highest existing voucher number for this branch
        val currentList = repository.getCachedTransactions(_selectedBranch.value.id)
            .ifEmpty { _transactions.value }

        var maxNumber = 0
        for (item in currentList) {
            val vNo = item.voucherNo
            // e.g. N-26-0001
            val parts = vNo.split("-")
            if (parts.size >= 3) {
                val num = parts[2].toIntOrNull() ?: 0
                if (num > maxNumber) {
                    maxNumber = num
                }
            }
        }

        val nextNum = maxNumber + 1
        val formattedNum = String.format(Locale.getDefault(), "%04d", nextNum)

        _formVoucherNo.value = "$prefix-$yy-$formattedNum"
        _formTxnId.value = "TXN-$timePart"
    }

    // Form modifications
    fun setFormDate(date: String) {
        _formDate.value = date
    }

    fun addEntryRow(type: String = "Dr") {
        val current = _formEntries.value.toMutableList()
        current.add(JournalEntry(type = type, account = "", amount = ""))
        _formEntries.value = current
    }

    fun updateEntryRow(index: Int, type: String, account: String, amount: String) {
        val current = _formEntries.value.toMutableList()
        if (index in current.indices) {
            current[index] = JournalEntry(type = type, account = account, amount = amount)
            _formEntries.value = current
        }
    }

    fun removeEntryRow(index: Int) {
        val current = _formEntries.value.toMutableList()
        if (current.size > 2 && index in current.indices) {
            current.removeAt(index)
            _formEntries.value = current
        } else if (index in current.indices) {
            // Keep at least 2 rows
            _toastMessage.value = "Voucher must contain at least 2 accounts"
        }
    }

    fun setFormNarration(narration: String) {
        _formNarration.value = narration
    }

    fun addFormBillLink(link: String) {
        if (link.isNotBlank()) {
            val current = _formBillLinks.value.toMutableList()
            current.add(link)
            _formBillLinks.value = current
        }
    }

    fun removeFormBillLink(index: Int) {
        val current = _formBillLinks.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _formBillLinks.value = current
        }
    }

    fun submitFormVoucher() {
        val entries = _formEntries.value
        val totalDr = entries.filter { it.type.equals("Dr", true) }.sumOf { it.amountDouble }
        val totalCr = entries.filter { it.type.equals("Cr", true) }.sumOf { it.amountDouble }

        if (Math.abs(totalDr - totalCr) > 0.001) {
            _toastMessage.value = "Total Debits (₹$totalDr) must exactly equal Total Credits (₹$totalCr)!"
            return
        }
        if (totalDr <= 0) {
            _toastMessage.value = "Total Amount cannot be zero."
            return
        }

        // Validate account names
        for ((idx, entry) in entries.withIndex()) {
            if (entry.account.isBlank()) {
                _toastMessage.value = "Please specify account name for row #${idx + 1}"
                return
            }
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            refreshVoucherNumbers() // Ensure fresh time-based TXN

            val record = VoucherRecord(
                txnId = _formTxnId.value,
                voucherNo = _formVoucherNo.value,
                date = _formDate.value,
                entries = entries,
                narration = _formNarration.value.trim(),
                billLinks = _formBillLinks.value,
                timestamp = System.currentTimeMillis()
            )

            val branch = _selectedBranch.value.id
            val result = repository.saveVoucher(branch, record)

            result.onSuccess {
                _toastMessage.value = "⚡ Voucher ${record.voucherNo} Synced Successfully!"
                // Reset form
                _formEntries.value = listOf(
                    JournalEntry("Dr", "", ""),
                    JournalEntry("Cr", "", "")
                )
                _formNarration.value = ""
                _formBillLinks.value = emptyList()
                refreshVoucherNumbers()
                loadTransactions()
            }.onFailure { err ->
                _toastMessage.value = "Save Failed: ${err.message}"
            }
            _isSubmitting.value = false
        }
    }

    // Edit operations
    fun startEditing(record: VoucherRecord) {
        _editingVoucher.value = record
    }

    fun cancelEditing() {
        _editingVoucher.value = null
    }

    fun saveEditing(updatedRecord: VoucherRecord) {
        val totalDr = updatedRecord.totalDebit
        val totalCr = updatedRecord.totalCredit
        if (Math.abs(totalDr - totalCr) > 0.001) {
            _toastMessage.value = "Total Debits (₹$totalDr) must equal Total Credits (₹$totalCr)!"
            return
        }

        viewModelScope.launch {
            val branch = _selectedBranch.value.id
            val result = repository.updateVoucher(branch, updatedRecord.key, updatedRecord)
            result.onSuccess {
                _toastMessage.value = "✓ Voucher ${updatedRecord.voucherNo} Updated!"
                _editingVoucher.value = null
                loadTransactions()
            }.onFailure { err ->
                _toastMessage.value = "Update failed: ${err.message}"
            }
        }
    }

    // Delete operations
    fun deleteVoucher(record: VoucherRecord) {
        viewModelScope.launch {
            val branch = _selectedBranch.value.id
            val result = repository.deleteVoucher(branch, record.key)
            result.onSuccess {
                _toastMessage.value = "Voucher ${record.voucherNo} deleted"
                loadTransactions()
            }.onFailure { err ->
                _toastMessage.value = "Delete failed: ${err.message}"
            }
        }
    }

    // Receipt operations
    fun viewReceipt(record: VoucherRecord) {
        _receiptVoucher.value = record
    }

    fun dismissReceipt() {
        _receiptVoucher.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
