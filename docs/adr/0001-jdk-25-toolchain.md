# 1. JDK 25 Gradle toolchain

Status: accepted (2026-11-06)

## Context
The developer machine defaults to JDK 25. Tools that run inside the build (Kotlin, AGP, KSP/Hilt, Detekt,
ktlint, Robolectric) often lag behind new JDKs.

## Decision
Compile with a JDK 25 toolchain everywhere (local and CI). Emit bytecode for Java 17 in Android modules and
Java 21 in JVM modules. Verified in the skeleton build: AGP 9.5, Kotlin 2.4, KSP, Hilt, Compose, Detekt 2
(alpha), Spotless/ktlint and Robolectric 4.17 all work.

## Consequences
- Robolectric needs `--add-exports java.base/jdk.internal.access=ALL-UNNAMED` (and a few `--add-opens`),
  set in the convention plugins.
- Robolectric 4.17 has no SDK 37 runtime, so `config/robolectric/robolectric.properties` pins `sdk=36`.
- Detekt 2 is still alpha; its config keys may change before 2.0 final.
- If a tool breaks on a future JDK update, fall back to JDK 21 for that tool only and record it here.
