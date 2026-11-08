import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

content = content.replace(
    '[versions]\n',
    '[versions]\njaudiotagger = "3.0.1"\n'
)

content = content.replace(
    '[libraries]\n',
    '[libraries]\njaudiotagger = { group = "net.jthink", name = "jaudiotagger", version.ref = "jaudiotagger" }\n'
)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)

with open('app/build.gradle.kts', 'r') as f:
    build_content = f.read()

build_content = build_content.replace(
    'implementation(libs.androidx.room.runtime)',
    'implementation(libs.androidx.room.runtime)\n  implementation(libs.jaudiotagger)'
)

with open('app/build.gradle.kts', 'w') as f:
    f.write(build_content)

