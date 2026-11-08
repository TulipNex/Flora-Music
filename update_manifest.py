with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """            <intent-filter>
                <action android:name="android.media.browse.MediaBrowserService" />
            </intent-filter>"""
replacement = """            <intent-filter>
                <action android:name="androidx.media3.session.MediaLibraryService" />
                <action android:name="android.media.browse.MediaBrowserService" />
            </intent-filter>"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Updated manifest")
else:
    print("Manifest target not found")
