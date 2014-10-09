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
import com.github.fge.jsonschema.core.report.ListReportProvider;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
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

    private ProcessingReport jsonSchemaValidation(JsonNode jsonSchemaNode,
            final String jsonMessage) throws ProcessingException {

        if (bcontext == null) { //  unit test case
            LOG.debug("jsonSchemaValidationAction without class loader switch"); 
            return jsonSchemaValidationAction(jsonSchemaNode, jsonMessage);
        }

        // OSGi context: need to switch ClassLoader to access 
        // bundle resources

        ClassLoader specialClassLoader = bcontext.getBundle().getClass()
                .getClassLoader();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            LOG.debug("jsonSchemaValidationAction with a dedicated class loader");
            BundleClassLoader schemaCoreClassLoader = new BundleClassLoader(
                    specialClassLoader, bcontext.getBundle());
            Thread.currentThread().setContextClassLoader(schemaCoreClassLoader);
            return jsonSchemaValidationAction(jsonSchemaNode, jsonMessage);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    ProcessingReport jsonSchemaValidationAction(final JsonNode jsonSchemaNode,
            final String jsonMessage)
            throws ProcessingException {
        JsonNode jsonMessageNode = null;
        try {
            jsonMessageNode = JsonLoader.fromString(jsonMessage);
        } catch (IOException e) {
            return produceErrorReport(e.getMessage());
        }
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        final JsonSchema schema = factory.getJsonSchema(jsonSchemaNode);
        return schema.validate(jsonMessageNode);
    }



    ProcessingReport produceErrorReport(String basicError)
            throws ProcessingException {
        ListReportProvider reportProvider = new ListReportProvider(LogLevel.INFO, LogLevel.FATAL);
        ProcessingReport notValidJsonReport = reportProvider.newReport(LogLevel.INFO, LogLevel.FATAL);
        notValidJsonReport.error(new ProcessingMessage().setMessage(basicError)
                .put("domain", "syntax"));
        return notValidJsonReport;
    }

    public ProcessingReport jsonValidation(String jsonMessage,
            String jsonSchemaDir, String jsonSchema)
            throws ProcessingException, IOException {
        final JsonNode jsonSchemaNode = loadResource(jsonSchemaDir, jsonSchema);
        // LOG.debug("schema loaded : " + jsonSchemaNode.toString());
        return jsonSchemaValidation(jsonSchemaNode, jsonMessage);
    }

    public ProcessingReport jsonValidation(String jsonMessage,
            JsonNode jsonSchemaNode) throws ProcessingException, IOException {
        return jsonSchemaValidation(jsonSchemaNode, jsonMessage);
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
     * Load one storage resource search in Filesystem first and then in the
     * current bundle resources
     * 
     * @param name
     *            name of the resource
     * @param relativePath
     *            relative path directory of the resource
     * @return a JSON document
     * @throws IOException
     *             resource not found
     */
    public JsonNode loadResource(final String relativePath, final String name)
            throws IOException {
        Bundle bundleToSearch = bcontext == null ? null : bcontext.getBundle();
        return loadResource(relativePath, name, bundleToSearch);
    }

    /**
     * Load one storage resource search in Filesystem first and then in the
     * given bundle resources
     * 
     * @param relativePath
     *            relative path directory of the resource
     * @param name
     *            name of the resource
     * @param bundle
     *            bundle to search in
     * @return a JSON document
     * @throws IOException
     *             resource not found
     */
    public JsonNode loadResource(final String relativePath, final String name,
            Bundle bundle) throws IOException {
        String schemaFullFilename = relativePath;
        if (!relativePath.endsWith("/") && !relativePath.endsWith("\\")) {
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
