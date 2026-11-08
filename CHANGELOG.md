# Changelog

All notable changes to Arbres de Paris. Generated from the commit history with [git-cliff](https://git-cliff.org);
see `RELEASING.md`. User-facing release notes, in French, are in `fastlane/metadata/android/fr-FR/changelogs/`.

## [Unreleased]

### Added

- **domain**: Add models, repositories and use cases
- **data**: Add room databases and repositories
- **data**: Add tree dataset update script
- **designsystem**: Add theme and reusable components
- **map**: Add provider-neutral map contract and osmdroid implementation
- **map**: Add map screen with tree details
- **favorites,about**: Add favorites and about screens
- **app**: Wire hilt, navigation and bottom bar
- **map**: Make the locate button centre on the user or explain why not
- **map**: Add a remarkable trees filter chip with gold pins
- **designsystem**: Herbier palette, serif typography and line icons
- **map**: Herbier map overlays, ringed pins and herbarium tree sheet
- **favorites**: Herbier list with leaf badges and an explore-map empty state
- **about**: Herbier cards with an app card and font credits
- **app**: Paper bottom bar with a sage indicator
- **map**: Replace the brand pill and filter chip with all/remarkable segments
- **app**: Moss leaf app icon, themed icon and herbier splash screen
- **app**: Name the app « Arbres de Paris » with a serif word mark on the splash screen
- **designsystem**: Fine-line leaf drawings for the 21 most common genera
- **map**: Show the tree's genus leaf as the detail sheet watermark
- **domain**: Tree statistics model and use case for the Herbier screen
- **data**: Aggregate queries for tree statistics
- **herbier**: Statistics tab with genus leaves, stages, remarkable records and wooded arrondissements
- **app**: Add the Herbier tab between Carte and Favoris
- **about**: Move the open source libraries to their own screen
- **data**: Record and show which dataset extraction the app ships

### Fixed

- **map**: Bigger density-aware tree pins and deeper zoom
- **data**: Find the user's position indoors
- **map**: Quieter leaf watermark, inline arrondissement and a short stage label
- **favorites**: Show the arrondissement before the address so it never truncates
- **map**: Same size for the tree and remarkable legend dots
- **domain**: Restore the accent of a lone « à » in dataset addresses
- **herbier**: Say « à Paris » in the headline and let the gold header reach the card edge

### Changed

- **domain**: Use dataset coverage area instead of city bounds
- **domain**: Share place name formatting between features

### Documentation

- Add design spec and implementation plan
- **plan**: Revise plan after review
- Add agent setup, readme and adrs
- Keep trees managed outside paris in spec and plan
- **map**: Park the maplibre vector map plan
- Detail the next steps roadmap


