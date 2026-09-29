package com.example.zenaral.ui.dialogs

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.zenaral.model.JournalEntry
import com.example.zenaral.model.VoucherRecord
import com.example.zenaral.ui.theme.CreditRedBg
import com.example.zenaral.ui.theme.CreditRedText
import com.example.zenaral.ui.theme.DebitBlueBg
import com.example.zenaral.ui.theme.DebitBlueText
import com.example.zenaral.util.ImageAttachmentHelper
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
    var voucherType by remember { mutableStateOf(voucher.voucherType.ifBlank { "Journal" }) }
    var date by remember { mutableStateOf(voucher.displayDate.ifBlank { voucher.date }) }
    var narration by remember { mutableStateOf(voucher.narration) }
    var entries by remember { mutableStateOf(voucher.entries.toMutableList()) }
    var billLinks by remember { mutableStateOf(voucher.billLinks.toMutableList()) }
    var previewAttachmentUrl by remember { mutableStateOf<String?>(null) }
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
                    previewAttachmentUrl = base64Data
                    Toast.makeText(context, "📸 Image uploaded - Preview opened", Toast.LENGTH_SHORT).show()
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
                    previewAttachmentUrl = base64Data
                    Toast.makeText(context, "📸 Photo captured - Preview opened", Toast.LENGTH_SHORT).show()
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
                    // Voucher Type Selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Voucher Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        val types = listOf("Journal", "Payment", "Receipt", "Contra", "Sales", "Purchase")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            types.forEach { t ->
                                val isSelected = voucherType.equals(t, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { voucherType = t },
                                    label = { Text(t, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

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
                            label = { Text("Date (DD-MM-YYYY)") },
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
                            // Compact Dr/Cr Toggle Box (Only 48dp wide, 1-tap quick toggle)
                            val isDr = entry.type.equals("Dr", ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDr) DebitBlueBg else CreditRedBg)
                                    .border(1.5.dp, if (isDr) DebitBlueText else CreditRedText, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextType = if (isDr) "Cr" else "Dr"
                                        entries = entries.toMutableList().also {
                                            it[index] = entry.copy(type = nextType)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = entry.type,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = if (isDr) DebitBlueText else CreditRedText
                                )
                            }

                            // Account (wraps multi-line)
                            OutlinedTextField(
                                value = entry.account,
                                onValueChange = { newAcc ->
                                    entries = entries.toMutableList().also {
                                        it[index] = entry.copy(account = newAcc)
                                    }
                                },
                                placeholder = { Text("Account Name...", fontSize = 13.sp) },
                                singleLine = false,
                                maxLines = 4,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                modifier = Modifier.weight(1.2f)
                            )

                            // Amount (expanded width with ₹ prefix so full amount is clearly visible)
                            OutlinedTextField(
                                value = entry.amount,
                                onValueChange = { newAmt ->
                                    entries = entries.toMutableList().also {
                                        it[index] = entry.copy(amount = newAmt)
                                    }
                                },
                                placeholder = { Text("Amount", fontSize = 13.sp) },
                                prefix = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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
                        val isDrive = ImageAttachmentHelper.isGoogleDriveLink(link)
                        val displayUrl = ImageAttachmentHelper.getDisplayableImageUrl(link)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { previewAttachmentUrl = link }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (ImageAttachmentHelper.isBase64Image(link)) {
                                val bm = remember(link) { ImageAttachmentHelper.base64ToBitmap(link) }
                                if (bm != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bm.asImageBitmap(),
                                        contentDescription = "Attachment #$idx",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    AsyncImage(
                                        model = displayUrl,
                                        contentDescription = "Attachment #$idx",
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
                                    contentDescription = "Attachment #$idx",
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
                                    text = if (isDrive) "☁️ Google Drive Document" else if (link.startsWith("data:image")) "📷 Captured / Uploaded Photo #${idx + 1}" else "🔗 Link #${idx + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap to preview 🔍",
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
                        Spacer(modifier = Modifier.height(4.dp))
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
                                voucherType = voucherType,
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

    // Attachment Full Preview Dialog
    previewAttachmentUrl?.let { url ->
        AttachmentPreviewDialog(
            attachmentUrl = url,
            title = "Attachment Preview",
            onDismiss = { previewAttachmentUrl = null }
        )
    }
}
