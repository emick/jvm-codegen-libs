package com.github.mickeer.codegen.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceUtilTest {

    @ParameterizedTest
    @CsvSource(value = {
            "field; FIELD",
            "myField; MY_FIELD",
            "myAPIKey; MY_API_KEY",
    }, delimiter = ';')
    public void shouldConvertFieldNameToEnumName(String input, String result) {
        assertEquals(result, SourceUtil.fieldNameToEnumName(input));
    }
}
