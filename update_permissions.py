import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

if 'MANAGE_EXTERNAL_STORAGE' not in content:
    content = content.replace(
        '<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />',
        '<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />\n    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage" />'
    )
    content = content.replace('<manifest xmlns:android="http://schemas.android.com/apk/res/android"', '<manifest xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:tools="http://schemas.android.com/tools"')
    
    with open('app/src/main/AndroidManifest.xml', 'w') as f:
        f.write(content)
