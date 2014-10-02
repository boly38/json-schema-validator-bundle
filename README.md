json-schema-validator-bundle
============================

json-schema-validator issue#111 proposal

Try to make an OSGi'fyed version (bundle) of [json-schema-validator](https://github.com/fge/json-schema-validator)

Howto
=====

install required osgi bundles :

        btf-1.2
        jackson-annotations-2.4.1.jar
        jackson-core-2.4.1.1.jar
        msg-simple-1.1.jar
        jackson-databind-2.4.1.3.jar
        guava-16.0.1.jar
        uri-template-0.9.jar
        jackson-coreutils-1.8.jar
        rhino-1.7R4.jar
        libphonenumber-6.0.jar
        mailapi-1.4.3.jar
        jopt-simple-4.8-SNAPSHOT.jar
        joda-time-2.3.jar
        json-schema-core-1.2.4.jar
        json-schema-validator-2.2.5.jar

build and install this bundle

usage example:

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY, policy = ReferencePolicy.DYNAMIC)
    protected JsonValidatorService jsonValidator;
    
    ..
    public boolean isValidMsgVersion(String jsonMessage) {
        String jsonSchemaDir = "resource/json_schema";
        String jsonSchema = "msgversion.schema.json";
        return jsonValidator.isJsonValid(jsonMessage, jsonSchemaDir, jsonSchema);
    }


related issues :
- [json-schema-validator issues #111](https://github.com/fge/json-schema-validator/issues/111)
- [blog post](http://curiositedevie.blogspot.fr/2014/08/use-json-schema-validator-as-osgi-bundle.html)