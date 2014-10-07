package com.github.jsonschemavalidator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

public class JsonValidatorTest {
    final String MSG_VERSION = "\"ver\":\"myVer/2014\"";

    @Test
    public void should_validate_json_message() {
        // GIVEN
        String jsonMessage = "{" + MSG_VERSION + "}";
        String jsonSchema = "msgversion.schema.json";
        String jsonSchemaDir = "src/test/resources";

        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isJsonValid(jsonMessage, jsonSchemaDir,
                jsonSchema, true);

        // THEN
        assertTrue(isValid);
    }


    @Test
    public void should_validate_regex_message() {
        // GIVEN
        String jsonMessage = "{\"folder\":\"/tmp\",\"phone\":\"+1 415 599 2671\"}";
        String jsonSchema = "regex_phone.schema.json";
        String jsonSchemaDir = "src/test/resources";
        
        
        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, true);
        
        // THEN
        assertTrue(isValid);
    }

    @Test
    public void should_fail_regex_message() {
        // GIVEN
        String jsonMessage = "{\"folder\":\"Q\"}";
        String jsonSchema = "regex_phone.schema.json";
        String jsonSchemaDir = "src/test/resources";
        
        
        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, true);
        
        // THEN
        assertFalse(isValid);
    }
    
    @Ignore("schema 4 do not support phone numbers, probably create a schema3 test.")
    @Test
    public void should_fail_phone_message() {
        // GIVEN
        String jsonMessage = "{\"folder\":\"/tmp\",\"phone\":\"+42\"}";
        String jsonSchema = "regex_phone.schema.json";
        String jsonSchemaDir = "src/test/resources";
        
        
        JsonValidatorService validator = new JsonValidator();
        System.out.println("MSG:" + jsonMessage);
        // WHEN
        boolean isValid = validator.isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, true);
        
        // THEN
        assertFalse(isValid);
    }
}
