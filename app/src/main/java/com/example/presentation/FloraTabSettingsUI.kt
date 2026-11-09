package com.example.presentation

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.CardSlate
import kotlin.math.roundToInt

enum class TabRole(val label: String) {
    MAIN("Utama"),
    SUB("Subtab"),
    HIDDEN("Sembunyi"),
    HEADER("Header")
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
    val customTabIcons by viewModel.customTabIcons.collectAsState()
    
    var iconPickerItem by remember { mutableStateOf<TabEditItem?>(null) }
    var forceRefresh by remember { mutableStateOf(0) }

    val allItems = remember(tabConfigs, hiddenModules, forceRefresh) {
        val list = mutableStateListOf<TabEditItem>()
        for (config in tabConfigs) {
            if (config.isCustom) {
                list.add(TabEditItem("CUSTOM_${config.name}", config.name, TabRole.MAIN, true))
                for (item in config.items) {
                    if (item.startsWith("CUSTOM_")) {
                        list.add(TabEditItem(item, item.removePrefix("CUSTOM_"), TabRole.SUB, true))
                    } else {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            } else {
                list.add(TabEditItem(config.name, config.name, TabRole.MAIN, false))
                for (item in config.items) {
                    if (item.startsWith("CUSTOM_")) {
                        list.add(TabEditItem(item, item.removePrefix("CUSTOM_"), TabRole.SUB, true))
                    } else if (item != config.name) {
                        list.add(TabEditItem(item, item, TabRole.SUB, false))
                    }
                }
            }
        }
list.add(TabEditItem("HEADER_HIDDEN", "Tab Tersembunyi", TabRole.HEADER, false))
        for (hidden in hiddenModules) {
            if (hidden.startsWith("CUSTOM_")) {
                list.add(TabEditItem(hidden, hidden.removePrefix("CUSTOM_"), TabRole.HIDDEN, true))
            } else {
                list.add(TabEditItem(hidden, hidden, TabRole.HIDDEN, false))
            }
        }
        list
    }

        var showAddCustomDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var newCustomTabName by remember { mutableStateOf("") }

    var draggedItemId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    val itemHeightDp = 56.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeightDp.toPx() }

    val draggedIndex = allItems.indexOfFirst { it.id == draggedItemId }
    val targetIndex = if (draggedIndex != -1) {
        (draggedIndex + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, allItems.lastIndex)
    } else {
        -1
    }

    val saveFn = { items: List<TabEditItem> ->
        val newConfigs = mutableListOf<TabConfig>()
        val currentHidden = mutableListOf<String>()
        
        var currentConfigName = ""
        var currentConfigItems = mutableListOf<String>()
        var currentIsCustom = false
        
        for (item in items) {
            if (item.role == TabRole.HIDDEN) {
                currentHidden.add(item.id)
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
                    currentConfigItems.add(item.id)
                } else {
                    currentConfigName = item.name
                    currentConfigItems = mutableListOf()
                    currentIsCustom = item.isCustom
                    currentConfigItems.add(item.id)
                }
            }
        }
        if (currentConfigName.isNotEmpty()) {
            newConfigs.add(TabConfig(currentConfigName, currentConfigItems.toList(), currentIsCustom))
        }
        
        viewModel.saveTabConfigs(newConfigs, currentHidden)
    }

    val validateAndSaveFn = {
        val headerIdx = allItems.indexOfFirst { it.id == "HEADER_HIDDEN" }
        if (headerIdx != -1) {
            for (i in allItems.indices) {
                if (allItems[i].role == TabRole.HEADER) continue
                if (i < headerIdx) {
                    if (allItems[i].role == TabRole.HIDDEN) {
                        allItems[i] = allItems[i].copy(role = TabRole.MAIN)
                    }
                } else {
                    if (allItems[i].role != TabRole.HIDDEN) {
                        allItems[i] = allItems[i].copy(role = TabRole.HIDDEN)
                    }
                }
            }
        }

        var hasMain = false
        for (item in allItems) {
            if (item.role == TabRole.MAIN) {
                hasMain = true
                break
            }
        }
        
        if (!hasMain) {
            val firstVisibleIdx = allItems.indexOfFirst { it.role != TabRole.HIDDEN && it.role != TabRole.HEADER }
            if (firstVisibleIdx != -1) {
                allItems[firstVisibleIdx] = allItems[firstVisibleIdx].copy(role = TabRole.MAIN)
            }
        }

        var seenMain = false
        var currentMainName = ""
        for (i in allItems.indices) {
            val item = allItems[i]
            if (item.role == TabRole.MAIN) {
                seenMain = true
                currentMainName = item.name
            } else if (item.role == TabRole.SUB) {
                if (!seenMain || currentMainName in listOf("Lagu", "Artis", "Album")) {
                    allItems[i] = item.copy(role = TabRole.MAIN)
                    seenMain = true
                    currentMainName = item.name
                }
            }
        }
        
        val success = saveFn(allItems)
        if (!success) {
            forceRefresh++
        }
    }


    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Pengaturan Tab") },
            text = { Text("Apakah Anda yakin ingin mengembalikan pengaturan tab ke bawaan? Semua tab custom akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTabsToDefault()
                        showResetDialog = false
                    }
                ) { Text("Reset", color = EmeraldGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
            }
        )
    }
    if (iconPickerItem != null) {
        AlertDialog(
            onDismissRequest = { iconPickerItem = null },
            title = { Text("Pilih Ikon") },
            text = {
                val iconOptions = listOf(
                    "api" to Icons.Default.Whatshot,
                    "bintang" to Icons.Default.Star,
                    "bulan" to Icons.Default.DarkMode,
                    "game/console" to Icons.Default.VideogameAsset,
                    "olahraga" to Icons.Default.FitnessCenter,
                    "buku" to Icons.Default.MenuBook,
                    "gitar" to Icons.Default.MusicNote,
                    "headphone" to Icons.Default.Headphones,
                    "kamera" to Icons.Default.CameraAlt,
                    "film" to Icons.Default.Movie,
                    "mobil" to Icons.Default.DirectionsCar,
                    "pesawat" to Icons.Default.Flight,
                    "laptop" to Icons.Default.Laptop,
                    "belanja" to Icons.Default.ShoppingCart,
                    "tas" to Icons.Default.Work,
                    "folder" to Icons.Default.Folder
                )
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(iconOptions.size) { index ->
                        val (name, iconRes) = iconOptions[index]
                        IconButton(onClick = {
                            viewModel.saveCustomTabIcon(iconPickerItem!!.name, name)
                            iconPickerItem = null
                        }) {
                            Icon(iconRes, contentDescription = name, tint = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { iconPickerItem = null }) { Text("Tutup") }
            }
        )
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
                            validateAndSaveFn()
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
            Text("Pengaturan Tab", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opsi", tint = Color.White)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CardSlate)
                ) {
                    DropdownMenuItem(
                        text = { Text("Tambah Tab Custom", color = Color.White) },
                        onClick = {
                            showMenu = false
                            showAddCustomDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Reset Pengaturan", color = Color.White) },
                        onClick = {
                            showMenu = false
                            showResetDialog = true
                        }
                    )
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tahan dan geser untuk memindahkan",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 82.dp)
        ) {
            allItems.forEachIndexed { index, item ->
                key(item.id) {
                    val isDragging = index == draggedIndex
                    val zIndex = if (isDragging) 1f else 0f
                    
                    val translationY = if (isDragging) {
                        dragOffset
                    } else if (draggedIndex != -1) {
                        if (draggedIndex < index && index <= targetIndex) {
                            -itemHeightPx
                        } else if (draggedIndex > index && index >= targetIndex) {
                            itemHeightPx
                        } else {
                            0f
                        }
                    } else {
                        0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp)
                            .zIndex(zIndex)
                            .graphicsLayer { this.translationY = translationY }
                            .pointerInput(item.id, allItems) {
                                if (item.role != TabRole.HEADER) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { 
                                            draggedItemId = item.id
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (draggedItemId == null) return@detectDragGesturesAfterLongPress
                                            dragOffset += dragAmount.y
                                        },
                                        onDragEnd = {
                                            val currentDraggedIndex = allItems.indexOfFirst { it.id == item.id }
                                            val currentTargetIndex = if (currentDraggedIndex != -1) {
                                                (currentDraggedIndex + (dragOffset / itemHeightPx).roundToInt()).coerceIn(0, allItems.lastIndex)
                                            } else -1

                                            if (currentDraggedIndex != -1 && currentTargetIndex != -1 && currentDraggedIndex != currentTargetIndex) {
                                                val movedItem = allItems.removeAt(currentDraggedIndex)
                                                allItems.add(currentTargetIndex, movedItem)
                                            }
                                            draggedItemId = null
                                            dragOffset = 0f
                                            validateAndSaveFn()
                                        },
                                        onDragCancel = {
                                            draggedItemId = null
                                            dragOffset = 0f
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                    if (item.role == TabRole.HEADER) {
                        Text(
                            text = item.name,
                            color = EmeraldGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    } else {
                        val customIconName = customTabIcons[item.name]
                        val icon = if (customIconName != null) {
                            when (customIconName) {
                                "api" -> Icons.Default.Whatshot
                                "bintang" -> Icons.Default.Star
                                "bulan" -> Icons.Default.DarkMode
                                "game/console" -> Icons.Default.VideogameAsset
                                "olahraga" -> Icons.Default.FitnessCenter
                                "buku" -> Icons.Default.MenuBook
                                "gitar" -> Icons.Default.MusicNote
                                "headphone" -> Icons.Default.Headphones
                                "kamera" -> Icons.Default.CameraAlt
                                "film" -> Icons.Default.Movie
                                "mobil" -> Icons.Default.DirectionsCar
                                "pesawat" -> Icons.Default.Flight
                                "laptop" -> Icons.Default.Laptop
                                "belanja" -> Icons.Default.ShoppingCart
                                "tas" -> Icons.Default.Work
                                else -> Icons.Default.Folder
                            }
                        } else {
                            when (item.name) {
                                "Beranda" -> Icons.Default.Home
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
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .then(
                                    if (isDragging) {
                                        Modifier
                                            .shadow(8.dp, RoundedCornerShape(8.dp))
                                            .background(CardSlate, RoundedCornerShape(8.dp))
                                            .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    } else {
                                        Modifier.background(Color.Transparent, RoundedCornerShape(8.dp))
                                    }
                                )
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
                            
                            if (item.isCustom) {
                                IconButton(onClick = { iconPickerItem = item }, modifier = Modifier.size(24.dp)) {
                                    Icon(icon, contentDescription = "Pilih Ikon", tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Icon(icon, contentDescription = null, tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.name, 
                                color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White, 
                                fontSize = 16.sp, 
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (item.isCustom) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        allItems.remove(item)
                                        validateAndSaveFn()
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Button(
                                        onClick = {
                                            val nextRole = when (item.role) {
                                                TabRole.MAIN -> TabRole.SUB
                                                else -> TabRole.MAIN
                                            }
                                            val idx = allItems.indexOf(item)
                                            if (idx != -1) {
                                                if (nextRole == TabRole.SUB && idx == 0) {
                                                    val nextMainIdx = allItems.indexOfFirst { it != item && it.role == TabRole.MAIN }
                                                    if (nextMainIdx != -1) {
                                                        val moved = allItems.removeAt(idx)
                                                        allItems.add(nextMainIdx, moved.copy(role = TabRole.SUB))
                                                        validateAndSaveFn()
                                                        return@Button
                                                    }
                                                }
                                                allItems[idx] = item.copy(role = nextRole)
                                                validateAndSaveFn()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(item.role.label, fontSize = 12.sp, color = EmeraldGreen)
                                    }
                                }
                            } else if (item.role != TabRole.HIDDEN) {
                                if (item.name !in listOf("Lagu", "Artis", "Album")) {
                                    Button(
                                        onClick = {
                                            val nextRole = when (item.role) {
                                                TabRole.MAIN -> TabRole.SUB
                                                else -> TabRole.MAIN
                                            }
                                            val idx = allItems.indexOf(item)
                                            if (idx != -1) {
                                                if (nextRole == TabRole.SUB && idx == 0) {
                                                    val nextMainIdx = allItems.indexOfFirst { it != item && it.role == TabRole.MAIN }
                                                    if (nextMainIdx != -1) {
                                                        val moved = allItems.removeAt(idx)
                                                        allItems.add(nextMainIdx, moved.copy(role = TabRole.SUB))
                                                        validateAndSaveFn()
                                                        return@Button
                                                    }
                                                }
                                                allItems[idx] = item.copy(role = nextRole)
                                                validateAndSaveFn()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CardSlate),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(item.role.label, fontSize = 12.sp, color = EmeraldGreen)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .background(CardSlate, RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.role.label, fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }
}
