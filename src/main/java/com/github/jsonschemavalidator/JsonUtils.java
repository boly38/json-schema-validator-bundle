package com.github.jsonschemavalidator;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;

public class JsonUtils {
    /**
     * Logger
     */
    private Log LOG = LogFactory.getLog(JsonUtils.class.getName());

    private String storagePath;

    public JsonUtils(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * Load one storage resource
     *
     * @param name name of the resource
     * @return a JSON document
     * @throws IOException resource not found
     */
    public JsonNode loadResource(final String name)
        throws IOException {
        String schemaFullFilename = storagePath;
        if (!storagePath.endsWith("/")
          &&!storagePath.endsWith("\\")) {
            schemaFullFilename += File.separator; 
        }
        schemaFullFilename += name;
        LOG.debug("load schema : " + schemaFullFilename);
        File schemaFile = new File(schemaFullFilename);
        return JsonLoader.fromFile(schemaFile);
    }
}
