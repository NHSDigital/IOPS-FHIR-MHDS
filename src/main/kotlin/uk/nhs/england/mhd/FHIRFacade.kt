package uk.nhs.england.mhd

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import uk.nhs.england.mhd.configuration.FHIRServerProperties


@SpringBootApplication
@ServletComponentScan
@EnableConfigurationProperties(FHIRServerProperties::class)
open class FHIRFacade

fun main(args: Array<String>) {
    runApplication<uk.nhs.england.mhd.FHIRFacade>(*args)
}
