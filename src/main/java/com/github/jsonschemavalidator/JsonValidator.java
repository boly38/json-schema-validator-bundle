package com.github.jsonschemavalidator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

@Component(immediate = true, // activate on boot
createPid = false, metatype = false, policy = ConfigurationPolicy.IGNORE)
@Service
public class JsonValidator implements JsonValidatorService {
    private static final String EXCEPTION_WHILE_PARSING = "exception while parsing json message : ";
    /**
     * Logger
     */
    private Log LOG = LogFactory.getLog(JsonValidator.class.getName());

    protected BundleContext bcontext;

    protected void activate(ComponentContext context)
            throws ConfigurationException {
        LOG.debug("activate JSON validator");
        bcontext = context.getBundleContext();
    }

    private JsonSchemaFactory getJsonSchemaFactory() {
        // find the bundle you need:
        String targetBundleSN = "com.github.fge.json-schema-core"; // com.github.fge.json-schema-validator";
        Bundle selected = findBundleBySymbolicName(targetBundleSN);
        if (selected == null) {
            LOG.debug("get JsonSchemaFactory without bundle"); // unit test case
            return JsonSchemaFactory.byDefault();
        }
        ClassLoader specialClassLoader = selected.getClass().getClassLoader();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            BundleClassLoader loaderToUse = new BundleClassLoader(
                    specialClassLoader, selected);
            Thread.currentThread().setContextClassLoader(loaderToUse);
            final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
            return factory;
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private Bundle findBundleBySymbolicName(String targetBundleSN) {
        Bundle selected = null;
        Bundle[] osgiBundles = bcontext != null ? bcontext.getBundles() : null;
        if (osgiBundles == null) {
            return null;
        }
        for (Bundle bundle : osgiBundles) {
            String bundleSN = bundle.getSymbolicName();
            if (targetBundleSN.equals(bundleSN)) {
                selected = bundle;
                break;
            }
            // LOG.debug("bundle => " + bundleSN);
        }
        LOG.debug("bundle *found* => " + selected.getSymbolicName());
        return selected;
    }

    public ProcessingReport jsonValidation(String jsonMessage,
            String jsonSchemaDir, String jsonSchema)
            throws ProcessingException, IOException {
        final JsonNode jsonSchemaNode = loadResource(jsonSchemaDir, jsonSchema);
        // LOG.debug("schema loaded : " + jsonSchemaNode.toString());
        final JsonNode jsonMessageNode = JsonLoader.fromString(jsonMessage);
        final JsonSchemaFactory factory = getJsonSchemaFactory();
        final JsonSchema schema = factory.getJsonSchema(jsonSchemaNode);
        return schema.validate(jsonMessageNode);
    }

    public ProcessingReport jsonValidation(String jsonMessage,
            JsonNode jsonSchemaNode) throws ProcessingException, IOException {
        final JsonNode jsonMessageNode = JsonLoader.fromString(jsonMessage);
        final JsonSchemaFactory factory = getJsonSchemaFactory();
        final JsonSchema schema = factory.getJsonSchema(jsonSchemaNode);
        return schema.validate(jsonMessageNode);
    }

    public boolean isJsonValid(String jsonMessage, JsonNode jsonSchemaNode) {
        return isJsonValid(jsonMessage, jsonSchemaNode, false);
    }

    public boolean isJsonValid(String jsonMessage, String jsonSchemaDir,
            String jsonSchema) {
        return isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, false);
    }

    public boolean isJsonValid(String jsonMessage, JsonNode jsonSchemaNode,
            boolean verboseOutput) {
        try {
            ProcessingReport report = jsonValidation(jsonMessage,
                    jsonSchemaNode);
            boolean reportIsSuccess = report != null && report.isSuccess();
            if (!reportIsSuccess && verboseOutput) {
                LOG.info("Json message is not conform to the schema :\n"
                        + report);
            }
            return reportIsSuccess;
        } catch (Exception e) {
            LOG.warn(EXCEPTION_WHILE_PARSING + e.getMessage(), e);
            return false;
        }
    }

    public boolean isJsonValid(String jsonMessage, String jsonSchemaDir,
            String jsonSchema, boolean verboseOutput) {
        try {
            ProcessingReport report = jsonValidation(jsonMessage,
                    jsonSchemaDir, jsonSchema);
            boolean reportIsSuccess = report != null && report.isSuccess();
            if (!reportIsSuccess && verboseOutput) {
                LOG.info("Json message is not conform to " + jsonSchema
                        + " :\n" + report);
            }
            return reportIsSuccess;
        } catch (Exception e) {
            LOG.warn(EXCEPTION_WHILE_PARSING + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isWellFormattedJsonData(String jsonMessage) {
        // avoid NPE
        if (jsonMessage == null || jsonMessage.isEmpty()) {
            return false;
        }
        try {
            JsonLoader.fromString(jsonMessage);
            return true;
        } catch (Exception e) {
            LOG.debug(e);
            return false;
        }
    }


    /**
     * Load one storage resource
     *  search in Filesystem first and then in the current bundle resources 
     * @param name name of the resource
     * @param relativePath relative path directory of the resource
     * @return a JSON document
     * @throws IOException resource not found
     */
    public JsonNode loadResource(final String relativePath, final String name)
        throws IOException {
        Bundle bundleToSearch = bcontext == null ? null : bcontext.getBundle();
        return loadResource(relativePath, name, bundleToSearch);
    }

    /**
     * Load one storage resource
     *  search in Filesystem first and then in the given bundle resources 
     *
     * @param relativePath relative path directory of the resource
     * @param name name of the resource
     * @param bundle bundle to search in
     * @return a JSON document
     * @throws IOException resource not found
     */
    public JsonNode loadResource(final String relativePath, final String name, Bundle bundle)
        throws IOException {
        String schemaFullFilename = relativePath;
        if (!relativePath.endsWith("/")
          &&!relativePath.endsWith("\\")) {
            schemaFullFilename += File.separator; 
        }
        schemaFullFilename += name;

        File schemaFile = new File(schemaFullFilename);
        if (schemaFile == null || !schemaFile.exists()) {
            URL jsonResourceUrl = null;
            if (bundle != null) {
                LOG.debug("load from bundle : " + bundle.getSymbolicName());
                schemaFullFilename = "/" + relativePath + "/" + name;
                jsonResourceUrl = bundle.getResource(schemaFullFilename);
            }
            if (jsonResourceUrl != null) {
                return JsonLoader.fromURL(jsonResourceUrl);
            }
        }
        if (schemaFile == null || !schemaFile.exists()) {
            LOG.warn("unable to load schema : " + schemaFullFilename);
            throw new IOException(schemaFullFilename + " not found");
        }
        return JsonLoader.fromFile(schemaFile);
    }
}
