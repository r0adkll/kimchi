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

## [0.5.1] - 2025-01-05

### Fixed

- Fixed Circuit presenter code generator - [#98](https://github.com/r0adkll/kimchi/pull/98)

## [0.5.0] - 2024-12-19

### Added

- Add support for linuxArm64/x64 to annotations from [@bwalter089](https://github.com/bwalter089) - [#91](https://github.com/r0adkll/kimchi/pull/91)
- Provide contributed subcomponent factories on its parents graph - [#94](https://github.com/r0adkll/kimchi/pull/94)

### Fixed

- Updated Danger check to be more sensitive about [CHANGELOG.md] changes - [#92](https://github.com/r0adkll/kimchi/pull/92)

### Other Notes & Contributions

- Special thanks to [@bwalter089](https://github.com/bwalter089) for contributing to this release

## [0.4.0] - 2024-09-14

### Added

- Support for multiple uses of `@ContributesBinding`, `@ContributesMultibinding`, and `@ContributesTo` on single elements - [#59](https://github.com/r0adkll/kimchi/pull/59)

### Fixed

- Fixed explicit parent components with @MergeComponent components - [#52](https://github.com/r0adkll/kimchi/pull/52)
- Fixed non-primitive custom keys not generating correctly - [#55](https://github.com/r0adkll/kimchi/pull/55)
- Fixed name collisions with generated hint classes - [#56](https://github.com/r0adkll/kimchi/pull/56)

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
