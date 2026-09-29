package com.example.zenaral.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.example.zenaral.model.DateFilterMode
import com.example.zenaral.model.LedgerBranch
import com.example.zenaral.ui.components.AutocompleteTextField
import com.example.zenaral.ui.components.VoucherCard
import com.example.zenaral.ui.dialogs.AttachmentPreviewDialog
import com.example.zenaral.ui.dialogs.DateFilterModalDialog
import com.example.zenaral.ui.dialogs.EditVoucherDialog
import com.example.zenaral.ui.dialogs.PrintReceiptDialog
import com.example.zenaral.ui.theme.AppTheme
import com.example.zenaral.ui.theme.CreditRedBg
import com.example.zenaral.ui.theme.CreditRedText
import com.example.zenaral.ui.theme.DebitBlueBg
import com.example.zenaral.ui.theme.DebitBlueText
import com.example.zenaral.ui.viewmodel.LedgerViewModel
import com.example.zenaral.util.HtmlExportHelper
import com.example.zenaral.util.ImageAttachmentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

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
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsStateWithLifecycle()
    val currentFilterMode by viewModel.currentFilterMode.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showDateFilterModal by remember { mutableStateOf(false) }
    var activeFormPreviewUrl by remember { mutableStateOf<String?>(null) }

    val formVoucherType by viewModel.formVoucherType.collectAsStateWithLifecycle()
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
                    activeFormPreviewUrl = base64Data
                    Toast.makeText(context, "📸 Image uploaded - Preview opened", Toast.LENGTH_SHORT).show()
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
                    activeFormPreviewUrl = base64Data
                    Toast.makeText(context, "📸 Photo captured - Preview opened", Toast.LENGTH_SHORT).show()
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.widthIn(max = 330.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download & Export",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.close() } },
                            modifier = Modifier.testTag("btn_close_drawer")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Drawer")
                        }
                    }

                    HorizontalDivider()

                    // Date Filter Card in Download & Export section
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch { drawerState.close() }
                                showDateFilterModal = true
                            }
                            .testTag("drawer_date_filter_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Data Date Filter / फ़िल्टर",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Preserved ✓",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${currentFilterMode.title} (${currentFilterMode.hindiTitle})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = if (filterFromDate.isNotBlank() && filterToDate.isNotBlank()) {
                                    "📅 $filterFromDate  से  $filterToDate"
                                } else "📅 सभी रिकॉर्ड (All Historical Records)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch { drawerState.close() }
                                    showDateFilterModal = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                                    .testTag("drawer_btn_change_date_filter"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Filter Data (10/20/30/Custom) ⚙️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            val (_, msg) = HtmlExportHelper.downloadToDownloadsFolder(context)
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("drawer_btn_download_standalone_html"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download index.html", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val msg = HtmlExportHelper.shareIndexHtml(context)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("drawer_btn_share_standalone_html")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share index.html", fontSize = 14.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // App Theme Picker Section ("jaha per download wala hai option hai waha")
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "App Theme / थीम",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Preserved ✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AppTheme.entries.forEach { theme ->
                            val isSelected = currentTheme == theme
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        viewModel.setTheme(theme)
                                        Toast.makeText(context, "${theme.title} applied & preserved", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("theme_option_${theme.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(theme.primaryHex))
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = theme.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = theme.hindiTitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Zenaral",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_menu_drawer")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer Menu", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showDateFilterModal = true },
                            modifier = Modifier.testTag("topbar_btn_filter")
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = "Filter Dates", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_download_index_html")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Downloads & Offline Export", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(
                            onClick = { viewModel.loadTransactions() },
                            modifier = Modifier.testTag("btn_refresh")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onPrimary)
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
                    Spacer(modifier = Modifier.height(2.dp))
                }

            item {
                // Branch Selector Toggle (Natwa 🏠 vs Basahi 🏢 with Blue Verified Check on Firebase Connect)
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "🏠 Natwa",
                                        fontWeight = if (selectedBranch == LedgerBranch.NATWA) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                    if (isFirebaseConnected) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Firebase Connected",
                                            tint = Color(0xFF1E88E5),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.testTag("tab_natwa")
                        )
                        Tab(
                            selected = selectedBranch == LedgerBranch.BASAHI,
                            onClick = { viewModel.selectBranch(LedgerBranch.BASAHI) },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "🏢 Basahi",
                                        fontWeight = if (selectedBranch == LedgerBranch.BASAHI) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                    if (isFirebaseConnected) {
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Firebase Connected",
                                            tint = Color(0xFF1E88E5),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
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
                        // Voucher Type Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Voucher Type",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick select chips
                            val voucherTypes = listOf("Journal", "Payment", "Receipt", "Contra", "Sales", "Purchase")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                voucherTypes.forEach { type ->
                                    val isSelected = formVoucherType.equals(type, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setFormVoucherType(type) },
                                        label = {
                                            Text(
                                                type,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.testTag("chip_voucher_type_$type")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Voucher Header: Auto Voucher No & Date (Clean borderless mobile input)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Voucher Auto No", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formVoucherNo.ifBlank { "${selectedBranch.prefix}-26-0001" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Date (DD-MM-YYYY)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                    IconButton(
                                        onClick = {
                                            showDatePickerDialog(context, formDate) { picked ->
                                                viewModel.setFormDate(picked)
                                            }
                                        },
                                        modifier = Modifier.size(24.dp).testTag("btn_pick_form_date")
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday,
                                            contentDescription = "Pick Date",
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                TextField(
                                    value = formDate,
                                    onValueChange = { viewModel.setFormDate(it) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("form_date_input"),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
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
                                // Compact Dr / Cr Toggle Box (Only 48dp wide, 1-tap quick toggle)
                                val isDr = entry.type.equals("Dr", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .height(52.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDr) DebitBlueBg else CreditRedBg)
                                        .border(1.5.dp, if (isDr) DebitBlueText else CreditRedText, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val next = if (isDr) "Cr" else "Dr"
                                            viewModel.updateEntryRow(index, next, entry.account, entry.amount)
                                        }
                                        .testTag("toggle_dr_cr_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = entry.type,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = if (isDr) DebitBlueText else CreditRedText
                                    )
                                }

                                // Account name with autocomplete (wraps multi-line)
                                AutocompleteTextField(
                                    value = entry.account,
                                    onValueChange = { newAcc ->
                                        viewModel.updateEntryRow(index, entry.type, newAcc, entry.amount)
                                    },
                                    suggestions = uniqueAccounts,
                                    placeholder = "Account Name...",
                                    modifier = Modifier.weight(1.2f),
                                    testTag = "input_account_$index"
                                )

                                // Amount (expanded width with ₹ prefix so full amount is clearly visible)
                                OutlinedTextField(
                                    value = entry.amount,
                                    onValueChange = { newAmt ->
                                        viewModel.updateEntryRow(index, entry.type, entry.account, newAmt)
                                    },
                                    placeholder = { Text("Amount", fontSize = 13.sp) },
                                    prefix = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f).testTag("input_amount_$index"),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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
                            val isDrive = ImageAttachmentHelper.isGoogleDriveLink(link)
                            val displayUrl = ImageAttachmentHelper.getDisplayableImageUrl(link)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { activeFormPreviewUrl = link }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (ImageAttachmentHelper.isBase64Image(link)) {
                                    val bm = remember(link) { ImageAttachmentHelper.base64ToBitmap(link) }
                                    if (bm != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = bm.asImageBitmap(),
                                            contentDescription = "Form Attachment #$idx",
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = displayUrl,
                                            contentDescription = "Form Attachment #$idx",
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.LightGray),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = displayUrl,
                                        contentDescription = "Form Attachment #$idx",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.LightGray),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isDrive) "☁️ Google Drive Document" else if (link.startsWith("data:image")) "📷 Captured Photo #${idx + 1}" else "🔗 Link #${idx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Tap to preview 🔍",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeFormBillLink(idx) },
                                    modifier = Modifier.size(32.dp)
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

            // SECTION HEADER: LEDGER RECORDS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedBranch.symbol} ${selectedBranch.displayName} Ledger Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isFirebaseConnected) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Firebase Connected",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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

    // Date Filter Modal Dialog (Pop Modal)
    if (showDateFilterModal) {
        val (savedFrom, savedTo) = viewModel.getSavedCustomDates()
        DateFilterModalDialog(
            initialMode = currentFilterMode,
            initialCustomFrom = if (filterFromDate.isNotBlank()) filterFromDate else savedFrom,
            initialCustomTo = if (filterToDate.isNotBlank()) filterToDate else savedTo,
            onDismiss = { showDateFilterModal = false },
            onApply = { mode, from, to ->
                viewModel.applyDateFilterMode(mode, from, to)
                showDateFilterModal = false
                Toast.makeText(context, "${mode.title} filter applied & preserved ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Attachment Full Preview Dialog for Form attachments
    activeFormPreviewUrl?.let { url ->
        AttachmentPreviewDialog(
            attachmentUrl = url,
            title = "Attachment Preview",
            onDismiss = { activeFormPreviewUrl = null }
        )
    }
}

private fun showDatePickerDialog(context: Context, currentDateStr: String, onDateSelected: (String) -> Unit) {
    val cal = Calendar.getInstance()
    try {
        val parts = currentDateStr.split("-", "/")
        if (parts.size == 3) {
            if (parts[0].length == 4) {
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            } else {
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            }
        }
    } catch (_: Exception) {}

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val dd = String.format(Locale.getDefault(), "%02d", dayOfMonth)
            val mm = String.format(Locale.getDefault(), "%02d", month + 1)
            onDateSelected("$dd-$mm-$year")
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
