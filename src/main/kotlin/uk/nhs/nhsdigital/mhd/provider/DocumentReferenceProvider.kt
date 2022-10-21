package uk.nhs.nhsdigital.mhd.provider

import ca.uhn.fhir.rest.annotation.IdParam
import ca.uhn.fhir.rest.annotation.OptionalParam
import ca.uhn.fhir.rest.annotation.Read
import ca.uhn.fhir.rest.annotation.Search
import ca.uhn.fhir.rest.param.DateRangeParam
import ca.uhn.fhir.rest.param.StringParam
import ca.uhn.fhir.rest.param.TokenParam
import ca.uhn.fhir.rest.server.IResourceProvider
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.nhsdigital.mhd.interceptor.CognitoAuthInterceptor
import javax.servlet.http.HttpServletRequest

@Component
class DocumentReferenceProvider(var cognitoAuthInterceptor: CognitoAuthInterceptor) : IResourceProvider {
    override fun getResourceType(): Class<DocumentReference> {
        return DocumentReference::class.java
    }

    @Read
    fun read(httpRequest : HttpServletRequest, @IdParam internalId: IdType): DocumentReference? {
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, null)
        return if (resource is DocumentReference) resource else null
    }

    @Search
    fun search(
        httpRequest : HttpServletRequest,
        @OptionalParam(name = DocumentReference.SP_PATIENT) patient : TokenParam?,
        @OptionalParam(name = DocumentReference.SP_DATE) date : DateRangeParam?,
        @OptionalParam(name = DocumentReference.SP_IDENTIFIER)  identifier :TokenParam?,
        @OptionalParam(name = DocumentReference.SP_RES_ID)  resid : StringParam?,

    ): List<DocumentReference> {
        val healthcareServices = mutableListOf<DocumentReference>()
        val resource: Resource? = cognitoAuthInterceptor.readFromUrl(httpRequest.pathInfo, httpRequest.queryString)
        if (resource != null && resource is Bundle) {
            for (entry in resource.entry) {
                if (entry.hasResource() && entry.resource is DocumentReference) healthcareServices.add(entry.resource as DocumentReference)
            }
        }

        return healthcareServices
    }
}
