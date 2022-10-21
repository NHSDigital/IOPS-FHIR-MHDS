package uk.nhs.nhsdigital.mhd.provider

import ca.uhn.fhir.rest.annotation.Operation
import ca.uhn.fhir.rest.annotation.ResourceParam
import ca.uhn.fhir.rest.annotation.Transaction
import ca.uhn.fhir.rest.annotation.TransactionParam
import org.hl7.fhir.r4.model.*
import org.springframework.stereotype.Component


@Component
class ProcessMessageProvider() {

    @Operation(name = "\$process-message", idempotent = true)
    fun processMesage(@ResourceParam bundle:Bundle,
             ): OperationOutcome? {

        var operationOutcome = OperationOutcome();

        return operationOutcome
    }
    @Transaction
    fun transaction(@TransactionParam bundle:Bundle,
    ): OperationOutcome? {

        var operationOutcome = OperationOutcome();

        return operationOutcome
    }


}
