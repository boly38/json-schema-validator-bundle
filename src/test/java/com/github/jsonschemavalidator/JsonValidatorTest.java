package com.github.jsonschemavalidator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JsonValidatorTest {
    final String MSG_VERSION = "\"ver\":\"myVer/2014\"";

    
    @Test
    public void should_validate_json_message() {
        // GIVEN
        String jsonMessage = "{"+MSG_VERSION + "}";
        String jsonSchema = "msgversion.schema.json";
        String jsonSchemaDir = "src/test/resources";
        
        
        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, true);
        
        // THEN
        assertTrue(isValid);
    }

}
