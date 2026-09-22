package com.example.tallyledger.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.tallyledger.model.LedgerBranch
import com.example.tallyledger.ui.components.AutocompleteTextField
import com.example.tallyledger.ui.components.VoucherCard
import com.example.tallyledger.ui.dialogs.EditVoucherDialog
import com.example.tallyledger.ui.dialogs.PrintReceiptDialog
import com.example.tallyledger.ui.theme.CreditRedBg
import com.example.tallyledger.ui.theme.CreditRedText
import com.example.tallyledger.ui.theme.DebitBlueBg
import com.example.tallyledger.ui.theme.DebitBlueText
import com.example.tallyledger.ui.viewmodel.LedgerViewModel
import com.example.tallyledger.util.HtmlExportHelper
import com.example.tallyledger.util.ImageAttachmentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDashboardScreen(viewModel: LedgerViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedBranch by viewModel.selectedBranch.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val formVoucherNo by viewModel.formVoucherNo.collectAsStateWithLifecycle()
    val formTxnId by viewModel.formTxnId.collectAsStateWithLifecycle()
    val formDate by viewModel.formDate.collectAsStateWithLifecycle()
    val formEntries by viewModel.formEntries.collectAsStateWithLifecycle()
    val formNarration by viewModel.formNarration.collectAsStateWithLifecycle()
    val formBillLinks by viewModel.formBillLinks.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val filterFromDate by viewModel.filterFromDate.collectAsStateWithLifecycle()
    val filterToDate by viewModel.filterToDate.collectAsStateWithLifecycle()

    val uniqueAccounts by viewModel.uniqueAccounts.collectAsStateWithLifecycle()
    val uniqueNarrations by viewModel.uniqueNarrations.collectAsStateWithLifecycle()

    val editingVoucher by viewModel.editingVoucher.collectAsStateWithLifecycle()
    val receiptVoucher by viewModel.receiptVoucher.collectAsStateWithLifecycle()

    var customFromDate by remember(filterFromDate) { mutableStateOf(filterFromDate) }
    var customToDate by remember(filterToDate) { mutableStateOf(filterToDate) }

    val totalDr = remember(formEntries) {
        formEntries.filter { it.type.equals("Dr", true) }.sumOf { it.amountDouble }
    }
    val totalCr = remember(formEntries) {
        formEntries.filter { it.type.equals("Cr", true) }.sumOf { it.amountDouble }
    }
    val isBalanced = remember(totalDr, totalCr) {
        Math.abs(totalDr - totalCr) < 0.001 && totalDr > 0
    }

    val coroutineScope = rememberCoroutineScope()
    var isProcessingFormPhoto by remember { mutableStateOf(false) }
    var tempDashboardCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val dashboardGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isProcessingFormPhoto = true
            coroutineScope.launch {
                val base64Data = withContext(Dispatchers.IO) {
                    ImageAttachmentHelper.uriToBase64DataUrl(context, uri)
                }
                if (base64Data != null) {
                    viewModel.addFormBillLink(base64Data)
                }
                isProcessingFormPhoto = false
            }
        }
    }

    val dashboardCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempDashboardCameraUri
        if (success && uri != null) {
            isProcessingFormPhoto = true
            coroutineScope.launch {
                val base64Data = withContext(Dispatchers.IO) {
                    ImageAttachmentHelper.uriToBase64DataUrl(context, uri)
                }
                if (base64Data != null) {
                    viewModel.addFormBillLink(base64Data)
                }
                isProcessingFormPhoto = false
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "⚡ Tally Multi-Line Ledger",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Branch: ${selectedBranch.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val (_, msg) = HtmlExportHelper.downloadToDownloadsFolder(context)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.testTag("btn_download_index_html")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download index.html to Downloads", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.loadTransactions() },
                        modifier = Modifier.testTag("btn_refresh")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Secure Status Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Standalone index.html Download Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🌐 Download Complete index.html",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All-in-one standalone file: HTML + CSS + JS, Multi-line Ledger, Camera/Gallery photo attachments, Natwa/Basahi branches & Thermal print slips.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Button(
                                onClick = {
                                    val (_, msg) = HtmlExportHelper.downloadToDownloadsFolder(context)
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.testTag("btn_download_standalone_html"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val msg = HtmlExportHelper.shareIndexHtml(context)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.testTag("btn_share_standalone_html")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item {
                // Branch Selector Toggle (Natwa 🏠 vs Basahi 🏢)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    TabRow(
                        selectedTabIndex = if (selectedBranch == LedgerBranch.NATWA) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (selectedBranch == LedgerBranch.NATWA) 0 else 1]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedBranch == LedgerBranch.NATWA,
                            onClick = { viewModel.selectBranch(LedgerBranch.NATWA) },
                            text = {
                                Text(
                                    "🏠 Natwa",
                                    fontWeight = if (selectedBranch == LedgerBranch.NATWA) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_natwa")
                        )
                        Tab(
                            selected = selectedBranch == LedgerBranch.BASAHI,
                            onClick = { viewModel.selectBranch(LedgerBranch.BASAHI) },
                            text = {
                                Text(
                                    "🏢 Basahi",
                                    fontWeight = if (selectedBranch == LedgerBranch.BASAHI) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier.testTag("tab_basahi")
                        )
                    }
                }
            }

            // NEW MULTI-LINE JOURNAL VOUCHER CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("new_voucher_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📝 New Multi-Line Journal Voucher",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedBranch.symbol + " " + selectedBranch.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voucher Header: Auto Voucher No & Date
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Voucher Auto No", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = formVoucherNo.ifBlank { "${selectedBranch.prefix}-26-0001" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                OutlinedTextField(
                                    value = formDate,
                                    onValueChange = { viewModel.setFormDate(it) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("form_date_input"),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Double Entry Splits",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Rows of Dr/Cr Entries
                        formEntries.forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dr / Cr dropdown
                                var expandedType by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expandedType,
                                    onExpandedChange = { expandedType = !expandedType },
                                    modifier = Modifier.width(78.dp)
                                ) {
                                    OutlinedTextField(
                                        value = entry.type,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                                        textStyle = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.type == "Dr") DebitBlueText else CreditRedText
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expandedType,
                                        onDismissRequest = { expandedType = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Dr", fontWeight = FontWeight.Bold, color = DebitBlueText) },
                                            onClick = {
                                                viewModel.updateEntryRow(index, "Dr", entry.account, entry.amount)
                                                expandedType = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Cr", fontWeight = FontWeight.Bold, color = CreditRedText) },
                                            onClick = {
                                                viewModel.updateEntryRow(index, "Cr", entry.account, entry.amount)
                                                expandedType = false
                                            }
                                        )
                                    }
                                }

                                // Account name with autocomplete
                                AutocompleteTextField(
                                    value = entry.account,
                                    onValueChange = { newAcc ->
                                        viewModel.updateEntryRow(index, entry.type, newAcc, entry.amount)
                                    },
                                    suggestions = uniqueAccounts,
                                    placeholder = "Account...",
                                    modifier = Modifier.weight(1.3f),
                                    testTag = "input_account_$index"
                                )

                                // Amount
                                OutlinedTextField(
                                    value = entry.amount,
                                    onValueChange = { newAmt ->
                                        viewModel.updateEntryRow(index, entry.type, entry.account, newAmt)
                                    },
                                    placeholder = { Text("₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("input_amount_$index"),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                // Remove button
                                IconButton(
                                    onClick = { viewModel.removeEntryRow(index) },
                                    modifier = Modifier.size(32.dp).testTag("btn_remove_row_$index"),
                                    enabled = formEntries.size > 2
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = if (formEntries.size > 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Row Button
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.addEntryRow("Dr") },
                                modifier = Modifier.testTag("btn_add_dr_row")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Dr", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Dr Row", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.addEntryRow("Cr") },
                                modifier = Modifier.testTag("btn_add_cr_row")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Cr", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Cr Row", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Multi-line Narration with autocomplete
                        Text(
                            text = "Multi-line Narration / Remarks",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AutocompleteTextField(
                            value = formNarration,
                            onValueChange = { viewModel.setFormNarration(it) },
                            suggestions = uniqueNarrations,
                            placeholder = "Write comprehensive multiline details...",
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "input_narration",
                            singleLine = false,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Receipts & Bill Attachments
                        Text(
                            text = "Receipts & Bill Attachments (${formBillLinks.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        formBillLinks.forEachIndexed { idx, link ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (link.startsWith("data:image") || link.startsWith("http")) {
                                    AsyncImage(
                                        model = link,
                                        contentDescription = "Form Attachment #$idx",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (link.startsWith("data:image")) "📷 Captured / Uploaded Photo #${idx + 1}" else "🔗 Link #${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (link.startsWith("data:image")) "Embedded Image (${link.length / 1024} KB)" else link.take(30) + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeFormBillLink(idx) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Attachment", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (isProcessingFormPhoto) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing image...", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        val uri = ImageAttachmentHelper.createTempPictureUri(context)
                                        tempDashboardCameraUri = uri
                                        dashboardCameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                enabled = !isProcessingFormPhoto,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Click Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = {
                                    dashboardGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                enabled = !isProcessingFormPhoto
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Totals verification box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isBalanced) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total Dr: ₹${String.format("%.2f", totalDr)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBalanced) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Total Cr: ₹${String.format("%.2f", totalCr)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBalanced) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                if (!isBalanced) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (totalDr <= 0) "⚠️ Total Amount cannot be zero." else "⚠️ Total Debits must exactly equal Total Credits!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "✓ Debits and Credits match perfectly!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Submit Button
                        Button(
                            onClick = { viewModel.submitFormVoucher() },
                            enabled = isBalanced && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_submit_voucher"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving Voucher Entry...")
                            } else {
                                Text("Save Voucher Entry", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // DATE FILTER BAR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customFromDate,
                                onValueChange = { customFromDate = it },
                                label = { Text("From Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("filter_from_date")
                            )

                            OutlinedTextField(
                                value = customToDate,
                                onValueChange = { customToDate = it },
                                label = { Text("To Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("filter_to_date")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.setFilterDates(customFromDate, customToDate) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_filter_data"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = "Filter", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔍 Filter Ledger Records", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SECTION HEADER: LEDGER RECORDS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedBranch.symbol} ${selectedBranch.displayName} Ledger Records",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${transactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // RECORDS LIST
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No records found for ${selectedBranch.displayName}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create your first voucher above or adjust the date filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(transactions, key = { it.key.ifBlank { it.txnId + it.voucherNo } }) { voucher ->
                    VoucherCard(
                        voucher = voucher,
                        onEdit = { viewModel.startEditing(it) },
                        onDelete = { viewModel.deleteVoucher(it) },
                        onPrint = { viewModel.viewReceipt(it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Edit Voucher Dialog
    editingVoucher?.let { voucherToEdit ->
        EditVoucherDialog(
            voucher = voucherToEdit,
            onDismiss = { viewModel.cancelEditing() },
            onSave = { updated -> viewModel.saveEditing(updated) }
        )
    }

    // Print Receipt Dialog
    receiptVoucher?.let { voucherToPrint ->
        PrintReceiptDialog(
            voucher = voucherToPrint,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}
