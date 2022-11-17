package uk.nhs.nhsdigital.mhd

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.server.RestfulServer
import ca.uhn.fhir.rest.server.interceptor.CorsInterceptor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.cors.CorsConfiguration
import uk.nhs.nhsdigital.mhd.configuration.FHIRServerProperties
import uk.nhs.nhsdigital.mhd.interceptor.AWSAuditEventLoggingInterceptor
import uk.nhs.nhsdigital.mhd.interceptor.CapabilityStatementInterceptor
import uk.nhs.nhsdigital.mhd.provider.*
import java.util.*
import javax.servlet.annotation.WebServlet

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

        val config = CorsConfiguration()
        config.addAllowedHeader("x-fhir-starter")
        config.addAllowedHeader("Origin")
        config.addAllowedHeader("Accept")
        config.addAllowedHeader("X-Requested-With")
        config.addAllowedHeader("Content-Type")
        config.addAllowedHeader("Authorization")
        config.addAllowedHeader("x-api-key")

        config.addAllowedOrigin("*")

        config.addExposedHeader("Location")
        config.addExposedHeader("Content-Location")
        config.allowedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        // Create the interceptor and register it
        interceptorService.registerInterceptor(CorsInterceptor(config))

        isDefaultPrettyPrint = true
        defaultResponseEncoding = EncodingEnum.JSON
    }
}
