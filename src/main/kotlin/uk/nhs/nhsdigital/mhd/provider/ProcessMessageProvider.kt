package uk.nhs.nhsdigital.mhd.provider

import ca.uhn.fhir.rest.annotation.Operation
import ca.uhn.fhir.rest.annotation.ResourceParam
import ca.uhn.fhir.rest.annotation.Transaction
import ca.uhn.fhir.rest.annotation.TransactionParam
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component
import uk.nhs.nhsdigital.mhd.awsProvider.AWSBinary
import uk.nhs.nhsdigital.mhd.awsProvider.AWSBundle
import uk.nhs.nhsdigital.mhd.awsProvider.AWSDocumentReference
import uk.nhs.nhsdigital.mhd.awsProvider.AWSPatient
import uk.nhs.nhsdigital.mhd.configuration.FHIRServerProperties


@Component
class ProcessMessageProvider(
    val fhirServerProperties: FHIRServerProperties,
    val awsDocumentReference: AWSDocumentReference,
    val awsBundle: AWSBundle,
    val awsBinary: AWSBinary
) {

    @Operation(name = "\$process-message", idempotent = true)
    fun expand(@ResourceParam bundle:Bundle,
    ): OperationOutcome? {
        val filterMessageHeaders = awsBundle.filterResources(bundle,"MessageHeader")

        var operationOutcome = OperationOutcome();
        var focusType : String? = null
        if (filterMessageHeaders.size > 0) {
            val messageHeader = filterMessageHeaders[0] as MessageHeader
            if (messageHeader.hasEventCoding()) {
                when (messageHeader.eventCoding.code) {
                    "document" -> {
                        focusType = "DocumentReference"
                    }
                }
                if (focusType != null) {
                    processFocusResource(bundle,focusType,operationOutcome)

                }
            }
        }
        return operationOutcome
    }
    @Transaction
    fun transaction(@TransactionParam bundle:Bundle,
    ): Bundle {
        // only process document metadata
        val list = processTransaction(bundle,"DocumentReference")
        val bundle = Bundle()
        bundle.type = Bundle.BundleType.TRANSACTIONRESPONSE
        for (resource in list) {
            bundle.entry.add(Bundle.BundleEntryComponent()
                //.setResource(resource)
                .setResponse(Bundle.BundleEntryResponseComponent()
                    .setStatus("200 OK")
                    .setLocation(resource.id)
                )
            )
        }
        return bundle
    }

    fun processFocusResource(bundle: Bundle, focusType : String, operationOutcome: OperationOutcome)
           {

        var focusResources = ArrayList<Resource>()
        focusResources.addAll(awsBundle.filterResources(bundle,focusType))

        if (focusResources.size>0) {
            for (workerResource in focusResources) {
                when (focusType) {

                    "DocumentReference" -> {
                        val document = workerResource as DocumentReference
                        if (document.hasContent()) {
                            for(content in document.content) {
                                if (content.hasAttachment()) {
                                    val attachment = content.attachment
                                    if (attachment.hasUrl()) {
                                        val entry = awsBundle.findResource(bundle, "Binary", attachment.url)
                                        if (entry != null && entry is Binary) {
                                            val outcome = awsBinary.create(entry)
                                            if (outcome != null && outcome.resource != null && outcome.resource is Binary)  content.attachment.url = fhirServerProperties.server.baseUrl + "/Binary/" + (outcome.resource as Binary).id
                                        }
                                    }
                                }
                            }
                        }
                        val documentReference = awsDocumentReference.createUpdateAWSDocumentReference(workerResource as DocumentReference,bundle)
                        if (documentReference != null) {
                            bundle.entry.add(Bundle.BundleEntryComponent().setResource(documentReference))
                            operationOutcome.issue.add(
                                OperationOutcome.OperationOutcomeIssueComponent()
                                    .setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
                                    .setCode(OperationOutcome.IssueType.INFORMATIONAL)
                                    .addLocation(documentReference.id))
                        }
                    }
                }
            }
        }

    }

    fun processTransaction(bundle: Bundle, focusType : String) : List<Resource>
    {
        val returnBundle = ArrayList<Resource>()
        if (bundle.hasEntry()) {
            for (entry in bundle.entry) {
                if (entry.hasResource()) {
                    val workerResource = entry.resource
                    if (workerResource is DocumentReference) {
                        val document = workerResource as DocumentReference
                        if (document.hasContent()) {
                            for(content in document.content) {
                                if (content.hasAttachment()) {
                                    val attachment = content.attachment
                                    if (attachment.hasUrl()) {
                                        val entry = awsBundle.findResource(bundle, "Binary", attachment.url)
                                        if (entry != null && entry is Binary) {
                                            val outcome = awsBinary.create(entry)
                                            if (outcome != null && outcome.resource != null && outcome.resource is Binary)  content.attachment.url = fhirServerProperties.server.baseUrl + "/Binary/" +(outcome.resource as Binary).id
                                        }
                                    }
                                }
                            }
                        }
                        val documentReference = awsDocumentReference.createUpdateAWSDocumentReference(
                            workerResource as DocumentReference, bundle)
                        if (documentReference != null) {
                            returnBundle.add(documentReference)
                        }
                    }
                }
            }
        }
        return returnBundle
    }


}
