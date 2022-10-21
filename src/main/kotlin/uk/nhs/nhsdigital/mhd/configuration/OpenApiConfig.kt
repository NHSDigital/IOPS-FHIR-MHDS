package uk.nhs.nhsdigital.mhd.configuration


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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.nhs.nhsdigital.mhd.util.FHIRExamples


@Configuration
open class OpenApiConfig(@Qualifier("R4") val ctx : FhirContext) {
    var MHD = "Mobile access to Health Documents"
    var ITI65 = "Provide Document Bundle"
    var ITI67 = "Find Document References"
    var ITI68 = "Retrieve Document"

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
                        fhirServerProperties.server.name
                                + "\n "
                                + "\n [UK Core Implementation Guide (0.5.1)](https://simplifier.net/guide/ukcoreimplementationguide0.5.0-stu1/home?version=current)"
                                + "\n\n [NHS Digital Implementation Guide (2.6.0)](https://simplifier.net/guide/nhsdigital?version=2.6.0)"
                    )
                    .termsOfService("http://swagger.io/terms/")
                    .license(License().name("Apache 2.0").url("http://springdoc.org"))
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
                .name(MHD + " " + ITI67)
                .description("[HL7 FHIR Foundation Module](https://hl7.org/fhir/foundation-module.html) \n"
                + " [IHE MHD ITI-67](https://profiles.ihe.net/ITI/MHD/ITI-67.html)")
        )

        oas.addTagsItem(
            io.swagger.v3.oas.models.tags.Tag()
                .name(MHD + " " + ITI68)
                .description("[HL7 FHIR Foundation Module](https://hl7.org/fhir/foundation-module.html) \n"
                        + " [IHE MHD ITI-68](https://profiles.ihe.net/ITI/MHD/ITI-68.html)")
        )
        var examples = LinkedHashMap<String,Example?>()

        examples.put("Provide Document Bundle Message",
            Example().value(FHIRExamples().loadExample("document-message.json",ctx))
        )

        var messageItem = PathItem()
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

        var examples2 = LinkedHashMap<String,Example?>()

        examples2.put("Provide Document Bundle with Comprehensive metadata of one document",
            Example().value(FHIRExamples().loadExample("MHD-transaction.json",ctx))
        )
        var transactionItem = PathItem()
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

        var binaryItem = PathItem()
            .get(
                Operation()
                    .addTagsItem(MHD + " " + ITI68)
                    .summary("Any url can be used for retrieval of a raw document. See [document binary](https://care-connect-documents-api.netlify.app/api_documents_binary.html)")
                    .responses(getApiResponses())
                    .addParametersItem(Parameter()
                        .name("id")
                        .`in`("path")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("The ID of the resource")
                        .schema(StringSchema())
                    )
            )

        oas.path("/FHIR/R4/Binary/{id}",binaryItem)
   
        // DocumentReference
        var documentReferenceItem = PathItem()
            .get(
                Operation()
                    .addTagsItem(MHD + " " + ITI67)
                    .summary("Read DocumentReference")
                    .responses(getApiResponses())
                    .addParametersItem(Parameter()
                        .name("id")
                        .`in`("path")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("The ID of the resource")
                        .schema(StringSchema())
                    )
            )

        oas.path("/FHIR/R4/DocumentReference/{id}",documentReferenceItem)
        documentReferenceItem = PathItem()
            .get(
                Operation()
                    .addTagsItem(MHD + " " + ITI67)
                    .summary("Search DocumentReference")
                    .description("The Health Document Supplier shall support the following search parameters on the DocumentReference resource")
                    .responses(getApiResponses())
                    .addParametersItem(Parameter()
                        .name("patient")
                        .`in`("query")
                        .required(true)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("Who/what is the subject of the document")
                        .schema(StringSchema())
                        .example("073eef49-81ee-4c2e-893b-bc2e4efd2630")
                    )
                    .addParametersItem(Parameter()
                        .name("date")
                        .`in`("query")
                        .required(false)
                        .style(Parameter.StyleEnum.SIMPLE)
                        .description("When this document reference was created")
                        .schema(StringSchema())
                    )


            )
        oas.path("/FHIR/R4/DocumentReference",documentReferenceItem)
        

      
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
}
