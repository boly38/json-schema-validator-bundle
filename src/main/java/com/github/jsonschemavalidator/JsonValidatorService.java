package com.github.jsonschemavalidator;

import java.io.IOException;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public interface JsonValidatorService {

    ProcessingReport jsonValidation(String jsonMessage, String jsonSchemaDir, String jsonSchema) throws ProcessingException, IOException;
    boolean isJsonValid(String jsonMessage,  String jsonSchemaDir, String jsonSchema);
    boolean isJsonValid(String jsonMessage,  String jsonSchemaDir, String jsonSchema, boolean verboseOutput);
}
