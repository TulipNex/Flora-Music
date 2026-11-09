package com.example.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.ui.theme.CardSlate
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TextSecondary
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

val TAB_ICON_OPTIONS = listOf(
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
    "folder" to Icons.Default.Folder,
    "favorit" to Icons.Default.Favorite,
    "petir" to Icons.Default.Bolt,
    "kopi" to Icons.Default.LocalCafe,
    "tag" to Icons.Default.LocalOffer
)

@Composable
fun TabSettingsScreen(
    viewModel: MusicViewModel,
    onBackClick: () -> Unit
) {
    val tabConfigs by viewModel.tabConfigs.collectAsState()
    val hiddenModules by viewModel.hiddenModules.collectAsState()
    val customTabIcons by viewModel.customTabIcons.collectAsState()

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
    var showResetDialog by remember { mutableStateOf(false) }
    var editingTabItem by remember { mutableStateOf<TabEditItem?>(null) }

    // Add dialog state
    var newCustomTabName by remember { mutableStateOf("") }
    var newCustomTabRole by remember { mutableStateOf(TabRole.SUB) }
    var newCustomParentName by remember { mutableStateOf("Beranda") }
    var newCustomSelectedIcon by remember { mutableStateOf("api") }

    // Edit dialog state
    var editTabName by remember { mutableStateOf("") }
    var editTabRole by remember { mutableStateOf(TabRole.SUB) }
    var editParentName by remember { mutableStateOf("Beranda") }
    var editSelectedIcon by remember { mutableStateOf("api") }

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

        val fixedTabs = listOf("Lagu", "Artis", "Album")
        val allConfigNames = newConfigs.map { it.name }
        val missing = fixedTabs.filter { it !in allConfigNames }
        if (missing.isNotEmpty()) {
            false
        } else {
            viewModel.saveTabConfigs(newConfigs, currentHidden)
            true
        }
    }

    val validateAndSaveFn = {
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

    // List of active Main Tab names to choose as parent for Subtabs
    val activeMainTabs = remember(allItems, forceRefresh) {
        allItems.filter { it.role == TabRole.MAIN && it.name !in listOf("Lagu", "Artis", "Album") }
            .map { it.name }
            .ifEmpty { listOf("Beranda") }
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Pengaturan Tab", color = Color.White) },
            text = { Text("Apakah Anda yakin ingin mengembalikan pengaturan tab ke bawaan? Semua tab custom akan dihapus.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTabsToDefault()
                        showResetDialog = false
                    }
                ) { Text("Reset", color = EmeraldGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Batal", color = TextSecondary) }
            },
            containerColor = CardSlate
        )
    }

    // ================= DIALOG 1: TAMBAH TAB / SUBTAB =================
    if (showAddCustomDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomDialog = false },
            title = { Text("Tambah Tab / Subtab", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Tentukan nama, posisi hierarki, dan ikon tab:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newCustomTabName,
                        onValueChange = { newCustomTabName = it },
                        label = { Text("Nama Tab / Subtab") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF2E3832)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Peran Hierarki:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Role Segmented Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2421))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { newCustomTabRole = TabRole.SUB },
                            shape = RoundedCornerShape(6.dp),
                            color = if (newCustomTabRole == TabRole.SUB) EmeraldGreen else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "↳ Subtab",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newCustomTabRole == TabRole.SUB) Color.Black else TextSecondary
                                )
                            }
                        }
                        Surface(
                            onClick = { newCustomTabRole = TabRole.MAIN },
                            shape = RoundedCornerShape(6.dp),
                            color = if (newCustomTabRole == TabRole.MAIN) EmeraldGreen else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "Tab Utama",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newCustomTabRole == TabRole.MAIN) Color.Black else TextSecondary
                                )
                            }
                        }
                    }

                    // Pilihan Induk jika Subtab
                    if (newCustomTabRole == TabRole.SUB) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Pilih Tab Utama Induk:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2421))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            activeMainTabs.forEach { parentName ->
                                val isSelected = newCustomParentName == parentName
                                Surface(
                                    onClick = { newCustomParentName = parentName },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else Color(0xFF2E3832)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            parentName,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldGreen else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pilih Ikon:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Grid Ikon Scroll Vertikal Saja (5 Kolom, Fixed Height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161A18))
                            .padding(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(TAB_ICON_OPTIONS.size) { index ->
                                val (iconKey, iconRes) = TAB_ICON_OPTIONS[index]
                                val isSelected = newCustomSelectedIcon == iconKey
                                IconButton(
                                    onClick = { newCustomSelectedIcon = iconKey },
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen.copy(alpha = 0.22f) else Color.Transparent)
                                ) {
                                    Icon(
                                        iconRes,
                                        contentDescription = iconKey,
                                        tint = if (isSelected) EmeraldGreen else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCustomTabName.isNotBlank()) {
                            val newItem = TabEditItem(
                                id = "CUSTOM_$newCustomTabName",
                                name = newCustomTabName,
                                role = newCustomTabRole,
                                isCustom = true
                            )
                            if (newCustomTabRole == TabRole.SUB) {
                                val parentIdx = allItems.indexOfFirst { it.role == TabRole.MAIN && it.name == newCustomParentName }
                                if (parentIdx != -1) {
                                    allItems.add(parentIdx + 1, newItem)
                                } else {
                                    allItems.add(0, newItem)
                                }
                            } else {
                                allItems.add(0, newItem)
                            }
                            viewModel.saveCustomTabIcon(newCustomTabName, newCustomSelectedIcon)
                            validateAndSaveFn()
                        }
                        showAddCustomDialog = false
                        newCustomTabName = ""
                    }
                ) { Text("Tambah", color = EmeraldGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomDialog = false }) { Text("Batal", color = TextSecondary) }
            },
            containerColor = CardSlate
        )
    }

    // ================= DIALOG 2: EDIT TAB (LENGKAP DENGAN HAPUS HANYA UNTUK CUSTOM) =================
    if (editingTabItem != null) {
        val currentItem = editingTabItem!!
        LaunchedEffect(currentItem) {
            editTabName = currentItem.name
            editTabRole = currentItem.role
            editSelectedIcon = customTabIcons[currentItem.name] ?: "api"

            // Cari parent jika subtab
            val idx = allItems.indexOf(currentItem)
            if (idx != -1) {
                val parent = allItems.take(idx).lastOrNull { it.role == TabRole.MAIN }
                editParentName = parent?.name ?: "Beranda"
            }
        }

        AlertDialog(
            onDismissRequest = { editingTabItem = null },
            title = { Text("Edit Tab: ${currentItem.name}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ubah nama, peran hierarki, atau ikon tab:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editTabName,
                        onValueChange = { if (currentItem.isCustom) editTabName = it },
                        label = { Text(if (currentItem.isCustom) "Nama Tab" else "Nama Tab (Bawaan)") },
                        enabled = currentItem.isCustom,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.6f),
                            focusedBorderColor = EmeraldGreen,
                            unfocusedBorderColor = Color(0xFF2E3832),
                            disabledBorderColor = Color(0xFF252B28)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Peran Hierarki:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Role Segmented Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2421))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            onClick = { editTabRole = TabRole.SUB },
                            shape = RoundedCornerShape(6.dp),
                            color = if (editTabRole == TabRole.SUB) EmeraldGreen else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "↳ Subtab",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (editTabRole == TabRole.SUB) Color.Black else TextSecondary
                                )
                            }
                        }
                        Surface(
                            onClick = { editTabRole = TabRole.MAIN },
                            shape = RoundedCornerShape(6.dp),
                            color = if (editTabRole == TabRole.MAIN) EmeraldGreen else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    "Tab Utama",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (editTabRole == TabRole.MAIN) Color.Black else TextSecondary
                                )
                            }
                        }
                    }

                    // Pilihan Induk jika Subtab
                    if (editTabRole == TabRole.SUB) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Pilih Tab Utama Induk:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2421))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            activeMainTabs.forEach { parentName ->
                                val isSelected = editParentName == parentName
                                Surface(
                                    onClick = { editParentName = parentName },
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent,
                                    border = BorderStroke(1.dp, if (isSelected) EmeraldGreen else Color(0xFF2E3832)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            parentName,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldGreen else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pilih Ikon:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Grid Ikon Scroll Vertikal Saja (5 Kolom, Fixed Height)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161A18))
                            .padding(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(TAB_ICON_OPTIONS.size) { index ->
                                val (iconKey, iconRes) = TAB_ICON_OPTIONS[index]
                                val isSelected = editSelectedIcon == iconKey
                                IconButton(
                                    onClick = { editSelectedIcon = iconKey },
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldGreen.copy(alpha = 0.22f) else Color.Transparent)
                                ) {
                                    Icon(
                                        iconRes,
                                        contentDescription = iconKey,
                                        tint = if (isSelected) EmeraldGreen else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Hapus: HANYA untuk tab custom
                    if (currentItem.isCustom) {
                        TextButton(
                            onClick = {
                                allItems.remove(currentItem)
                                validateAndSaveFn()
                                editingTabItem = null
                            }
                        ) {
                            Text("Hapus Tab", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { editingTabItem = null }) {
                            Text("Batal", color = TextSecondary)
                        }
                        TextButton(
                            onClick = {
                                val idx = allItems.indexOf(currentItem)
                                if (idx != -1) {
                                    val updated = currentItem.copy(
                                        name = if (currentItem.isCustom && editTabName.isNotBlank()) editTabName else currentItem.name,
                                        role = editTabRole
                                    )
                                    allItems[idx] = updated

                                    // Simpan ikon terpilih
                                    viewModel.saveCustomTabIcon(updated.name, editSelectedIcon)

                                    // Jika posisi parent berubah
                                    if (editTabRole == TabRole.SUB) {
                                        val parentIdx = allItems.indexOfFirst { it.role == TabRole.MAIN && it.name == editParentName }
                                        if (parentIdx != -1 && parentIdx != idx) {
                                            allItems.removeAt(idx)
                                            val targetInsert = if (parentIdx < idx) parentIdx + 1 else parentIdx
                                            allItems.add(targetInsert.coerceIn(0, allItems.size), updated)
                                        }
                                    }
                                    validateAndSaveFn()
                                }
                                editingTabItem = null
                            }
                        ) {
                            Text("Simpan", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            containerColor = CardSlate
        )
    }

    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Tab Beranda", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))

            // Action Buttons: + Tambah (Emerald) & ↺ Reset
            Button(
                onClick = { showAddCustomDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tambah", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Default", tint = Color.White)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Tahan dan geser untuk mengubah urutan tab.",
                color = TextSecondary,
                fontSize = 12.5.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                        } else 0f
                    } else 0f

                    val icon: ImageVector = when {
                        item.name == "Beranda" -> Icons.Default.Home
                        item.name == "Lagu" -> Icons.Default.MusicNote
                        item.name == "Album" -> Icons.Default.Album
                        item.name == "Artis" -> Icons.Default.Person
                        item.name == "Favorit" -> Icons.Default.Favorite
                        item.name == "Riwayat" -> Icons.Default.History
                        item.name == "Teratas" -> Icons.Default.TrendingUp
                        item.name == "Terbaru" -> Icons.Default.AutoAwesome
                        item.name == "Playlist" -> Icons.Default.QueueMusic
                        item.name == "Genre" -> Icons.Default.Folder
                        else -> {
                            val savedIcon = customTabIcons[item.name] ?: "api"
                            TAB_ICON_OPTIONS.find { it.first == savedIcon }?.second ?: Icons.Default.Whatshot
                        }
                    }

                    val isFixed = item.name in listOf("Lagu", "Artis", "Album", "Beranda")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeightDp)
                            .zIndex(zIndex)
                            .graphicsLayer { this.translationY = translationY }
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (item.role == TabRole.HEADER) Color.Transparent
                                else if (item.role == TabRole.SUB) CardSlate.copy(alpha = 0.85f)
                                else CardSlate
                            )
                            .then(
                                if (item.role != TabRole.HEADER) {
                                    Modifier.pointerInput(Unit) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                draggedItemId = item.id
                                                dragOffset = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount.y
                                            },
                                            onDragEnd = {
                                                if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
                                                    val moved = allItems.removeAt(draggedIndex)
                                                    val headerIdx = allItems.indexOfFirst { it.role == TabRole.HEADER }
                                                    val finalTarget = targetIndex.coerceIn(0, allItems.size)

                                                    if (headerIdx != -1) {
                                                        if (finalTarget <= headerIdx) {
                                                            if (moved.role == TabRole.HIDDEN) {
                                                                allItems.add(finalTarget, moved.copy(role = TabRole.SUB))
                                                            } else {
                                                                allItems.add(finalTarget, moved)
                                                            }
                                                        } else {
                                                            allItems.add(finalTarget, moved.copy(role = TabRole.HIDDEN))
                                                        }
                                                    } else {
                                                        allItems.add(finalTarget, moved)
                                                    }
                                                    validateAndSaveFn()
                                                }
                                                draggedItemId = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggedItemId = null
                                                dragOffset = 0f
                                            }
                                        )
                                    }
                                } else Modifier
                            )
                    ) {
                        if (item.role == TabRole.HEADER) {
                            Text(
                                text = item.name,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DragHandle, contentDescription = "Geser", tint = TextSecondary)
                                Spacer(modifier = Modifier.width(10.dp))

                                if (item.role == TabRole.SUB) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        Icons.Default.SubdirectoryArrowRight,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                // Static icon display (no click listener)
                                Box(
                                    modifier = Modifier
                                        .size(if (item.role == TabRole.SUB) 28.dp else 32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E2421)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White,
                                        modifier = Modifier.size(if (item.role == TabRole.SUB) 16.dp else 18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.name,
                                        color = if (item.role == TabRole.HIDDEN) TextSecondary else Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (item.isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Custom",
                                                color = EmeraldGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                if (item.role != TabRole.HIDDEN) {
                                    if (isFixed) {
                                        // Fixed tab (Beranda, Lagu, Artis, Album) has Tetap badge, NO edit button
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFF1E2421)
                                        ) {
                                            Text(
                                                "Tetap",
                                                fontSize = 11.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else {
                                        // All non-fixed tabs (Favorit, Riwayat, Teratas, Terbaru, Playlist, and custom tabs):
                                        // Role Chip + Edit Button (Pencil Icon)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                onClick = {
                                                    val nextRole = if (item.role == TabRole.MAIN) TabRole.SUB else TabRole.MAIN
                                                    val idx = allItems.indexOf(item)
                                                    if (idx != -1) {
                                                        if (nextRole == TabRole.SUB && idx == 0) {
                                                            val nextMainIdx = allItems.indexOfFirst { it != item && it.role == TabRole.MAIN }
                                                            if (nextMainIdx != -1) {
                                                                val moved = allItems.removeAt(idx)
                                                                allItems.add(nextMainIdx, moved.copy(role = TabRole.SUB))
                                                                validateAndSaveFn()
                                                                return@Surface
                                                            }
                                                        }
                                                        allItems[idx] = item.copy(role = nextRole)
                                                        validateAndSaveFn()
                                                    }
                                                },
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (item.role == TabRole.SUB) EmeraldGreen.copy(alpha = 0.15f) else Color(0xFF1E2421),
                                                border = BorderStroke(1.dp, if (item.role == TabRole.SUB) EmeraldGreen.copy(alpha = 0.35f) else Color(0xFF2E3832))
                                            ) {
                                                Text(
                                                    text = item.role.label,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldGreen,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Edit Button (Pencil Icon)
                                            IconButton(
                                                onClick = { editingTabItem = item },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Edit Tab",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
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
}
