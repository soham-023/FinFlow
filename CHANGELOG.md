# Changelog

All notable changes to FinFlow are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- `GET /actuator/health` public health endpoint; the Docker image uses it as its `HEALTHCHECK`.
- Machine-readable `code` field on every error response (e.g. `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`).
- `GET /api/transactions` can filter by `type` and `category` at the same time.
- GitHub Actions CI running `mvn -B verify` on pushes and pull requests to `main`.
- Tests for JwtService, the JWT filter's public paths, the security filter chain, BudgetController,
  AnalyticsController, request validation, mappers and the YearMonth converter.

### Fixed
- Accounts registered before email normalization (stored as e.g. `Alex@Example.com`) could no longer
  log in. A startup data fix (`EmailCaseMigration`) rewrites their emails to trimmed lower case; accounts
  that would collide with an existing lower-case address are left unchanged and logged for manual review.

### Changed
- Reading, updating or deleting another user's transaction or budget now returns `404 RESOURCE_NOT_FOUND`
  (same as a missing id) instead of `403`, so ids belonging to other users cannot be probed.
- Missing, invalid or expired JWTs now get a JSON `401 UNAUTHENTICATED` instead of an empty `403`.
- Failed logins return `401` instead of `500`.
- Emails are trimmed and lower-cased on registration and login, so addresses differing only in case map to one account.
- Categories are trimmed and upper-cased independently of the server locale.
- Service read methods run in read-only transactions.
- The Docker image runs as a non-root user.
- Public endpoints are defined once and shared by the security config and the JWT filter.

### Fixed
- Creating a second budget for the same category and month returns `409` instead of a silent duplicate.
- Malformed JSON bodies and unknown enum values return `400` instead of `500`.
- Unknown routes, unsupported HTTP methods and non-JSON bodies return `404`, `405` and `415` instead of `500`.
- Out-of-range `page`/`size` and unknown `sortBy`/`sortDir` values return `400` instead of `500`.
- Malformed `month` query parameters return `400` instead of `500`.
- Amounts with more than 2 decimals or 17 integer digits, over-long strings, and passwords over BCrypt's
  72-byte limit are rejected with `400` instead of failing in the database or being silently truncated.
- Unexpected `500` errors no longer leak exception messages to clients; they are logged server-side.
