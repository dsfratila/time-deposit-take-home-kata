# Time deposit system

## Scope and compatibility

- This directory holds the Java implementation. Read `../README.md` before implementing behavior; it defines the assignment, interest rules, and submission constraints. The requirements below specify the target implementation stack and engineering conventions.
- Expose exactly two business REST endpoints: retrieve all time deposits with withdrawals, and update all time deposit balances. Provide an OpenAPI/Swagger contract and invocation instructions.
- Preserve the public API of `org.ikigaidigital.TimeDeposit`, including its existing constructor, ID type, getters, and balance setter. Preserve `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` and its existing calculation behavior.
- Keep persistence entities and API DTOs separate from the shared class where needed to preserve compatibility. Preserve the existing model structure and its `int` ID type.
- Preserve the existing interest eligibility boundaries and rounding behavior. Use characterization tests before refactoring calculations.

## Target stack and design

- Use Java 25, Maven, Spring Boot 4, Spring Data JPA, and the compatible Hibernate Types/Hypersistence library.
- Verify dependency compatibility with Spring Boot 4 and its Hibernate version before choosing artifacts.
- Follow hexagonal architecture: keep interest calculation independent of HTTP and persistence, put orchestration in application services, and implement external concerns in adapters.
- Apply SOLID principles and clean code practices. Make interest rules extensible through focused policies or strategies without adding speculative abstractions.
- Prefer clear names and small methods over comments. Add comments only to explain non-obvious intent, constraints, or required assumptions; avoid comments that restate the code.
- Follow `checkstyle/openjdk_checks.xml` for production and test code. Use these rules as the source of truth; preserve them when fixing style violations. Run Maven with Java 25 (`JAVA_HOME` pointing to the JDK); the configured Checkstyle engine cannot run on Java 17.
- Include request tracing and correlated logs with trace/span identifiers propagated through the request lifecycle.
- Use decimal arithmetic for persisted monetary values and new monetary calculations, while preserving the shared API and established calculation behavior.

## Java structure and DDD boundaries

Use the following package structure beneath the project's base package (`org.ikigaidigital`):

```text
project
├── api
├── business
├── domain
└── infra
    └── repo
```

- `api`: REST controllers, API DTOs, and HTTP mapping to application use cases.
- `services`: application services that orchestrate use cases, transactions, and repository access.
- `domain`: aggregates, value objects, interest policies, and repository contracts; keep this package independent of Spring, JPA, and HTTP.
- `infra`: external adapters and configuration that wires implementations to contracts. Put Spring Data repositories, JPA entities, and persistence mapping in `infra.repo`.
- Keep `TimeDeposit` and `TimeDepositCalculator` in `org.ikigaidigital`, outside these subpackages. Map `TimeDeposit` directly as a JPA entity while preserving its existing public API and calculation behavior; keep its Spring Data repository and persistence converters in `infra.repo`. Keep `TimeDepositCalculator` unchanged. This shared entity is an exception to the rule that JPA entities belong inside the persistence adapter.

- Use domain language consistently in packages, types, and methods. Model the time deposit as an aggregate root that controls its balance and withdrawal invariants; treat withdrawals as part of that aggregate unless requirements justify a separate boundary.
- Put business behavior in aggregate methods, value objects, and interest policies. Use domain services only for domain behavior that does not naturally belong to one object. Keep application services focused on orchestration, transactions, and repository access.
- Keep domain code independent of Spring, JPA, HTTP DTOs, and infrastructure. Application code depends on the domain and outbound interfaces; infrastructure implements those interfaces and maps external representations. Wire implementations in configuration.
- Define repositories around aggregates and domain operations, including the locking semantics required by balance updates. Keep Spring Data interfaces and JPA entities inside the persistence adapter; expose domain types through repository contracts.
- Use immutable value objects for concepts such as money where they clarify invariants while preserving the existing shared `TimeDeposit` contract.
- Keep controllers thin: map the OpenAPI contract to application use cases and map results back to HTTP responses. Keep persistence mapping in its adapter rather than sharing JPA entities with controllers.
- Introduce domain events, additional bounded contexts, or additional ports only when a concrete requirement needs them. Keep context internals private where practical and communicate across contexts through explicit application interfaces.

## Persistence and concurrency

- Use database-generated auto-incrementing integer primary keys compatible with the model's `int` ID. Configure JPA with `@GeneratedValue(strategy = GenerationType.IDENTITY)` and define the corresponding auto-increment columns in Liquibase.
- Use Liquibase as the authority for schema creation and versioning; use JPA schema validation instead of automatic schema creation or updates.
- Write all Liquibase changelogs and changesets in XML.
- Treat committed changesets as immutable. Introduce every subsequent schema or data migration in a new versioned changeset.
- Give every changeset explicit preconditions appropriate to the affected objects or data so applying migrations to an existing database is safe. Liquibase tracks applied changesets; do not use `runAlways` or `runOnChange` as a substitute for versioning.
- Configure changeset preconditions with `onFail="MARK_RAN"`: a failed precondition means the intended change is already applied, so Liquibase records the changeset as executed without running it. Write preconditions so failure identifies that existing state. Use `onError="HALT"` for errors while evaluating preconditions.
- Execute account balance changes within a transaction using pessimistic write locking. Acquire the lock before reading the balance used for calculation and retain it until commit or rollback.
- For updates involving multiple accounts, acquire locks in a deterministic order to reduce deadlocks.

## Tests

- Follow contract-first TDD for every new or changed REST API behavior: first create or update the OpenAPI definition with the operation, request/response schemas, and status codes; then write an integration test against that contract before implementing the API behavior.
- Run the integration test and confirm it fails because the requested behavior is missing (Red), implement the smallest change that makes it pass (Green), then refactor while keeping tests passing. Compilation errors or infrastructure failures do not count as the expected Red result. Repeat this cycle for each additional behavior, keeping the contract, tests, and implementation aligned.
- Cover every REST endpoint with at least one integration test, including persisted effects for balance updates.
- Use REST Assured to send HTTP requests in API integration tests and JsonUnit's AssertJ integration to assert JSON response bodies.
- Create a reusable abstract integration test base annotated with `@SpringBootTest`; all integration tests extend it.
- Run integration tests against a real database container using Testcontainers and Spring Boot integration (`@ServiceConnection`). Use the same database engine as the application and apply Liquibase migrations in tests.
- Revert database changes made by tests. Use transaction rollback where the application work participates in the test transaction; for real HTTP requests or independently committed work, explicitly clean up committed data. Tests must run independently and without order dependencies.
- Structure unit tests as Arrange–Act–Assert. Use Mockito for mocks and AssertJ for assertions.
- Use JUnit parameterized tests for algorithms with multiple input/output cases, such as `TimeDepositCalculator` interest calculations. Include boundary cases and descriptive case names; use `@MethodSource` for complex fixtures. Express Arrange–Act–Assert through test structure without unnecessary section comments.
- Extract reusable fixtures into Object Mother classes, such as `TimeDepositMother`, with named factories for meaningful deposit states. Return fresh instances to avoid shared mutable test data.
- Cover interest boundaries, monetary rounding, and new policies with unit tests. Verify pessimistic locking with concurrent database transactions when implementing balance persistence.

## Workflow and verification

- Keep changes focused and respect existing user edits. Follow the repository's restrictions on branches and pull requests from `../README.md`.
- Run relevant Maven tests for each change and the full verification lifecycle (`mvn verify`, or `./mvnw verify` when a wrapper exists) before declaring implementation complete. Integration tests require a working container runtime; report any checks that could not run.
- After completing each task, run the Maven Checkstyle check (`mvn checkstyle:check`, or `./mvnw checkstyle:check` when a wrapper exists), fix reported style violations, and rerun it until it passes. Use the repository's configured Maven formatter/fix goal if one exists; otherwise fix violations manually, since Checkstyle itself does not automatically fix code. If Checkstyle configuration is missing, report that limitation instead of claiming compliance with unconfigured rules.
- Keep setup and run instructions current, including Java 25, database/container prerequisites, migrations, and API usage.
- Document the reproducible AI tooling setup and a brief account of AI-assisted work as required by the assignment. This file supplies the repository-specific agent rules.
