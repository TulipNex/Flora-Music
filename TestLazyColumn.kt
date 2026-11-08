package com.example.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TestLazy() {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(top = 100.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(50) { index ->
            Text("Item $index")
        }
    }
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect { info ->
            info.visibleItemsInfo.forEach {
                println("Item ${it.index}: offset=${it.offset}")
            }
        }
    }
}
