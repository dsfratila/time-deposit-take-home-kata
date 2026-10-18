# Time deposit system

## Scope and compatibility

- Preserve the constructor, ID type, getters, and balance setter of `org.ikigaidigital.core.domain.TimeDeposit`. Preserve `TimeDepositCalculator.updateBalance(List<TimeDeposit>)` and its existing calculation behavior.
- Keep persistence entities and API DTOs separate from the shared class where needed to preserve compatibility. Preserve the existing model structure and its `int` ID type.
- Preserve the existing interest eligibility boundaries and rounding behavior. Use characterization tests before refactoring calculations.

## Target stack and design

- Use Java 25, Maven, Spring Boot 4, Spring Data JPA, and the compatible Hibernate Types/Hypersistence library.
- Verify dependency compatibility with Spring Boot 4 and its Hibernate version before choosing artifacts.
- Apply SOLID principles and clean code practices. Make interest rules extensible through focused policies or strategies without adding speculative abstractions.
- Prefer clear names and small methods over comments. Add comments only to explain non-obvious intent, constraints, or required assumptions; avoid comments that restate the code.
- Follow `checkstyle/openjdk_checks.xml` for production and test code. Use these rules as the source of truth; preserve them when fixing style violations. Run Maven with Java 25 (`JAVA_HOME` pointing to the JDK); 
- Include request tracing and correlated logs with trace/span identifiers propagated through the request lifecycle.
- Use decimal arithmetic for persisted monetary values and new monetary calculations, while preserving the shared API and established calculation behavior.

## Java structure and DDD boundaries

The current production packages beneath `org.ikigaidigital` are:

```text
org.ikigaidigital
├── core
│   ├── business
│   │   └── TimeDepositCalculator
│   └── domain
│       ├── TimeDeposit
│       ├── TimeDepositBalanceConverter
│       └── TimeDepositPlan
└── infra
    ├── TimeDepositApplication
    ├── inbound
    │   └── web
    └── outbound
        └── repo
            └── TimeDepositRepository
```

- `core.business` contains `TimeDepositCalculator`. Put application services for use-case orchestration, transactions, and repository access here as those use cases are implemented.
- `core.domain` contains the `TimeDeposit` aggregate, `TimeDepositPlan` interest rules, and `TimeDepositBalanceConverter`. The aggregate is mapped directly as a JPA entity, and its converter implements JPA's `AttributeConverter`. Keep calculation logic independent of HTTP and Spring.
- `infra.TimeDepositApplication` owns Spring Boot startup and component/entity scanning.
- `infra.inbound.web` currently contains package documentation only. Put future REST controllers and operation-specific request/response DTOs here. Controllers call application services and return DTOs, including DTOs for nested objects and collections.
- `infra.outbound.repo.TimeDepositRepository` is the existing Spring Data `JpaRepository<TimeDeposit, Integer>`. Keep Spring Data repositories in this package.

### Dependencies and responsibilities

- `TimeDepositCalculator` depends on `TimeDeposit` and delegates interest accrual to the aggregate. Keep `core.domain` independent of `core.business` and HTTP code.
- Keep controllers focused on HTTP mapping and application services focused on orchestration. Keep business rules in aggregates, value objects, and interest policies.
- Use the existing Spring Data repository for persistence access. Add repository operations and locking queries when concrete use cases require them; keep transaction boundaries in application services.
- Introduce additional interfaces or wrapper classes only when a concrete requirement justifies them. Keep the package tree and class references in this file aligned with the source tree when reorganizing code.

### Shared classes and business behavior

- Keep `TimeDeposit` and `TimeDepositPlan` in `org.ikigaidigital.core.domain` and `TimeDepositCalculator` in `org.ikigaidigital.core.business`. Preserve calculation behavior when reorganizing packages. Put their tests in the matching packages under `src/test/java`.
- Use domain language consistently in packages, types, and methods. Model the time deposit as an aggregate root that controls its balance and withdrawal invariants; treat withdrawals as part of that aggregate unless requirements justify a separate boundary.
- Put business behavior in aggregate methods, value objects, and interest policies. Use domain services only for behavior that does not naturally belong to one object. Use immutable value objects for concepts such as money where they clarify invariants while preserving the shared `TimeDeposit` contract.
- Introduce domain events or additional bounded contexts only when a concrete requirement needs them. Keep context internals private where practical and communicate across contexts through explicit application interfaces.

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
- Store expected JSON response bodies in dedicated files under `src/test/resources/responses`, organized by API operation. Load them through the test helper `org.ikigaidigital.test.util.StringUtils.readJson(String)`, which reads a classpath JSON resource as a UTF-8 string.
- Assert dynamic JSON properties with JsonUnit's typed any-value placeholders, such as `${json-unit.any-number}`, `${json-unit.any-string}`, or `${json-unit.any-boolean}`, in the expected response files. Keep deterministic values explicit and verify relationships such as returned IDs matching persisted records separately.
- Create a reusable abstract integration test base annotated with `@SpringBootTest`; all integration tests extend it.
- Run integration tests against a real database container using Testcontainers and Spring Boot integration (`@ServiceConnection`). Use the same database engine as the application and apply Liquibase migrations in tests.
- Revert database changes made by tests. Use transaction rollback where the application work participates in the test transaction; for real HTTP requests or independently committed work, explicitly clean up committed data. Tests must run independently and without order dependencies.
- Structure unit tests as Arrange–Act–Assert. Use Mockito for mocks and AssertJ for assertions.
- Use JUnit parameterized tests for algorithms with multiple input/output cases, such as `TimeDepositCalculator` interest calculations. Include boundary cases and descriptive case names; use `@MethodSource` for complex fixtures. Express Arrange–Act–Assert through test structure without unnecessary section comments.
- Extract reusable fixtures into Object Mother classes, such as `TimeDepositMother`, with named factories for meaningful deposit states. Return fresh instances to avoid shared mutable test data.
- Cover interest boundaries, monetary rounding, and new policies with unit tests. Verify pessimistic locking with concurrent database transactions when implementing balance persistence.

## Workflow and verification

- Run relevant Maven tests for each change and the full verification lifecycle (`mvn verify`, or `./mvnw verify` when a wrapper exists) before declaring implementation complete. Integration tests require a working container runtime; report any checks that could not run.
- After completing each task, run the Maven Checkstyle check (`mvn checkstyle:check`, or `./mvnw checkstyle:check` when a wrapper exists), fix reported style violations, and rerun it until it passes. Use the repository's configured Maven formatter/fix goal if one exists; otherwise fix violations manually, since Checkstyle itself does not automatically fix code. If Checkstyle configuration is missing, report that limitation instead of claiming compliance with unconfigured rules.
- Keep setup and run instructions current, including Java 25, database/container prerequisites, migrations, and API usage.
- Document the reproducible AI tooling setup and a brief account of AI-assisted work as required by the assignment. This file supplies the repository-specific agent rules.
