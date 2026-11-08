package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.presentation.FloraMainScreen
import com.example.presentation.MusicViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        // Resolve central MVVM Viewmodel utilizing robust static Application Factory
        viewModel = ViewModelProvider(this, MusicViewModel.Factory)[MusicViewModel::class.java]
        
        handleIntent(intent)
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    FloraMainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH || 
            intent?.action == android.content.Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(android.app.SearchManager.QUERY) ?: ""
            val focus = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_FOCUS)
            val title = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_TITLE)
            val artist = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_ARTIST)
            val album = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_ALBUM)
            
            viewModel.playFromSearch(query, focus, title, artist, album)
        }
    }

    override fun onStart() {
        super.onStart()
        // Check permissions before running background sync on start
        val permissionString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(this, permissionString) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (hasPermission && ::viewModel.isInitialized) {
            viewModel.scanLocalMedia(silent = true)
        }
    }
}
