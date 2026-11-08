# R8 rules for the release build. Most libraries ship their own consumer rules (Room, Hilt, Compose, AndroidX,
# kotlinx.serialization, Navigation 3), so this file only covers what they do not.

# Keep file names and line numbers in crash stack traces; the mapping file (app/build/outputs/mapping/release/)
# turns obfuscated traces back into readable ones. Upload it with each release to the Play Console.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
