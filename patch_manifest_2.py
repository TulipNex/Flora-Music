with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """            <intent-filter>
                <action android:name="android.media.action.MEDIA_PLAY_FROM_SEARCH" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>"""
replacement = """            <intent-filter>
                <action android:name="android.media.action.MEDIA_PLAY_FROM_SEARCH" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEARCH" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Patched manifest")
