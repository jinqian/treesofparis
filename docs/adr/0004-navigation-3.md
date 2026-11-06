# 4. Jetpack Navigation 3

Status: accepted (2026-11-06)

## Context
The spec asks for Jetpack Compose navigation. Jetpack now offers Navigation 3 (stable, Compose-first,
the back stack is app-owned state) next to Navigation 2 (`navigation-compose`). The vendored android/skills
`navigation-3` skill targets Navigation 3.

## Decision
Use Navigation 3 in `:app`, with `@Serializable` `NavKey` routes and one back stack per bottom-bar tab.
Feature modules do not depend on any navigation library: they expose `XxxRoute` composables and callbacks.

## Consequences
- Switching navigation libraries only touches `:app`.
- ViewModels are scoped to back-stack entries with `lifecycle-viewmodel-navigation3`.
