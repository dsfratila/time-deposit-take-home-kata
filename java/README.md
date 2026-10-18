# Java implementation

Requires JDK 25 and Maven. Set `JAVA_HOME` to your JDK 25 installation and add
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

Maven requires Java 25, compiles with `--release 25`, and checks production and
test sources against `checkstyle/openjdk_checks.xml` during validation. Run the
style check separately with:

```sh
mvn checkstyle:check
```

See [the assignment](../README.md) for business requirements and
[AGENTS.md](AGENTS.md) for implementation conventions.
