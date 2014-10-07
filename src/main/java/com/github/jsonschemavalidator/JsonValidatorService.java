package com.github.jsonschemavalidator;

import java.io.IOException;

import org.osgi.framework.Bundle;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;

public interface JsonValidatorService {

    ProcessingReport jsonValidation(String jsonMessage, String jsonSchemaDir,
            String jsonSchema) throws ProcessingException, IOException;

    ProcessingReport jsonValidation(String jsonMessage, JsonNode jsonSchemaNode)
            throws ProcessingException, IOException;

    boolean isJsonValid(String jsonMessage, String jsonSchemaDir,
            String jsonSchema);

    boolean isJsonValid(String jsonMessage, String jsonSchemaDir,
            String jsonSchema, boolean verboseOutput);

    boolean isJsonValid(String jsonMessage, JsonNode jsonSchemaNode);

    boolean isJsonValid(String jsonMessage, JsonNode jsonSchemaNode,
            boolean verboseOutput);

    boolean isWellFormattedJsonData(String jsonMessage);

    JsonNode loadResource(final String relativePath, final String name,
            Bundle bundle) throws IOException;

    JsonNode loadResource(final String relativePath, final String name)
            throws IOException;
}
