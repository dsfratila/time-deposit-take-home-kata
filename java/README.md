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
tests are rolled back.

## Start the application

Start the local PostgreSQL database, then run Spring Boot:

```sh
docker compose up -d --wait
mvn spring-boot:run
```

The application listens on `http://localhost:8080`. Check startup with
`curl http://localhost:8080/actuator/health`; the response should contain
`"status":"UP"`. Swagger UI is available at
`http://localhost:8080/swagger-ui/index.html`. Business endpoints have not yet
been implemented.

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
Migrations halt on unexpected existing tables instead of treating an unverified
schema as compatible. Applied migrations are tracked by Liquibase and are not
reapplied on restart; add new XML changesets for future schema changes. The XML
changelogs retain the original YAML logical file paths to preserve migration
identities for databases initialized before the format conversion.

Stop the application with Ctrl+C and stop PostgreSQL with `docker compose down`.
The named Docker volume retains local database data between runs.

## Architecture

`core.domain` contains the interest plans and the framework-independent
`TimeDepositRepository` outbound port. `infra.outbound.repo` implements that port with
`JpaTimeDepositRepositoryAdapter`, backed by the Spring Data repository, and owns
the JPA balance converter. Application use cases should depend on the domain
port rather than the Spring Data interface.

`TimeDeposit` and `TimeDepositPlan` belong to `core.domain`, and
`TimeDepositCalculator` belongs to `core.business`. Their tests use matching
packages. `TimeDeposit` retains its constructor, method signatures, and JPA
mapping. Its converter reference is the explicit infrastructure dependency
documented in AGENTS.md. Interest calculations and rounding are unchanged.

There are no application services or REST controllers yet. Add orchestration in
`core.business` and HTTP adapters and response DTOs in `infra.inbound.web` when implementing those
use cases. The repository integration test verifies Spring wiring, generated
IDs, and decimal persistence through the domain port against PostgreSQL; its
transaction is rolled back after the test.

## AI-assisted workflow

Use Codex with this directory as its workspace and load [AGENTS.md](AGENTS.md)
as the repository instructions. Provide the requested change, then validate the
result using Java 25 with `mvn verify checkstyle:check`. This workflow requires no
additional agent services or credentials. AI assistance was used to configure
Spring Boot startup, PostgreSQL, Liquibase migrations, and database integration
tests, and to separate the domain repository port from its JPA adapter; the tests
exercise the resulting setup against a real database.

Maven requires Java 25, compiles with `--release 25`, and checks production and
test sources against `checkstyle/openjdk_checks.xml` during validation. Run the
style check separately with:

```sh
mvn checkstyle:check
```

See [the assignment](../README.md) for business requirements and
[AGENTS.md](AGENTS.md) for implementation conventions.
