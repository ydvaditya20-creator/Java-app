package com.example.zenaral.ui.dialogs

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.zenaral.model.DateFilterMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DateFilterModalDialog(
    initialMode: DateFilterMode,
    initialCustomFrom: String,
    initialCustomTo: String,
    onDismiss: () -> Unit,
    onApply: (mode: DateFilterMode, customFrom: String, customTo: String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }

    var selectedMode by remember { mutableStateOf(initialMode) }
    var customFrom by remember {
        mutableStateOf(
            if (initialCustomFrom.isNotBlank()) initialCustomFrom else {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -10)
                dateFormat.format(c.time)
            }
        )
    }
    var customTo by remember {
        mutableStateOf(
            if (initialCustomTo.isNotBlank()) initialCustomTo else {
                dateFormat.format(Calendar.getInstance().time)
            }
        )
    }

    fun showDatePicker(initialDateStr: String, onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        try {
            val d = dateFormat.parse(initialDateStr.trim())
            if (d != null) cal.time = d
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onSelected(dateFormat.format(chosen.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Helper to calculate preview range string
    fun getPreviewRangeText(mode: DateFilterMode): String {
        return when (mode) {
            DateFilterMode.LAST_10_DAYS -> {
                val c = Calendar.getInstance()
                val to = dateFormat.format(c.time)
                c.add(Calendar.DAY_OF_YEAR, -10)
                val from = dateFormat.format(c.time)
                "$from  से  $to"
            }
            DateFilterMode.LAST_20_DAYS -> {
                val c = Calendar.getInstance()
                val to = dateFormat.format(c.time)
                c.add(Calendar.DAY_OF_YEAR, -20)
                val from = dateFormat.format(c.time)
                "$from  से  $to"
            }
            DateFilterMode.LAST_30_DAYS -> {
                val c = Calendar.getInstance()
                val to = dateFormat.format(c.time)
                c.add(Calendar.DAY_OF_YEAR, -30)
                val from = dateFormat.format(c.time)
                "$from  से  $to"
            }
            DateFilterMode.CUSTOM -> {
                if (customFrom.isNotBlank() && customTo.isNotBlank()) {
                    "$customFrom  से  $customTo"
                } else "कस्टम तारीख़ चुनें"
            }
            DateFilterMode.ALL_TIME -> "सभी तारीख़ों के पूरे रिकॉर्ड"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 440.dp)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Filter Ledger Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "डेटा फ़िल्टर (Auto-Preserved)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_date_filter_modal")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "चुनें कि कितने दिनों का डेटा देखना और डाउनलोड करना है। यह फ़िल्टर सुरक्षित रहेगा:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // Preset options list
                DateFilterMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = mode }
                            .testTag("filter_option_${mode.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ),
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMode = mode }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = mode.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${mode.hindiTitle})",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = getPreviewRangeText(mode),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // If CUSTOM is selected, show From and To date pickers inline
                            if (mode == DateFilterMode.CUSTOM && isSelected) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = customFrom,
                                        onValueChange = { customFrom = it },
                                        label = { Text("From Date") },
                                        placeholder = { Text("DD-MM-YYYY") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_modal_from_date"),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                showDatePicker(customFrom) { customFrom = it }
                                            }) {
                                                Icon(
                                                    Icons.Default.CalendarToday,
                                                    contentDescription = "Pick From Date",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )

                                    OutlinedTextField(
                                        value = customTo,
                                        onValueChange = { customTo = it },
                                        label = { Text("To Date") },
                                        placeholder = { Text("DD-MM-YYYY") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("input_modal_to_date"),
                                        trailingIcon = {
                                            IconButton(onClick = {
                                                showDatePicker(customTo) { customTo = it }
                                            }) {
                                                Icon(
                                                    Icons.Default.CalendarToday,
                                                    contentDescription = "Pick To Date",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("btn_cancel_date_filter")
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onApply(selectedMode, customFrom, customTo)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("btn_apply_date_filter"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Apply & Save ✓",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
