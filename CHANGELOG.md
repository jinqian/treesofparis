# Changelog

All notable changes to Arbres de Paris. Generated from the commit history with [git-cliff](https://git-cliff.org);
see `android/RELEASING.md`. User-facing release notes, in French, are in `android/fastlane/metadata/android/fr-FR/changelogs/`.

## [0.2.0] - 2026-11-13

### Added

- **website**: Explain how to join the closed test
- **website**: Count visits with GoatCounter
- **website**: Explain why the app needs a closed test
- **map**: Tap « Zoomez pour voir les arbres » to zoom in
- **map**: Open a tree's location in the Maps app
- **map**: Open the route from the tree's address line
- **map**: Tap the address to open Maps; shorter address and placeholders
- **about**: Ask before sending crash reports

### Fixed

- **map**: Fit labels on small screens and large text
- **map**: Give every leaf card the same height
- **map**: Keep the system location buttons from lingering over other tabs
- **website**: Say which personal data « Vos droits » covers
- **map**: Keep the map alive across tab switches, no white flash
- **map**: Scope retained values to the activity, no Context key
- **website**: Warn testers that Play access can take a few hours
- **app**: Show the system location button in French
- **map**: Show « Arbre le plus proche » once location is shared

### Documentation

- **release**: Closed test release notes for v0.1.0

## [0.1.0] - 2026-11-12

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
- **data**: Link each species to its French Wikipedia article
- **map**: Open the tree's French Wikipedia article from its sheet
- **herbier**: Open a genus on the map from the Herbier tab
- **about**: Credit the flowering sources
- **map**: Show when the tree flowers on its sheet
- **herbier**: Split the Herbier into a home and four screens
- **herbier**: List every remarkable tree on the Remarquables screen
- **map**: Find the tree nearest to the user
- **map**: Filter the map by genus from an « Essences » sheet
- **website**: Add the arbresdeparis.fr landing page
- **about**: Add a contact section and error reports from the tree sheet
- **map**: Draw the map with MapLibre and OpenFreeMap vector tiles in the Herbier style
- **map**: Show trees from zoom 15.5, up to 3,000 at once
- **app**: Install the debug build next to the release build, with its own icon
- **website**: Add a privacy page and the not-affiliated notice
- **map**: Share precise location through the Android location button
- **website**: Add a tester sign-up through the Google Group
- **app**: Send crash reports with Firebase Crashlytics, with an off switch
- **website**: Web map of the trees at /maps/, linked from the home page
- Add the trees of Grand Paris Seine Ouest and tree groups on the zoomed-out map
- **map**: Place tree groups where the trees gather, stable while panning
- **map**: Share the location from « Arbre le plus proche » in one tap
- **designsystem**: Add the « Goutte » tree app logo
- **about**: Show the tree logo in the app card
- **app**: Replace the leaf launcher icon with the « Goutte » tree
- **website**: Use the « Goutte » tree logo and favicon

### Fixed

- **map**: Bigger density-aware tree pins and deeper zoom
- **data**: Find the user's position indoors
- **map**: Quieter leaf watermark, inline arrondissement and a short stage label
- **favorites**: Show the arrondissement before the address so it never truncates
- **map**: Same size for the tree and remarkable legend dots
- **domain**: Restore the accent of a lone « à » in dataset addresses
- **herbier**: Say « à Paris » in the headline and let the gold header reach the card edge
- **designsystem**: Open bottom sheets at the height of their content
- **domain**: Keep Roman numerals upper-case in place names
- **herbier**: Keep two-line names off the calendar dividers
- **data**: Keep favorites through backups, upgrades and data refreshes
- **website**: Use the launcher icon with its veins, keep the footer email in the row
- **ci**: Name the release artifact after the app version, not the git ref
- **app**: Load the website fonts in the feature graphic again

### Changed

- **domain**: Use dataset coverage area instead of city bounds
- **domain**: Share place name formatting between features
- **data**: Ship the favorite snapshot in user database version 1
- **map**: Remove the osmdroid map

### Documentation

- Add design spec and implementation plan
- **plan**: Revise plan after review
- Add agent setup, readme and adrs
- Keep trees managed outside paris in spec and plan
- **map**: Park the maplibre vector map plan
- Detail the next steps roadmap
- Changelog, French release note and releasing guide
- Mark roadmap tasks 3, 4 and 5 as done
- Drop the finished libraries and data extraction tasks from the roadmap
- Mark the Wikipedia links task as done
- Collect feature ideas for later
- Develop the other cities idea
- Credit OpenFreeMap, OpenMapTiles, OpenStreetMap and MapLibre in the app, README and website
- Plan trees beyond Paris and delete the outdated docs
- **website**: Mention the neighbouring towns wherever the map is described, new screenshots
- Update the Grand Paris plan after phase 1, survey the east, add quiz and photo ideas


