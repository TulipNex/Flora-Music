import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

content = content.replace(
    '<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />',
    '<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />\n    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />\n    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />'
)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
