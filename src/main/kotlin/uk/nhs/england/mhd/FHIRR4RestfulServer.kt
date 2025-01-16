package uk.nhs.england.mhd

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.server.RestfulServer
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.cors.CorsConfiguration
import uk.nhs.england.mhd.configuration.FHIRServerProperties
import uk.nhs.england.mhd.interceptor.AWSAuditEventLoggingInterceptor
import uk.nhs.england.mhd.interceptor.CapabilityStatementInterceptor
import uk.nhs.england.mhd.provider.*
import java.util.*
import jakarta.servlet.annotation.WebServlet

@WebServlet("/FHIR/R4/*", loadOnStartup = 1, displayName = "FHIR Facade")
class FHIRR4RestfulServer(
    @Qualifier("R4") fhirContext: FhirContext,
    public val fhirServerProperties: FHIRServerProperties,

    val documentReferenceProvider:  DocumentReferenceProvider,
    val binaryProvider: BinaryProvider,
    val processMessageProvider: ProcessMessageProvider


) : RestfulServer(fhirContext) {

    override fun initialize() {
        super.initialize()

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        registerProvider(documentReferenceProvider)
        registerProvider(binaryProvider)
        registerProvider(processMessageProvider)

        val awsAuditEventLoggingInterceptor =
            AWSAuditEventLoggingInterceptor(
                this.fhirContext,
                fhirServerProperties
            )

        interceptorService.registerInterceptor(awsAuditEventLoggingInterceptor)
        registerInterceptor(CapabilityStatementInterceptor(fhirServerProperties))

        isDefaultPrettyPrint = true
        defaultResponseEncoding = EncodingEnum.JSON
    }
}
