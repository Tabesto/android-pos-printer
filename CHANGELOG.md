# Changelog



## [0.4.4] - 2021-02-11

### Fixed

- Do not block main job before to send callback
- Do not send Timber logs in double in caller apps



## [0.4.3] - 2021-02-08
### Added
- Send callback when main job is already running



## [0.4.2] - 2021-02-05
### Fixed
- Enforce using singleton instance for DeviceManager



## [0.4.1] - 2021-02-04
### Fixed
- Change Coroutine Dispatcher to avoid blocking UI



## [0.4.0] - 2021-02-03
### Changed
- Multi printer support (instead of unique printer)



## [0.3.0] - 2020-12-04
### Added
- Custom & complete logger
- Unit tests
- Dagger di



## [0.3.0.alpha.2] - 2020-10-29
### Added
- Printer status info on demand



## [0.3.0.alpha.1] - 2020-10-28
### Added
- Printer status info after print success



## [0.2.0.alpha.1] - 2020-10-28
### Changed
- Split printer listener to allow usage on multiple location in caller apps
- Init region & model without specific method



## [0.1.0.alpha.3] - 2020-10-27
### Removed
- Auto retry with discovery on ERR_CONNECT

### Changed
- Clean some code



## [0.1.0.alpha.2] - 2020-10-23
### Fixed
- Exception & TicketData builder

### Changed
- Clean some code



## [0.1.0.alpha.1] - 2020-10-22
### Added
- Connection & Disconnection handling
- Customizable ticket printing
- Error & Retries handling



## Guidelines

To contribute to this CHANGELOG, please follow the [Markdown Syntax](https://www.markdownguide.org/basic-syntax/) and the [Keep-a-Changelog Guidelines](https://keepachangelog.com/en/1.0.0/).