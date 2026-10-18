Store expected API response bodies here as JSON files grouped by operation.
Load a response with `StringUtils.readJson("responses/<operation>/<case>.json")`
and compare it using JsonUnit's `assertThatJson(actual).isEqualTo(expected)`.

Use `${json-unit.any-number}`, `${json-unit.any-string}`, and
`${json-unit.any-boolean}` for dynamic values. Assert deterministic fields exactly
and check relationships to persisted data separately.

Business endpoints have not yet been implemented, so there are no API response
fixtures to migrate. The loader's own fixtures live in `fixtures/`.
