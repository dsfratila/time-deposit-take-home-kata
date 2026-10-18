package org.ikigaidigital.test.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class StringUtils {

    private StringUtils() {
    }

    public static String readJson(String resourcePath) {
        String absolutePath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
        try (InputStream resource = StringUtils.class.getResourceAsStream(absolutePath)) {
            if (resource == null) {
                throw new IllegalArgumentException("JSON resource not found: " + resourcePath);
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read JSON resource: " + resourcePath, exception);
        }
    }
}
