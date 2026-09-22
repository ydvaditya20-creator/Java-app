package com.example.tallyledger.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.tallyledger.model.JournalEntry
import com.example.tallyledger.model.VoucherRecord
import com.example.tallyledger.ui.theme.CreditRedBg
import com.example.tallyledger.ui.theme.CreditRedText
import com.example.tallyledger.ui.theme.DebitBlueBg
import com.example.tallyledger.ui.theme.DebitBlueText
import com.example.tallyledger.util.ImageAttachmentHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVoucherDialog(
    voucher: VoucherRecord,
    onDismiss: () -> Unit,
    onSave: (VoucherRecord) -> Unit
) {
    var voucherNo by remember { mutableStateOf(voucher.voucherNo) }
    var date by remember { mutableStateOf(voucher.date) }
    var narration by remember { mutableStateOf(voucher.narration) }
    var entries by remember { mutableStateOf(voucher.entries.toMutableList()) }
    var billLinks by remember { mutableStateOf(voucher.billLinks.toMutableList()) }
    var newAttachmentUrl by remember { mutableStateOf("") }
    var isProcessingImage by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Gallery Photo Picker (Modern zero-permission picker)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isProcessingImage = true
            coroutineScope.launch {
                val base64Data = withContext(Dispatchers.IO) {
                    ImageAttachmentHelper.uriToBase64DataUrl(context, uri)
                }
                if (base64Data != null) {
                    billLinks = billLinks.toMutableList().also { it.add(base64Data) }
                }
                isProcessingImage = false
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = tempCameraUri
        if (success && uri != null) {
            isProcessingImage = true
            coroutineScope.launch {
                val base64Data = withContext(Dispatchers.IO) {
                    ImageAttachmentHelper.uriToBase64DataUrl(context, uri)
                }
                if (base64Data != null) {
                    billLinks = billLinks.toMutableList().also { it.add(base64Data) }
                }
                isProcessingImage = false
            }
        }
    }

    val totalDr = entries.filter { it.type.equals("Dr", true) }.sumOf { it.amountDouble }
    val totalCr = entries.filter { it.type.equals("Cr", true) }.sumOf { it.amountDouble }
    val isBalanced = Math.abs(totalDr - totalCr) < 0.001 && totalDr > 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .testTag("edit_voucher_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Journal Voucher",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Voucher No & Date Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = voucherNo,
                            onValueChange = { voucherNo = it },
                            label = { Text("Voucher No") },
                            modifier = Modifier.weight(1f).testTag("edit_voucher_no")
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f).testTag("edit_date")
                        )
                    }

                    Text(
                        text = "Ledger Entries (Double Entry)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Entries
                    entries.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dr/Cr Toggle Dropdown
                            var typeExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = typeExpanded,
                                onExpandedChange = { typeExpanded = !typeExpanded },
                                modifier = Modifier.width(80.dp)
                            ) {
                                OutlinedTextField(
                                    value = entry.type,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable),
                                    textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                ExposedDropdownMenu(
                                    expanded = typeExpanded,
                                    onDismissRequest = { typeExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Dr", fontWeight = FontWeight.Bold, color = DebitBlueText) },
                                        onClick = {
                                            entries = entries.toMutableList().also {
                                                it[index] = entry.copy(type = "Dr")
                                            }
                                            typeExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cr", fontWeight = FontWeight.Bold, color = CreditRedText) },
                                        onClick = {
                                            entries = entries.toMutableList().also {
                                                it[index] = entry.copy(type = "Cr")
                                            }
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }

                            // Account
                            OutlinedTextField(
                                value = entry.account,
                                onValueChange = { newAcc ->
                                    entries = entries.toMutableList().also {
                                        it[index] = entry.copy(account = newAcc)
                                    }
                                },
                                placeholder = { Text("Account...") },
                                modifier = Modifier.weight(1.4f)
                            )

                            // Amount
                            OutlinedTextField(
                                value = entry.amount,
                                onValueChange = { newAmt ->
                                    entries = entries.toMutableList().also {
                                        it[index] = entry.copy(amount = newAmt)
                                    }
                                },
                                placeholder = { Text("₹") },
                                modifier = Modifier.weight(1f)
                            )

                            // Remove
                            IconButton(
                                onClick = {
                                    if (entries.size > 2) {
                                        entries = entries.toMutableList().also { it.removeAt(index) }
                                    }
                                },
                                enabled = entries.size > 2,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Row",
                                    tint = if (entries.size > 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    // Add Row button
                    OutlinedButton(
                        onClick = {
                            entries = entries.toMutableList().also {
                                it.add(JournalEntry("Dr", "", ""))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Row")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account Row")
                    }

                    // Narration
                    OutlinedTextField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = { Text("Narration / Remarks") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("edit_narration")
                    )

                    // Attachments management
                    Text(
                        text = "Receipts & Bill Attachments",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (billLinks.isEmpty()) {
                        Text(
                            text = "No receipts attached yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    billLinks.forEachIndexed { idx, link ->
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
                                    contentDescription = "Attachment #$idx",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (link.startsWith("data:image")) "📷 Captured / Uploaded Photo #${idx + 1}" else "🔗 Link #${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (link.startsWith("data:image")) "Embedded Image (${link.length / 1024} KB)" else link.take(35) + "...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    billLinks = billLinks.toMutableList().also { it.removeAt(idx) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Attachment", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    if (isProcessingImage) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing photo...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Click Photo & Upload from Gallery Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val uri = ImageAttachmentHelper.createTempPictureUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_click_photo"),
                            enabled = !isProcessingImage,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Click Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                galleryPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_upload_photo"),
                            enabled = !isProcessingImage
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Optional URL fallback
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newAttachmentUrl,
                            onValueChange = { newAttachmentUrl = it },
                            placeholder = { Text("Or paste cloud image URL", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedButton(
                            onClick = {
                                if (newAttachmentUrl.isNotBlank()) {
                                    billLinks = billLinks.toMutableList().also { it.add(newAttachmentUrl.trim()) }
                                    newAttachmentUrl = ""
                                }
                            },
                            enabled = newAttachmentUrl.isNotBlank()
                        ) {
                            Text("Add", fontSize = 12.sp)
                        }
                    }

                    // Balance status summary
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBalanced) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Total Dr: ₹${String.format("%.2f", totalDr)} | Total Cr: ₹${String.format("%.2f", totalCr)}",
                                fontWeight = FontWeight.Bold,
                                color = if (isBalanced) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                            if (!isBalanced) {
                                Text(
                                    text = if (totalDr <= 0) "Total amount cannot be zero" else "⚠️ Debit and Credit must match exactly!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val updated = voucher.copy(
                                voucherNo = voucherNo,
                                date = date,
                                entries = entries,
                                narration = narration,
                                billLinks = billLinks
                            )
                            onSave(updated)
                        },
                        enabled = isBalanced,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}
