package uk.nhs.england.mhd.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fhir")
data class FHIRServerProperties(
    var server: Server
) {
    data class Server(
        var baseUrl: String,
        var name: String,
        var version: String
    )
}
