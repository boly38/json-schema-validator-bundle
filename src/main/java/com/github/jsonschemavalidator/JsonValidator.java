package com.github.jsonschemavalidator;

import java.io.IOException;

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
    protected JsonUtils jsonUtils;

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
            LOG.debug("no bundle");
            return JsonSchemaFactory.byDefault();
        }
        ClassLoader specialClassLoader = selected.getClass().getClassLoader();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            BundleClassLoader loaderToUse = new BundleClassLoader(specialClassLoader, selected);
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
            String jsonSchemaDir, String jsonSchema) throws ProcessingException, IOException {
        jsonUtils = new JsonUtils(jsonSchemaDir);
        final JsonNode jsonSchemaNode = jsonUtils.loadResource(jsonSchema);
        // LOG.debug("schema loaded : " + jsonSchemaNode.toString());
        final JsonNode jsonMessageNode = JsonLoader.fromString(jsonMessage);
        final JsonSchemaFactory factory = getJsonSchemaFactory();
        final JsonSchema schema = factory.getJsonSchema(jsonSchemaNode);
        return schema.validate(jsonMessageNode);
    }

    public boolean isJsonValid(String jsonMessage,  String jsonSchemaDir, String jsonSchema) {
        return isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema, false);
    }

    public boolean isJsonValid(String jsonMessage,  String jsonSchemaDir, String jsonSchema, boolean verboseOutput) {
        try {
            ProcessingReport report = jsonValidation(jsonMessage, jsonSchemaDir, jsonSchema);
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
}
