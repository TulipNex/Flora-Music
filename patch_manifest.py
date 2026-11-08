with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

target = """            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>"""

replacement = """            android:exported="true"
            android:launchMode="singleTask"
            android:label="@string/app_name"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.media.action.MEDIA_PLAY_FROM_SEARCH" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
            <intent-filter>"""

if target in content:
    content = content.replace(target, replacement)
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
    print("Patched AndroidManifest.xml")
else:
    print("Target not found")
