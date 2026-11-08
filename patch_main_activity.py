with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """        // Resolve central MVVM Viewmodel utilizing robust static Application Factory
        viewModel = ViewModelProvider(this, MusicViewModel.Factory)[MusicViewModel::class.java]"""

replacement = """        // Resolve central MVVM Viewmodel utilizing robust static Application Factory
        viewModel = ViewModelProvider(this, MusicViewModel.Factory)[MusicViewModel::class.java]
        
        handleIntent(intent)"""

if target in content:
    content = content.replace(target, replacement)

target2 = """    override fun onStart() {"""
replacement2 = """    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH) {
            val query = intent.getStringExtra(android.app.SearchManager.QUERY) ?: ""
            val focus = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_FOCUS)
            val title = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_TITLE)
            val artist = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_ARTIST)
            val album = intent.getStringExtra(android.provider.MediaStore.EXTRA_MEDIA_ALBUM)
            
            viewModel.playFromSearch(query, focus, title, artist, album)
        }
    }

    override fun onStart() {"""

if target2 in content:
    content = content.replace(target2, replacement2)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
    print("Patched MainActivity.kt")
else:
    print("Target2 not found")
