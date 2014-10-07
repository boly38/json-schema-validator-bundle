package com.github.jsonschemavalidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JsonValidatorWellFormattedTest {

    @Test
    public void should_detect_not_well_formatted_json_message() {
        // GIVEN
        String jsonMessage = "{invalidjson:234}";

        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isWellFormattedJsonData(jsonMessage);

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void should_detect_not_well_formatted_null_json_message() {
        // GIVEN
        String jsonMessage = null;

        JsonValidatorService validator = new JsonValidator();
        // WHEN
        boolean isValid = validator.isWellFormattedJsonData(jsonMessage);

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void should_detect_not_well_formatted_empty_json_message() {
        // GIVEN
        String jsonMessage = "";

        JsonValidatorService validator = new JsonValidator();
        // WHEN
        boolean isValid = validator.isWellFormattedJsonData(jsonMessage);

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void should_detect_text_not_well_formatted_json_message() {
        // GIVEN
        String jsonMessage = "blah blah";

        JsonValidatorService validator = new JsonValidator();
        // WHEN
        boolean isValid = validator.isWellFormattedJsonData(jsonMessage);

        // THEN
        assertFalse(isValid);
    }

    @Test
    public void should_detect_well_formatted_json_message() {
        // GIVEN
        String jsonMessage = "{\"validjson\":234}";

        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isWellFormattedJsonData(jsonMessage);

        // THEN
        assertTrue(isValid);
    }

}
