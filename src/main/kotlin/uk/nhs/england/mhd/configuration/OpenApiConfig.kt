package uk.nhs.england.mhd.configuration


import ca.uhn.fhir.context.FhirContext
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType

import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.nhs.england.mhd.util.FHIRExamples


@Configuration
open class OpenApiConfig(@Qualifier("R4") val ctx : FhirContext) {
    var MHD = ""
    var DSUB = "Provide Document FHIR RESTful"
    var ITI65 = "Provide Document FHIR Bundle (Message or Transaction)"
    var APIM = "Security and API Management"

    @Bean
    open fun customOpenAPI(
        fhirServerProperties: FHIRServerProperties
       // restfulServer: FHIRR4RestfulServer
    ): OpenAPI? {

        val oas = OpenAPI()
            .info(
                Info()
                    .title(fhirServerProperties.server.name)
                    .version(fhirServerProperties.server.version)
                    .description(
                 ""
                    )
                    .termsOfService("http://swagger.io/terms/")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
            )
        oas.addServersItem(
            Server().description(fhirServerProperties.server.name).url(fhirServerProperties.server.baseUrl)
        )

        // MHD

        oas.addTagsItem(
            io.swagger.v3.oas.models.tags.Tag()
                .name(MHD + " " + ITI65)
                .description("[HL7 FHIR Foundation Module](https://hl7.org/fhir/foundation-module.html) \n"
                        + " [IHE MHD ITI-65](https://profiles.ihe.net/ITI/MHD/ITI-65.html)")
        )

        oas.addTagsItem(
            io.swagger.v3.oas.models.tags.Tag()
                .name(DSUB)
                .description("[HL7 FHIR Foundation Module](https://hl7.org/fhir/foundation-module.html)")
        )

        val examples = LinkedHashMap<String,Example?>()

        examples.put("Provide Document Bundle Message (PDF)",
            Example().value(FHIRExamples().loadExample("document-message.json",ctx))
        )
        examples.put("Provide Document Bundle Message (FHIR Document STU3)",
            Example().value(FHIRExamples().loadExample("document-message-TOC.json",ctx))
        )

        val messageItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(MHD + " " + ITI65)
                    .summary("Send a document message to a Document Repository")
                    .description("API is for illustration only. \n" +
                            "See [process-message](https://simplifier.net/guide/nhsdigital/Home/FHIRAssets/AllAssets/OperationDefinition/process-message) \n\n"+
                            " | Supported Messages | \n" +
                            " |-------| \n" +
                            " | [document](https://simplifier.net/NHSDigital/MessageDefinition-example-duplicate-2/~json) |"
                    )
                    .responses(getApiResponses())
                    .requestBody(
                        RequestBody().content(Content()
                        .addMediaType("application/fhir+json",
                            MediaType()
                                .examples(examples)
                                .schema(StringSchema()))
                    )))


        oas.path("/FHIR/R4/\$process-message",messageItem)

        val examples2 = LinkedHashMap<String,Example?>()

        examples2.put("Provide Document Bundle with Comprehensive metadata of one document",
            Example().value(FHIRExamples().loadExample("MHD-transaction.json",ctx))
        )
        val transactionItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(MHD + " " + ITI65)
                    .summary("Send a MHD transaction Bundle to a Document Repository")
                    .description("API is for illustration only. \n See [transaction](https://www.hl7.org/fhir/R4/http.html#transaction)")
                    .responses(getApiResponses())
                    .requestBody(
                        RequestBody().content(Content()
                            .addMediaType("application/fhir+json",
                                MediaType()
                                    .examples(examples2)
                                    .schema(StringSchema()))
                        )))


        oas.path("/FHIR/R4/",transactionItem)

        // Binary


        val binExampl = LinkedHashMap<String,Example?>()
        binExampl.put("Hello World",Example().value("Hello World"))
        var binaryItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(DSUB)
                    .summary("This is a raw http POST. POST of a FHIR Binary can be used, with application/fhir+json Content-Type header")
                    .responses(getApiResponses())
                    .addParametersItem(Parameter()
                        .name("Content-Type")
                        .`in`("header")
                        .required(true)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("Mime type of the document/image")
                        .schema(StringSchema())
                    )
                    .requestBody(
                        RequestBody().content(Content()
                            .addMediaType("text/plain",
                                MediaType()
                                    .examples(binExampl)
                                    .schema(StringSchema()))
                        ))
            )

        oas.path("/FHIR/R4/Binary",binaryItem)
   
        // DocumentReference

        val examplesDSUB = LinkedHashMap<String,Example?>()

        examplesDSUB.put("Document Notification (PDF)",
            Example().value(FHIRExamples().loadExample("documentReference-DSUB.json",ctx))
        )
        examplesDSUB.put("Document Notification (FHIR Document STU3)",
            Example().value(FHIRExamples().loadExample("documentReference-TOC-Notification.json",ctx))
        )

        var documentReferenceItem = PathItem()
            .post(
                Operation()
                    .addTagsItem(DSUB)
                    .summary("Document Notification")
                    .description("See [Events and Notifications](http://lb-hl7-tie-1794188809.eu-west-2.elb.amazonaws.com/) for FHIR Subscription interactions")
                    .responses(getApiResponses())
                    .requestBody(RequestBody().content(Content()
                        .addMediaType("application/fhir+json",
                            MediaType()
                                .examples(examplesDSUB)
                                .schema(StringSchema()))
                    )))
        oas.path("/FHIR/R4/DocumentReference",documentReferenceItem)


        oas.path("/FHIR/R4/metadata",PathItem()
            .get(
                Operation()
                    .addTagsItem(APIM)
                    .summary("server-capabilities: Fetch the server FHIR CapabilityStatement").responses(getApiResponses())))


        return oas
    }



    fun getApiResponses() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content().addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }

    fun getApiResponsesMarkdown() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content().addMediaType("text/markdown", MediaType().schema(StringSchema()._default("{}")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }
    fun getApiResponsesXMLJSON() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content()
            .addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
            .addMediaType("application/fhir+xml", MediaType().schema(StringSchema()._default("<>")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }

    fun getApiResponsesRAWJSON() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content()
            .addMediaType("application/json", MediaType().schema(StringSchema()._default("{}")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }
    fun getPathItem(tag :String, name : String,fullName : String, param : String, example : String, description : String ) : PathItem {
        val pathItem = PathItem()
            .get(
                Operation()
                    .addTagsItem(tag)
                    .summary("search-type")
                    .description(description)
                    .responses(getApiResponses())
                    .addParametersItem(Parameter()
                        .name(param)
                        .`in`("query")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("The uri that identifies the "+fullName)
                        .schema(StringSchema().format("token"))
                        .example(example)))
        return pathItem
    }

    fun getApiResponsesBinary() : ApiResponses {

        val response200 = ApiResponse()
        response200.description = "OK"
        val exampleList = mutableListOf<Example>()
        exampleList.add(Example().value("{}"))
        response200.content = Content()
            .addMediaType("*/*", MediaType().schema(StringSchema()._default("{}")))
            .addMediaType("application/fhir+json", MediaType().schema(StringSchema()._default("{}")))
            .addMediaType("application/fhir+xml", MediaType().schema(StringSchema()._default("<>")))
        val apiResponses = ApiResponses().addApiResponse("200",response200)
        return apiResponses
    }
}
