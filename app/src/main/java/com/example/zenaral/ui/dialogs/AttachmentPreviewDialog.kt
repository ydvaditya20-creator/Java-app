package com.example.zenaral.ui.dialogs

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.zenaral.util.ImageAttachmentHelper

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AttachmentPreviewDialog(
    attachmentUrl: String,
    title: String = "Attachment Preview",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isDriveLink = remember(attachmentUrl) { ImageAttachmentHelper.isGoogleDriveLink(attachmentUrl) }
    val isBase64 = remember(attachmentUrl) { ImageAttachmentHelper.isBase64Image(attachmentUrl) }

    val decodedBitmap: Bitmap? = remember(attachmentUrl, isBase64) {
        if (isBase64) {
            ImageAttachmentHelper.base64ToBitmap(attachmentUrl)
        } else {
            null
        }
    }

    val drivePreviewUrl = remember(attachmentUrl) { ImageAttachmentHelper.getDrivePreviewUrl(attachmentUrl) }
    val driveViewUrl = remember(attachmentUrl) {
        ImageAttachmentHelper.getDriveViewUrl(attachmentUrl) ?: drivePreviewUrl ?: attachmentUrl
    }
    val displayableUrl = remember(attachmentUrl) { ImageAttachmentHelper.getDisplayableImageUrl(attachmentUrl) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 520.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isDriveLink) "☁️ Google Drive Document / Bill" else "📷 Receipt / Voucher Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isDriveLink) {
                                "Google Drive Preview (iframe)"
                            } else if (isBase64) {
                                "Base64 Image Preview (Firebase / Local Photo)"
                            } else {
                                "Online Image Preview"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_attachment_preview")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Preview")
                    }
                }

                HorizontalDivider()

                // Content Preview Box
                if (isBase64 && decodedBitmap != null) {
                    // 1. BASE64 IMAGE PREVIEW (Rendered directly as Image from Bitmap)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = decodedBitmap.asImageBitmap(),
                            contentDescription = "Base64 Receipt Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 220.dp, max = 460.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else if (isDriveLink && drivePreviewUrl != null) {
                    // 2. GOOGLE DRIVE IFRAME / WEBVIEW PREVIEW (Exactly like JavaScript iframe embed)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        var isIframeLoading by remember { mutableStateOf(true) }

                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.allowContentAccess = true
                                    settings.allowFileAccess = true
                                    settings.userAgentString =
                                        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Mobile Safari/537.36"
                                    webChromeClient = WebChromeClient()
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            isIframeLoading = false
                                        }
                                    }
                                    loadUrl(drivePreviewUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isIframeLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A).copy(alpha = 0.8f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Loading Google Drive iframe...",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 3. STANDARD WEB IMAGE FALLBACK (Coil SubcomposeAsyncImage)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1E1E)),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = displayableUrl,
                            contentDescription = "Attachment preview",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                                }
                            },
                            error = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "⚠️ Preview not available directly",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Use button below to view in Browser / Drive",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isDriveLink || attachmentUrl.startsWith("http://") || attachmentUrl.startsWith("https://")) {
                        Button(
                            onClick = {
                                try {
                                    val targetUri = Uri.parse(driveViewUrl)
                                    val intent = Intent(Intent.ACTION_VIEW, targetUri).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_open_in_drive"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Google Drive / Browser 🌐", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_dismiss_attachment_dialog")
                    ) {
                        Text("Close / बंद करें", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
