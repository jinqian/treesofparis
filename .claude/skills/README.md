# Agent skills

Skills in this folder are vendored from [android/skills](https://github.com/android/skills)
(commit `bf68e2d`, Apache License 2.0, see `LICENSE-android-skills.txt`). They are plain
`SKILL.md` files that Claude Code (and other agents following the agent-skills standard) load
on demand.

| Skill | Why it is here |
|-------|----------------|
| `navigation-3` | App navigation uses Jetpack Navigation 3 |
| `testing-setup` | Unit, Robolectric and Compose test infrastructure |
| `jetpack-compose-performance` | Map screen recomposition and list performance |
| `edge-to-edge` | Edge-to-edge insets with the bottom bar and the map |
| `android-permissions-security` | Location permission and manifest review |
| `android-cli` | Driving the emulator, installing builds and taking screenshots |
| `agp-9-upgrade` | AGP 9 DSL reference for `build-logic` |

## Updating

Re-copy the folders from a fresh clone of android/skills and update the commit above, or use the
Android CLI: `android skills update --all --project=.`. Other skills can be added the same way
(`android skills add <name> --project=.`).
