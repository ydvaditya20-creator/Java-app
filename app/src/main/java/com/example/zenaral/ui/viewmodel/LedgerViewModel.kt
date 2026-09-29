package com.example.zenaral.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenaral.data.FirebaseLedgerRepository
import com.example.zenaral.model.DateFilterMode
import com.example.zenaral.model.JournalEntry
import com.example.zenaral.model.LedgerBranch
import com.example.zenaral.model.VoucherRecord
import com.example.zenaral.ui.theme.AppTheme
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
    private val prefs = application.getSharedPreferences("zenaral_ledger_prefs", Context.MODE_PRIVATE)

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HHmmss-SSS", Locale.getDefault())
    private val yearShortFormat = SimpleDateFormat("yy", Locale.getDefault())

    // Firebase live secure connection indicator
    private val _isFirebaseConnected = MutableStateFlow(false)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

    // Theme state with persistent SharedPreferences
    private val _currentTheme = MutableStateFlow(
        AppTheme.fromId(prefs.getString("selected_app_theme", AppTheme.LIGHT.id))
    )
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        prefs.edit().putString("selected_app_theme", theme.id).apply()
    }

    // Branch selection
    private val _selectedBranch = MutableStateFlow(LedgerBranch.NATWA)
    val selectedBranch: StateFlow<LedgerBranch> = _selectedBranch.asStateFlow()

    // Date filters & persistent filter mode
    private val _currentFilterMode = MutableStateFlow(
        DateFilterMode.fromId(prefs.getString("selected_date_filter_mode", DateFilterMode.LAST_10_DAYS.id))
    )
    val currentFilterMode: StateFlow<DateFilterMode> = _currentFilterMode.asStateFlow()

    private val _filterFromDate = MutableStateFlow("")
    val filterFromDate: StateFlow<String> = _filterFromDate.asStateFlow()

    private val _filterToDate = MutableStateFlow("")
    val filterToDate: StateFlow<String> = _filterToDate.asStateFlow()

    // Records
    private val _transactions = MutableStateFlow<List<VoucherRecord>>(emptyList())
    val transactions: StateFlow<List<VoucherRecord>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("🔒 Secure Zenaral Multi-Line Engine Active")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Autocomplete sets
    private val _uniqueAccounts = MutableStateFlow<Set<String>>(emptySet())
    val uniqueAccounts: StateFlow<Set<String>> = _uniqueAccounts.asStateFlow()

    private val _uniqueNarrations = MutableStateFlow<Set<String>>(emptySet())
    val uniqueNarrations: StateFlow<Set<String>> = _uniqueNarrations.asStateFlow()

    // Form fields
    private val _formVoucherType = MutableStateFlow("Journal")
    val formVoucherType: StateFlow<String> = _formVoucherType.asStateFlow()

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
        // Load persistent date filter preference
        val savedModeId = prefs.getString("selected_date_filter_mode", DateFilterMode.LAST_10_DAYS.id)
        val savedMode = DateFilterMode.fromId(savedModeId)
        val savedCustomFrom = prefs.getString("saved_custom_from_date", "") ?: ""
        val savedCustomTo = prefs.getString("saved_custom_to_date", "") ?: ""
        _currentFilterMode.value = savedMode

        val (initialFrom, initialTo) = calculateDateRange(savedMode, savedCustomFrom, savedCustomTo)
        _filterFromDate.value = initialFrom
        _filterToDate.value = initialTo

        refreshVoucherNumbers()

        // Instant local cache hydration: show records on frame 1 without network waiting!
        val cached = repository.getCachedTransactions(_selectedBranch.value.id)
        if (cached.isNotEmpty()) {
            val filtered = repository.filterByDate(cached, initialFrom, initialTo)
            _transactions.value = filtered
            updateAutocompleteSets(cached)
        }

        loadTransactions()
    }

    fun checkFirebaseConnection() {
        viewModelScope.launch {
            val isConnected = repository.pingFirebase()
            _isFirebaseConnected.value = isConnected
        }
    }

    fun calculateDateRange(mode: DateFilterMode, customFrom: String = "", customTo: String = ""): Pair<String, String> {
        return when (mode) {
            DateFilterMode.LAST_10_DAYS -> {
                val cal = Calendar.getInstance()
                val to = dateFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -10)
                val from = dateFormat.format(cal.time)
                Pair(from, to)
            }
            DateFilterMode.LAST_20_DAYS -> {
                val cal = Calendar.getInstance()
                val to = dateFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -20)
                val from = dateFormat.format(cal.time)
                Pair(from, to)
            }
            DateFilterMode.LAST_30_DAYS -> {
                val cal = Calendar.getInstance()
                val to = dateFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -30)
                val from = dateFormat.format(cal.time)
                Pair(from, to)
            }
            DateFilterMode.CUSTOM -> {
                val cal = Calendar.getInstance()
                val defaultTo = dateFormat.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val defaultFrom = dateFormat.format(cal.time)
                val from = customFrom.ifBlank { defaultFrom }
                val to = customTo.ifBlank { defaultTo }
                Pair(from, to)
            }
            DateFilterMode.ALL_TIME -> {
                Pair("", "")
            }
        }
    }

    fun applyDateFilterMode(mode: DateFilterMode, customFrom: String = "", customTo: String = "") {
        _currentFilterMode.value = mode
        val (computedFrom, computedTo) = calculateDateRange(mode, customFrom, customTo)
        _filterFromDate.value = computedFrom
        _filterToDate.value = computedTo

        prefs.edit()
            .putString("selected_date_filter_mode", mode.id)
            .putString("saved_custom_from_date", customFrom)
            .putString("saved_custom_to_date", customTo)
            .apply()

        loadTransactions()
    }

    fun getSavedCustomDates(): Pair<String, String> {
        val from = prefs.getString("saved_custom_from_date", "") ?: ""
        val to = prefs.getString("saved_custom_to_date", "") ?: ""
        return Pair(from, to)
    }

    fun selectBranch(branch: LedgerBranch) {
        if (_selectedBranch.value != branch) {
            _selectedBranch.value = branch
            refreshVoucherNumbers()
            loadTransactions()
        }
    }

    fun setFilterDates(from: String, to: String) {
        applyDateFilterMode(DateFilterMode.CUSTOM, from, to)
    }

    fun loadTransactions() {
        viewModelScope.launch {
            if (_transactions.value.isEmpty()) {
                _isLoading.value = true
            }
            val branch = _selectedBranch.value.id
            val result = repository.getTransactions(
                branch = branch,
                fromDate = _filterFromDate.value,
                toDate = _filterToDate.value
            )

            result.onSuccess { list ->
                _isFirebaseConnected.value = repository.isLiveConnected
                _transactions.value = list
                updateAutocompleteSets(list)
                refreshVoucherNumbers()
                _isLoading.value = false
            }.onFailure { err ->
                _isFirebaseConnected.value = repository.isLiveConnected
                _isLoading.value = false
                if (_transactions.value.isEmpty()) {
                    _toastMessage.value = "Notice: ${err.localizedMessage ?: "Using cached ledger"}"
                }
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
    fun setFormVoucherType(type: String) {
        _formVoucherType.value = type
    }

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
                voucherType = _formVoucherType.value.ifBlank { "Journal" },
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
                _formVoucherType.value = "Journal"
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
