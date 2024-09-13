# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

<!--
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Other Notes & Contributions
-->

### Fixed

- Fixed explicit parent components with @MergeComponent components - [#52](https://github.com/r0adkll/kimchi/pull/52)
- Fixed non-primitive custom keys not generating correctly - [#55](https://github.com/r0adkll/kimchi/pull/55)

## [0.3.1] - 2024-09-10

### Fixed

- Fixed naive `toClassName()` function that broke on nested classes - [#48](https://github.com/r0adkll/kimchi/pull/48)

## [0.3.0] - 2024-09-09

### Fixed

- Fixed issue where @ContributesBinding elements with constructor injections and no @Inject annotation
- Fixed annotations passed to merged implementations clashing with kotlin-inject generation. - [#43](https://github.com/r0adkll/kimchi/pull/43)
- Fixed root component creation extension function to account for defined companion objects. - [#39](https://github.com/r0adkll/kimchi/pull/39)

## [0.2.0] - 2024-08-24

### Added

- [Circuit] Added ability to inject additional elements into Ui composable functions - [#30](https://github.com/r0adkll/kimchi/pull/30)

### Fixed

- Fixed `rank` ordering when contributing multiple bindings of the same type - [#34](https://github.com/r0adkll/kimchi/pull/34)

## [0.1.1] - 2024-08-24

### Added

- Added testFixtures in the `compiler-utils` module for writing Kimchi unit tests.

### Fixed

- Fixed kinject scopes not transferring to the underlying merged components.


## [0.1.0] - 2024-08-14

### Added

- Migrated implementation from Deckbox and Campfire
- Initial Release
