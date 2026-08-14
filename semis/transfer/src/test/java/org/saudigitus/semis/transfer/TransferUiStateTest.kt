package org.saudigitus.semis.transfer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState

class TransferUiStateTest {

    @Test
    fun `learner selection is required on first step`() {
        assertFalse(TransferUiState().canContinue)
        assertTrue(configuredState(selectedLearnerUids = setOf("learner")).canContinue)
    }

    @Test
    fun `transfer details cannot open before metadata is ready`() {
        assertFalse(
            configuredState(
                selectedLearnerUids = setOf("learner"),
                isLoadingMetadata = true,
            ).canContinue
        )
        assertFalse(TransferUiState(selectedLearnerUids = setOf("learner")).canContinue)
    }

    @Test
    fun `destination step only requires a valid mandatory form`() {
        val incomplete = TransferUiState(
            step = TransferStep.DESTINATION,
        )
        val complete = incomplete.copy(isTransferFormValid = true)

        assertFalse(incomplete.canContinue)
        assertTrue(complete.canContinue)
    }

    @Test
    fun `destination cannot continue while metadata is loading`() {
        val state = TransferUiState(
            step = TransferStep.DESTINATION,
            isLoadingMetadata = true,
            destinationOrgUnit = OrgUnit("destination", "Destination"),
            isTransferFormValid = true,
        )

        assertFalse(state.canContinue)
    }

    @Test
    fun `review cannot be confirmed while transfer is submitting`() {
        assertFalse(
            TransferUiState(
                step = TransferStep.REVIEW,
                isSubmitting = true,
                destinationOrgUnit = OrgUnit("destination", "Destination"),
            ).canContinue
        )
    }

    @Test
    fun `review requires a selected destination before confirmation`() {
        assertFalse(TransferUiState(step = TransferStep.REVIEW).canContinue)
        assertTrue(
            TransferUiState(
                step = TransferStep.REVIEW,
                destinationOrgUnit = OrgUnit("destination", "Destination"),
            ).canContinue
        )
    }

    @Test
    fun `incoming students tab hides outgoing transfer actions`() {
        val state = TransferUiState(selectedTab = TransferTab.INCOMING_STUDENTS)

        assertFalse(state.showTransferActions)
        assertTrue(TransferUiState().showTransferActions)
    }

    private fun configuredState(
        selectedLearnerUids: Set<String> = emptySet(),
        isLoadingMetadata: Boolean = false,
    ) = TransferUiState(
        selectedLearnerUids = selectedLearnerUids,
        isLoadingMetadata = isLoadingMetadata,
        transferProgramStage = "stage",
        originSchoolDataElement = "origin",
        destinationSchoolDataElement = "destination",
        statusDataElement = "status",
        pendingStatusCode = "PENDING",
    )
}
