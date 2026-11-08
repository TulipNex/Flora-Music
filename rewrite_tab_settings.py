import re

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'r') as f:
    content = f.read()

new_content = """package com.example.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.CardSlate

enum class TabRole(val label: String) {
    MAIN("Utama"),
    SUB("Subtab"),
    HIDDEN("Sembunyi")
}

data class TabEditItem(
    val id: String,
    val name: String,
    val role: TabRole,
    val isCustom: Boolean
)

@Composable
fun TabSettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit
) {
    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState()

    val allItems = remember(tabConfigs, hiddenModules) {
        val list = mutableStateListOf<TabEditItem>()
        for (config in tabConfigs) {
            if (config.isCustom) {
                list.add(TabEditItem("CUSTOM_${config.name}", config.name, TabRole.MAIN, true))
                for (item in config.items) {
                    list.add(TabEditItem(item, item, TabRole.SUB, false))
                }
            } else {
                list.add(TabEditItem(config.name, config.name, TabRole.MAIN, false))
                for (item in config.items) {
                    if (item != config.name) {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            }
        }
        for (hidden in hiddenModules) {
            list.add(TabEditItem(hidden, hidden, TabRole.HIDDEN, false))
        }
        list
    }

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var newCustomTabName by remember { mutableStateOf("") }

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val itemHeightDp = 56.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }

    val saveFn = { items: List<TabEditItem> ->
        val newConfigs = mutableListOf<TabConfig>()
        val currentHidden = mutableListOf<String>()
        
        var currentConfigName = ""
        var currentConfigItems = mutableListOf<String>()
        var currentIsCustom = false
        
        for (item in items) {
            if (item.role == TabRole.HIDDEN) {
                if (!item.isCustom) {
                    currentHidden.add(item.id)
                }
                continue
            }
            
            if (item.role == TabRole.MAIN) {
                if (currentConfigName.isNotEmpty()) {
                    newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
                }
                currentConfigName = item.name
                currentConfigItems = mutableListOf()
                currentIsCustom = item.isCustom
                if (!item.isCustom) {
                    currentConfigItems.add(item.id)
                }
            } else if (item.role == TabRole.SUB) {
                if (currentConfigName.isNotEmpty()) {
                    if (!item.isCustom) {
                        currentConfigItems.add(item.id)
                    }
                } else {
                    currentConfigName = item.name
                    currentConfigItems = mutableListOf()
                    currentIsCustom = item.isCustom
                    if (!item.isCustom) {
                        currentConfigItems.add(item.id)
                    }
                }
            }
        }
        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        viewModel.saveTabConfigs(newConfigs, currentHidden)
    }

    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Tambah Tab Custom") },
            text = {
                OutlinedTextField(
                    value = newCustomTabName,
                    onValueChange = { newCustomTabName = it },
                    label = { Text("Nama Tab") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCustomTabName.isNotBlank()) {
                            allItems.add(0, TabEditItem("CUSTOM_$newCustomTabName", newCustomTabName, TabRole.MAIN, true))
                            saveFn(allItems)
                        }
                        showAddCustomDialog = false
                        newCustomTabName = ""
                    }
                ) { Text("Tambah", color = EmeraldGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) { Text("Batal") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Pengaturan Tab", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tekan lama dan geser untuk mengatur tab",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { showAddCustomDialog = true }) {
                Text("+ Tab Custom", color = EmeraldGreen, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {
            allItems.forEachIndexed { index, item ->
                val isDragging = index == draggedIndex
                val zIndex = if (isDragging) 1f else 0f
                val translationY = if (isDragging) dragOffset else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp)
                        .zIndex(zIndex)
                        .graphicsLayer { this.translationY = translationY }
                        .pointerInput(item) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { 
                                    val currIdx = allItems.indexOf(item)
                                    draggedIndex = currIdx
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (draggedIndex == null) return@detectDragGesturesAfterLongPress
                                    dragOffset += dragAmount.y
                                    
                                    if (dragOffset > itemHeightPx * 0.5f && draggedIndex!! < allItems.lastIndex) {
                                        val nextIdx = draggedIndex!! + 1
                                        val temp = allItems[draggedIndex!!]
                                        allItems[draggedIndex!!] = allItems[nextIdx]
                                        allItems[nextIdx] = temp
                                        draggedIndex = nextIdx
                                        dragOffset -= itemHeightPx
                                    } else if (dragOffset < -itemHeightPx * 0.5f && draggedIndex!! > 0) {
                                        val prevIdx = draggedIndex!! - 1
                                        val temp = allItems[draggedIndex!!]
                                        allItems[draggedIndex!!] = allItems[prevIdx]
                                        allItems[prevIdx] = temp
                                        draggedIndex = prevIdx
                                        dragOffset += itemHeightPx
                                    }
                                },
                                onDragEnd = {
                                    draggedIndex = null
                                    dragOffset = 0f
                                    saveFn(allItems)
                                },
                                onDragCancel = {
                                    draggedIndex = null
                                    dragOffset = 0f
                                    saveFn(allItems)
                                }
                            )
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val icon = when (item.name) {
                        "Lagu" -> Icons.Default.MusicNote
                        "Album" -> Icons.Default.Album
                        "Artis" -> Icons.Default.Person
                        "Favorit" -> Icons.Default.Favorite
                        "Riwayat" -> Icons.Default.History
                        "Teratas" -> Icons.Default.TrendingUp
                        "Playlist" -> Icons.Default.PlaylistPlay
                        "Genre" -> Icons.Default.Category
                        else -> Icons.Default.Folder
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(if (isDragging) CardSlate else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DragHandle, contentDescription = "Geser", tint = TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        if (item.role == TabRole.SUB) {
                            Spacer(modifier = Modifier.width(24.dp))
                            Icon(Icons.Default.SubdirectoryArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.name, 
                            color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, 
                            fontSize = 16.sp, 
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (item.isCustom) {
                            IconButton(onClick = {
                                allItems.remove(item)
                                saveFn(allItems)
                            }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    val nextRole = when (item.role) {
                                        TabRole.MAIN -> TabRole.SUB
                                        TabRole.SUB -> TabRole.HIDDEN
                                        TabRole.HIDDEN -> TabRole.MAIN
                                    }
                                    val idx = allItems.indexOf(item)
                                    if (idx != -1) {
                                        allItems[idx] = item.copy(role = nextRole)
                                        saveFn(allItems)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(item.role.label, fontSize = 12.sp, color = if (item.role == TabRole.HIDDEN) TextSecondary else EmeraldGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/presentation/FloraTabSettingsUI.kt', 'w') as f:
    f.write(new_content)
