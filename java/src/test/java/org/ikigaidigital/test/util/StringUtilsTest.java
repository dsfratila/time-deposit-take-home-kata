package org.ikigaidigital.test.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

class StringUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {"fixtures/string-utils.json", "/fixtures/string-utils.json"})
    void readsUtf8JsonFromTheClasspath(String resourcePath) {
        String json = StringUtils.readJson(resourcePath);

        String message = "caf" + Character.toString(0xE9);
        assertThat(json).contains(message);
        assertThatJson(json).node("message").isEqualTo(message);
    }

    @Test
    void preservesDynamicValueMatchersInExpectedResponses() {
        String expected = StringUtils.readJson("fixtures/string-utils.json");

        assertThatJson("""
                {"id": 42, "reference": "generated", "active": true, "message": "caf%s"}
                """.formatted(Character.toString(0xE9)))
                .isEqualTo(expected);
    }

    @Test
    void reportsThePathOfAMissingResource() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> StringUtils.readJson("fixtures/missing.json"))
                .withMessage("JSON resource not found: fixtures/missing.json");
    }
}
