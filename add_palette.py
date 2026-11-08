import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

# Add version if not exists
if 'palette =' not in content:
    content = content.replace('[versions]\n', '[versions]\npalette = "1.0.0"\n')

# Add library if not exists
if 'androidx-palette =' not in content:
    content = content.replace('[libraries]\n', '[libraries]\nandroidx-palette = { group = "androidx.palette", name = "palette-ktx", version.ref = "palette" }\n')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)

with open('app/build.gradle.kts', 'r') as f:
    app_content = f.read()

if 'androidx.palette' not in app_content:
    app_content = app_content.replace('dependencies {\n', 'dependencies {\n    implementation(libs.androidx.palette)\n')
    with open('app/build.gradle.kts', 'w') as f:
        f.write(app_content)
