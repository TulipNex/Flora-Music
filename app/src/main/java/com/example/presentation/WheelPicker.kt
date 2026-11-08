package com.example.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    range: IntRange,
    initialValue: Int,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val items = range.toList()
    val itemHeight = 48.dp
    val realCount = items.size
    
    // Start at a large index to simulate infinite scrolling
    val middle = Int.MAX_VALUE / 2
    val initialIndex = items.indexOf(initialValue).coerceAtLeast(0)
    // Offset by 1 because 3 items are visible, so the selected one is in the middle (index + 1)
    val startIndex = middle - (middle % realCount) + initialIndex - 1
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            val centerIndex = (index + 1) % realCount
            if (centerIndex >= 0 && centerIndex < realCount) {
                onValueChange(items[centerIndex])
            }
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = snapBehavior,
        modifier = modifier
            .height(itemHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(Int.MAX_VALUE) { index ->
            val isSelected by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 == index } }
            val realIndex = index % realCount
            Box(
                modifier = Modifier
                    .height(itemHeight)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = items[realIndex].toString().padStart(2, '0'),
                    fontSize = if (isSelected) 36.sp else 28.sp,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}
