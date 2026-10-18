Store expected API response bodies here as JSON files grouped by operation.
Load a response with `StringUtils.readJson("responses/<operation>/<case>.json")`
and compare it using JsonUnit's `assertThatJson(actual).isEqualTo(expected)`.

Use typed placeholders such as `${json-unit.any-number}` only when exact identity
is irrelevant. Assert deterministic fields exactly; use fixture IDs or substitute
captured generated IDs when identity and relationships matter. Verify required
persisted effects through `DatabaseHelper` separately from response comparisons.

`list-time-deposits/` contains the populated and empty GET responses.
`accrue-interest/` contains GET responses after one and two accrual requests,
with deterministic IDs from the reset database. The POST response is HTTP 204
without a body, so its HTTP test asserts an empty body directly. See
`ListTimeDepositsIntegrationTest` and `AccrueInterestIntegrationTest` for usage.
The loader's own fixtures live in `fixtures/`.
