# 3. Clean architecture layers and models

Status: accepted (2026-11-06)

## Context
The project is a learning ground for a maintainable, testable Android architecture with fast builds.

## Decision
- Layers: UI (Compose screen + scoped `@HiltViewModel`) → use cases → repository interfaces (`:core:domain`,
  pure JVM) ← repository implementations (`:core:data`).
- A model per layer: Room entities, domain models, UI models, with mappers at each boundary.
- Tree data ships pre-packaged (`trees.db` asset, built by `data/scripts/update_trees.py` from Room's
  exported schema). Favorites live in a separate `UserDatabase`, so a data refresh never erases them.
- `UserDatabase` is user data (amended 2026-11-09): every schema change ships a migration, never a destructive
  fallback (`UserDatabaseMigrationTest` upgrades each exported schema); TRUNCATE journal mode so Android's backup of
  `user.db` holds every change; each favorite keeps a snapshot of its tree, so a tree removed by a data refresh still
  shows in Favoris as « Retiré des données de la Ville ».
- Data refresh: the script stamps a data version; a higher version makes Room re-copy the asset on existing
  installs.

## Consequences
- Domain logic is tested with plain JVM tests; ViewModels with fake repositories; Room with Robolectric.
- Feature modules compile in parallel and do not recompile when `:core:data` changes.
