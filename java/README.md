# Java implementation

Requires JDK 25, Maven, and Docker with a running container runtime (for example,
Docker Desktop). Set `JAVA_HOME` to your JDK 25 installation and add
`$JAVA_HOME/bin` to `PATH`. Configure your IDE's project SDK and Maven runner to
use JDK 25 as well.

On macOS, select an installed JDK 25 with:

```sh
export JAVA_HOME=$(/usr/libexec/java_home -v 25)
export PATH="$JAVA_HOME/bin:$PATH"
```

Confirm `mvn -version` reports Java 25, then build and run tests:

```sh
mvn clean verify
```

Integration tests start PostgreSQL 18.3 using Testcontainers and apply the same
Liquibase migrations as the application. Maven runs unit tests with Surefire and
`*IntegrationTest` classes with Failsafe during `verify`. Database writes in these
tests are rolled back or explicitly cleaned up after real HTTP requests.
The shared integration test base uses `DatabaseHelper` to reset application tables
and identity sequences before each test and clean up data afterward. Bulk test
data is loaded from classpath SQL fixtures through the same helper; schema and
Liquibase history are retained.

## Start the application

Start the local PostgreSQL database, then run Spring Boot:

```sh
docker compose up -d --wait
mvn spring-boot:run
```

The application listens on `http://localhost:8080`. Check startup with
`curl http://localhost:8080/actuator/health`; the response should contain
`"status":"UP"`. Swagger UI is available at
`http://localhost:8080/swagger-ui/index.html`, using the contract at
`http://localhost:8080/openapi.yaml`.

Retrieve all time deposits with:

```sh
curl http://localhost:8080/time-deposits
```

The endpoint returns HTTP 200 and a JSON array ordered by ascending deposit ID,
or `[]` when the database is empty. Each item contains `id`, `planType`, numeric
`balance`, integer `days`, and `withdrawals`. Withdrawals contain `id`, numeric
`amount`, and a `YYYY-MM-DD` date, ordered by ascending withdrawal ID; deposits
without withdrawals return an empty array. Plan names are returned as stored.
This operation does not accrue interest or change balances, and has no pagination
or filtering. The authoritative OpenAPI contract is
[`openapi.yaml`](openapi.yaml).

Apply monthly interest to all time deposits with:

```sh
curl -i -X POST http://localhost:8080/time-deposits/interest-accruals
```

This endpoint accepts no request body and returns HTTP 204 with no response body
after all balance changes commit, including when no deposits exist. It applies
the existing plan eligibility and rounding rules without changing elapsed days or
withdrawals. Requests accrue again on the stored balances; there is no
calendar-month tracking or retry deduplication, so retrying after a lost response
or a partial failure can accrue interest twice. Use the existing GET endpoint to inspect
the resulting balances. Both operations are also available through Swagger UI.

The operation runs synchronously for the kata's small dataset. It selects deposit
IDs once, leaving deposits inserted afterward for a subsequent run. IDs remain in
memory and are processed sequentially in batches of at most 500. Each batch runs
in its own transaction, locking deposits in ascending ID order before reading
balances and retaining locks until that batch commits or rolls back. If a batch
fails, it rolls back, earlier batches remain committed, and later batches are
skipped; the endpoint returns HTTP 500. Concurrent requests can interleave between
batches, while locks ensure calculations use the latest committed balances. See
[the batch transaction decision](docs/adr/0002-independent-accrual-batches.md).

Alternatively, run the executable JAR after `mvn verify`:

```sh
java -jar target/time-deposit-kata-1.0-SNAPSHOT.jar
```

The default database URL is `jdbc:postgresql://localhost:5432/time_deposit`, with
username and password `time_deposit` for local development. Override `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD` for another database, and `PORT` for another HTTP
port. To change the Compose database port, set `DB_PORT` and update `DB_URL` to
match. The database service creates the database; Liquibase creates its tables
on application startup. Hibernate uses `ddl-auto: validate` and does not create
or update the schema.

Request tracing and trace/span log correlation are enabled. Export to an
OpenTelemetry collector is disabled locally; set `OTLP_EXPORT_ENABLED=true`
and configure Spring Boot's OTLP endpoints when a collector is available.

Migrations preserve the assignment's camel-case names using quoted PostgreSQL
identifiers, including `"timeDeposits"`, `"planType"`, and `"timeDepositId"`.
Both primary keys are auto-incrementing integers. Monetary columns use
`DECIMAL(19,2)`, assuming two fractional digits for the assignment's currency.
Required columns are non-null, and withdrawals reference an existing deposit.
Migration preconditions mark existing tables as already applied and halt on
errors evaluating the preconditions. Hibernate validates the resulting schema.
Applied migrations are tracked by Liquibase and are not
reapplied on restart; add new XML changesets for future schema changes. The XML
changelogs retain the original YAML logical file paths to preserve migration
identities for databases initialized before the format conversion.

Stop the application with Ctrl+C and stop PostgreSQL with `docker compose down`.
The named Docker volume retains local database data between runs.

## Architecture

`core.domain` contains the `TimeDeposit` aggregate, its `Withdrawal` children,
`TimeDepositPlan` interest rules, and the JPA balance converter. The shared
constructor, getters, balance setter, interest calculations, and rounding remain
compatible. Withdrawals use decimal amounts and calendar dates.

`core.business` contains `TimeDepositCalculator`, `TimeDepositsService`, and
`TimeDepositsAccrualService`.
The listing service owns the read-only transaction, maps entities to response
DTOs within that transaction, and calls the existing Spring
Data `TimeDepositRepository` in `infra.outbound.repo`. Its fetch join loads deposits
and their normally lazy withdrawals in one query, including deposits without
withdrawals. An integration test clears the persistence context and verifies the
query count while accessing children of multiple deposits.

`TimeDepositsService` selects IDs and coordinates batches without starting a
transaction for the whole run. It invokes the separate Spring-managed
`TimeDepositsAccrualService`, whose `REQUIRES_NEW` transaction covers one batch,
even when invoked from another transaction. The batch service loads only the
specified deposits with pessimistic write locks, without fetching withdrawals,
and delegates calculation to the injected `TimeDepositCalculator`. JPA persists
the managed balance changes when each batch commits. Integration tests verify
multiple full batches and a final partial batch, failure isolation, independent
commits, and concurrent updates using the latest committed balances.

`infra.inbound.web.TimeDepositController` maps HTTP requests to the service and
returns its deposit and withdrawal DTOs directly. Persistence entities are
not serialized directly. HTTP integration tests verify stored values, ordering,
withdrawal associations and empty results against
PostgreSQL with Liquibase migrations.

## AI-assisted workflow

Use Codex with this directory as its workspace and load [AGENTS.md](AGENTS.md)
as the repository instructions. Provide the requested change, then validate the
result using Java 25 with `mvn verify checkstyle:check`. This workflow requires no
additional agent services or credentials. AI assistance was used to configure
Spring Boot startup, PostgreSQL, Liquibase migrations, and database integration
tests. For the listing endpoint, AI assistance clarified the response contract,
recorded domain vocabulary, wrote OpenAPI and HTTP tests before implementation,
confirmed HTTP 404 failures, and implemented the service, fetch plan, and DTOs.
For the accrual endpoint, AI assistance clarified retry and transaction semantics,
recorded the decision, wrote the OpenAPI contract and HTTP tests, confirmed HTTP
404 failures before implementation, and added locking and rollback verification.
AI assistance also planned and implemented independent batches of 500, confirmed
the changed failure contract with a failing HTTP test, and verified partial
success, batch rollback, transaction propagation, and locking.
The tests exercise the resulting setup against a real database.

Maven requires Java 25, compiles with `--release 25`, and checks production and
test sources against `checkstyle/openjdk_checks.xml` during validation. Run the
style check separately with:

```sh
mvn checkstyle:check
```

See [the assignment](../README.md) for business requirements and
[AGENTS.md](AGENTS.md) for implementation conventions.
