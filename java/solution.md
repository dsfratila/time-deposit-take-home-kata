# AI-assisted development workflow

I used an AI coding workflow throughout this take-home exercise, with repository
instructions, reusable skills, and executable checks to guide and validate the
work. The setup combined Codex, Matt Pocock's skills, and the project-specific
harness in [AGENTS.md](AGENTS.md).

## Tools and configuration

- **Coding agent used for implementation:** Codex CLI with the `gpt-6.1-sol`
  model, working in the `java` directory with access to the source, Git history,
  documentation, and terminal commands.
- **Agent configuration:** [AGENTS.md](AGENTS.md) provides the repository
  instructions used by the coding agent as part of the harness.
  It defines compatibility constraints, package responsibilities, testing steps,
  database conventions, and completion checks. I configured the harness after
  setting up the project infrastructure and refined it during implementation.
- **Reusable skills:** I used `grill-with-docs`, `implement`, and `retro` from
  [Matt Pocock's skills](https://github.com/mattpocock/skills/tree/main/skills)
  during implementation.
- **Application tooling:** Java 25, Maven, Spring Boot 4, Spring Data JPA,
  PostgreSQL, and Docker Compose. Dependency versions and Maven plugins are
  recorded in [pom.xml](pom.xml).
- **Verification:** JUnit, AssertJ, Mockito, REST Assured, JsonUnit, and
  Testcontainers. Integration tests use PostgreSQL and the application migrations.

Checkstyle enforces the configured Java style rules for production and test code
using [openjdk_checks.xml](checkstyle/openjdk_checks.xml). It runs during Maven's
`validate` phase, so generated and manually edited code face the same checks.
Checkstyle reports violations; it does not automatically format the code.

Liquibase standardizes database versioning through committed XML changesets in
[db/changelog](src/main/resources/db/changelog). Hibernate validates the schema
instead of creating or updating it. The same migrations run locally and in
container-based integration tests, making schema changes reproducible.

## Custom rules and working loop

I first set up the project infrastructure, then configured the AI agent harness
in [AGENTS.md](AGENTS.md). For each subsequent task, I used `grill-with-docs` to
clarify the feature or API requirements and generate an architectural decision
record (ADR). I then handed that ADR to the AI agent for implementation under
the repository rules, including the required tests and verification.

The harness and decision records keep the agreed constraints available to
subsequent agent sessions instead of relying on conversation history alone.
Domain vocabulary is recorded in [GLOSSARY.md](GLOSSARY.md), and transaction and
retry decisions are recorded in [the ADRs](docs/adr).

## Development sequence and AI assistance

The Git history records the implementation sequence below. The descriptions
summarize how AI assisted with each stage; commit history alone does not establish
which changes were AI-generated. Repository rules and executable checks guided
and validated the work.

| Step | Commit | Work and purpose of AI assistance |
| --- | --- | --- |
| Establish tooling | `ffff055` | Configure Java 25, dependencies, Checkstyle, and the initial agent instructions to establish a consistent working environment. |
| Characterize existing behavior | `85c4f00` | Add calculation cases and reusable fixtures before changing working logic, protecting compatibility. |
| Refactor interest calculation | `fb4a2ab` | Move behavior into the deposit and plan rules, keeping the calculator small and preserving characterized results. |
| Add persistence | `fcebe19` | Configure Spring Boot, PostgreSQL, JPA, Liquibase, and integration tests to make storage and startup reproducible. |
| Refine infrastructure | `ade9a27` | Align packages with their responsibilities and organize shared test helpers and monetary conversion tests. |
| Implement listing | `989900f` | Clarify the contract, add HTTP tests and JSON fixtures, then implement DTOs and a fetch plan with database query-count coverage. |
| Implement balance updates | `417bdb8` | Define accrual and retry semantics, implement independently committed batches of at most 500, and test persisted effects, rollback, and concurrent locking. |

AI also assisted with setup instructions, OpenAPI documentation, domain
terminology, and decision records to keep the implementation and its explanation
aligned. The README describes HTTP 404 failures before endpoint implementation
and a failing HTTP test before changing batch failure semantics. These are
documented development observations; the repository does not preserve a test-run
transcript for every intermediate Red/Green cycle.

## Reproducing the workflow

1. Install JDK 25, Maven, and Docker with a working container runtime. Follow
   [README.md](README.md) to configure `JAVA_HOME` and run the application.
2. Open the `java` directory in Codex CLI, select the `gpt-6.1-sol` model, and load
   [AGENTS.md](AGENTS.md) as its repository instructions.
3. Make the three skills listed above available to the assistant. The repository
   links to their upstream source; their definitions and an exact upstream
   revision are not vendored here. The exact Codex CLI version is also not
   recorded, so these steps reproduce the workflow rather than an identical AI
   environment.
4. For each task, use `grill-with-docs` to clarify the feature or API and generate
   an ADR. Hand the ADR to the AI agent for implementation, requiring it to follow
   the committed instructions, inspect relevant contracts and tests, and verify
   its changes.
5. Use the following commands from `java` with Maven running on Java 25:

   ```sh
   mvn -version
   mvn -Dtest=TimeDepositCalculatorTest test
   mvn -Dit.test=ListTimeDepositsIntegrationTest test-compile failsafe:integration-test failsafe:verify
   mvn verify
   mvn checkstyle:check
   ```

The focused commands illustrate the development loop; select the test matching
the change. `verify` includes the integration suite and requires Docker access.
The current Mockito setup uses runtime Java agent attachment, so the test
environment must permit it; the Maven configuration does not explicitly supply
a Mockito Java agent.

The workflow uses the assistant, repository files, and standard build tools; it
does not require a separate agent orchestration service. Build configuration,
schema migrations, contracts, and tests are committed so the resulting software
can be checked independently of the AI session.
