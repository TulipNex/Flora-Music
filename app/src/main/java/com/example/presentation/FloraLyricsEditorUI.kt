package com.example.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class EditableLyricLine(
    val id: Long = System.nanoTime(),
    var timestampMs: Long? = null,
    var text: String = ""
)

fun formatLyricTimestamp(ms: Long?): String {
    if (ms == null || ms < 0) return "--:--.--"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hundredths = (ms % 1000) / 10
    return String.format("%02d:%02d.%02d", minutes, seconds, hundredths)
}

fun parseLyricTimestamp(str: String): Long? {
    val clean = str.trim().removeSurrounding("[", "]")
    val parts = clean.split(":")
    if (parts.size == 2) {
        val min = parts[0].toLongOrNull() ?: return null
        val secParts = parts[1].split(".", ":")
        val sec = secParts[0].toLongOrNull() ?: return null
        val ms = if (secParts.size > 1) {
            val decStr = secParts[1]
            when (decStr.length) {
                1 -> (decStr.toLongOrNull() ?: 0L) * 100
                2 -> (decStr.toLongOrNull() ?: 0L) * 10
                else -> decStr.take(3).toLongOrNull() ?: 0L
            }
        } else 0L
        return (min * 60 + sec) * 1000 + ms
    }
    return null
}

fun parseRawLyricsToLines(rawText: String): List<EditableLyricLine> {
    val lines = rawText.lines()
    val result = mutableListOf<EditableLyricLine>()
    val regex = Regex("""^\[(\d{1,2}:\d{2}(?:[.:]\d{1,3})?)\](.*)$""")

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue

        // Skip metadata header tags
        if (trimmed.startsWith("[ti:") || trimmed.startsWith("[ar:") ||
            trimmed.startsWith("[al:") || trimmed.startsWith("[by:") ||
            trimmed.startsWith("[offset:")) {
            continue
        }

        val match = regex.find(trimmed)
        if (match != null) {
            val timeStr = match.groupValues[1]
            val textStr = match.groupValues[2].trim()
            val ms = parseLyricTimestamp(timeStr)
            result.add(EditableLyricLine(timestampMs = ms, text = textStr))
        } else {
            result.add(EditableLyricLine(timestampMs = null, text = trimmed))
        }
    }
    return result
}

fun formatLinesToRawLyrics(lines: List<EditableLyricLine>): String {
    return lines.joinToString("\n") { line ->
        if (line.timestampMs != null && line.timestampMs!! >= 0) {
            "[${formatLyricTimestamp(line.timestampMs)}]${line.text}"
        } else {
            line.text
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloraLyricsEditorDialog(
    initialLyrics: String,
    viewModel: MusicViewModel,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var isSyncedMode by remember { mutableStateOf(false) }
    var rawText by remember { mutableStateOf(TextFieldValue(initialLyrics)) }
    val lines = remember { mutableStateListOf<EditableLyricLine>() }

    // Initialize lines from initialLyrics
    LaunchedEffect(Unit) {
        val parsed = parseRawLyricsToLines(initialLyrics)
        lines.clear()
        lines.addAll(parsed)
        // If initial lyrics have timestamps, default to Disinkronkan
        if (parsed.any { it.timestampMs != null }) {
            isSyncedMode = true
        }
    }

    val playbackPosition by viewModel.playerController.playbackPosition.collectAsState()
    val isPlaying by viewModel.playerController.isPlaying.collectAsState()
    val duration by viewModel.playerController.duration.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var lineToEditIndex by remember { mutableStateOf<Int?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .systemBarsPadding(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                // Header (Batalkan - Toggle - Selesai)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceSlate)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Batalkan",
                            tint = Color.White
                        )
                    }

                    // Segmented Toggle Pill [Sederhana | Disinkronkan]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(CardSlate)
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sederhana
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isSyncedMode) EmeraldGreen else Color.Transparent)
                                .clickable {
                                    if (isSyncedMode) {
                                        // Sync to Sederhana
                                        rawText = TextFieldValue(formatLinesToRawLyrics(lines))
                                        isSyncedMode = false
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sederhana",
                                fontSize = 13.sp,
                                fontWeight = if (!isSyncedMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isSyncedMode) Color.White else TextSecondary
                            )
                        }

                        // Disinkronkan
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSyncedMode) EmeraldGreen else Color.Transparent)
                                .clickable {
                                    if (!isSyncedMode) {
                                        // Parse Sederhana to lines
                                        val parsed = parseRawLyricsToLines(rawText.text)
                                        lines.clear()
                                        lines.addAll(parsed)
                                        isSyncedMode = true
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Disinkronkan",
                                fontSize = 13.sp,
                                fontWeight = if (isSyncedMode) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSyncedMode) Color.White else TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val finalLyrics = if (isSyncedMode) {
                                formatLinesToRawLyrics(lines)
                            } else {
                                rawText.text
                            }
                            onSave(finalLyrics)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selesai",
                            tint = EmeraldGreen
                        )
                    }
                }

                HorizontalDivider(color = DividerSlate, thickness = 1.dp)

                // Main Content Body
                if (!isSyncedMode) {
                    // MODE SEDERHANA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = rawText,
                            onValueChange = { rawText = it },
                            placeholder = {
                                Text(
                                    "Ketik atau tempel lirik di sini...\nFormat [mm:ss.xx] juga didukung.",
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                lineHeight = 22.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = EmeraldGreen
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        HorizontalDivider(color = DividerSlate, thickness = 1.dp)

                        // Bilah Bawah Sederhana: Pilih Semua & Tempel (NO Cari button)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceSlate)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    rawText = rawText.copy(
                                        selection = androidx.compose.ui.text.TextRange(0, rawText.text.length)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pilih Semua",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = {
                                    val clip = clipboardManager.getText()
                                    if (clip != null && clip.text.isNotEmpty()) {
                                        val text = rawText.text
                                        val sel = rawText.selection
                                        val newText = text.substring(0, sel.start) + clip.text + text.substring(sel.end)
                                        rawText = TextFieldValue(
                                            text = newText,
                                            selection = androidx.compose.ui.text.TextRange(sel.start + clip.text.length)
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Tempel",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                } else {
                    // MODE DISINKRONKAN
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (lines.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Belum ada baris lirik.",
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Beralih ke tab 'Sederhana' untuk menempel teks lirik.",
                                        color = TextSecondary.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                itemsIndexed(
                                    items = lines,
                                    key = { _, item -> item.id }
                                ) { index, item ->
                                    val isSynced = item.timestampMs != null && item.timestampMs!! >= 0

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSynced) CardSlate else SurfaceSlate
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Tombol Timestamp / Sync Button
                                            Button(
                                                onClick = {
                                                    lines[index] = item.copy(timestampMs = playbackPosition)
                                                    // Auto advance to next line if playing
                                                    if (isPlaying && index + 1 < lines.size) {
                                                        coroutineScope.launch {
                                                            listState.animateScrollToItem(index + 1)
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSynced) EmeraldGreen.copy(alpha = 0.2f) else CardSlate
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text(
                                                    text = formatLyricTimestamp(item.timestampMs),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = if (isSynced) EmeraldLight else TextSecondary
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Teks Baris (Klik untuk buka pop-up edit)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { lineToEditIndex = index }
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = if (item.text.isBlank()) "(Baris kosong)" else item.text,
                                                    fontSize = 13.sp,
                                                    color = if (item.text.isBlank()) TextSecondary else Color.White,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Tombol Play Kecil (Preview/Jump)
                                            IconButton(
                                                onClick = {
                                                    if (item.timestampMs != null) {
                                                        viewModel.playerController.seekTo(item.timestampMs!!)
                                                        if (!isPlaying) {
                                                            viewModel.playerController.togglePlay()
                                                        }
                                                    }
                                                },
                                                enabled = item.timestampMs != null,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Putar dari sini",
                                                    tint = if (item.timestampMs != null) EmeraldLight else DividerSlate,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Tombol Tambah Baris di akhir list
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                lines.add(EditableLyricLine(text = ""))
                                                coroutineScope.launch {
                                                    listState.animateScrollToItem(lines.lastIndex)
                                                }
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Tambah Baris Lirik",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = EmeraldGreen
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = DividerSlate, thickness = 1.dp)

                        // Bilah Pemutar Bawah (Sync Player Controller)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceSlate)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Kontrol Navigasi Audio
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Play / Pause
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                        .clickable { viewModel.playerController.togglePlay() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = SoftWhite,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Rewind -5s
                                IconButton(
                                    onClick = {
                                        val newPos = (playbackPosition - 5000L).coerceAtLeast(0L)
                                        viewModel.playerController.seekTo(newPos)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastRewind,
                                        contentDescription = "Mundur 5 detik",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Forward +5s
                                IconButton(
                                    onClick = {
                                        val newPos = (playbackPosition + 5000L).coerceAtMost(duration)
                                        viewModel.playerController.seekTo(newPos)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastForward,
                                        contentDescription = "Maju 5 detik",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Timer Berjalan Live
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatLyricTimestamp(playbackPosition),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldLight
                                )
                                Text(
                                    text = "Posisi Live",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Pop-up Edit Baris (Row Edit Dialog)
    lineToEditIndex?.let { index ->
        val currentLine = lines.getOrNull(index)
        if (currentLine != null) {
            EditLyricRowDialog(
                initialTimestampMs = currentLine.timestampMs,
                initialText = currentLine.text,
                onSave = { updatedMs, updatedText ->
                    lines[index] = currentLine.copy(
                        timestampMs = updatedMs,
                        text = updatedText
                    )
                    lineToEditIndex = null
                },
                onDelete = {
                    lines.removeAt(index)
                    lineToEditIndex = null
                },
                onDismiss = { lineToEditIndex = null }
            )
        }
    }
}

@Composable
fun EditLyricRowDialog(
    initialTimestampMs: Long?,
    initialText: String,
    onSave: (Long?, String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var timestampMs by remember { mutableStateOf(initialTimestampMs) }
    var timeText by remember { mutableStateOf(formatLyricTimestamp(initialTimestampMs)) }
    var lyricText by remember { mutableStateOf(initialText) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceSlate),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Edit Baris Lirik",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stepper [-] [Time Input] [+]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol [-]
                    IconButton(
                        onClick = {
                            val current = timestampMs ?: 0L
                            val newMs = (current - 200L).coerceAtLeast(0L)
                            timestampMs = newMs
                            timeText = formatLyricTimestamp(newMs)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardSlate, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Kurang 200ms",
                            tint = Color.White
                        )
                    }

                    // Input Waktu Manual
                    OutlinedTextField(
                        value = timeText,
                        onValueChange = {
                            timeText = it
                            val parsed = parseLyricTimestamp(it)
                            if (parsed != null) {
                                timestampMs = parsed
                            }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = EmeraldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = DividerSlate
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Tombol [+]
                    IconButton(
                        onClick = {
                            val current = timestampMs ?: 0L
                            val newMs = current + 200L
                            timestampMs = newMs
                            timeText = formatLyricTimestamp(newMs)
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardSlate, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah 200ms",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Teks Lirik
                OutlinedTextField(
                    value = lyricText,
                    onValueChange = { lyricText = it },
                    label = { Text("Teks Baris", color = TextSecondary) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = DividerSlate,
                        focusedLabelColor = EmeraldGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: HAPUS, BATALKAN, OK
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("HAPUS", fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("BATALKAN", color = TextSecondary, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                val parsed = parseLyricTimestamp(timeText)
                                onSave(parsed ?: timestampMs, lyricText)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
