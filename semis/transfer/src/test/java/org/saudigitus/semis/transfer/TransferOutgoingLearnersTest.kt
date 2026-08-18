package org.saudigitus.semis.transfer

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.OutgoingTeiTransfer
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import java.util.Date

class TransferOutgoingLearnersTest {

    private fun learner(uid: String) = SearchTeiModel().apply {
        tei = TrackedEntityInstance.builder().uid(uid).build()
    }

    private fun pending(teiUid: String) = OutgoingTeiTransfer(
        eventUid = "event-$teiUid",
        teiUid = teiUid,
        enrollmentUid = "enrollment-$teiUid",
        learnerName = "Learner $teiUid",
        firstAttributeValue = "",
        destinationOrgUnit = "destination",
        destinationSchoolName = "Destination School",
        effectiveDate = Date(),
    )

    @Test
    fun `learners awaiting approval are dropped from the outgoing list`() {
        val state = TransferUiState(
            learners = listOf(learner("a"), learner("b"), learner("c")),
            pendingOutgoingTransfers = listOf(pending("b")),
        )

        assertEquals(listOf("a", "c"), state.outgoingLearners.map { it.tei.uid() })
    }

    @Test
    fun `every learner stays outgoing while nothing is pending`() {
        val state = TransferUiState(learners = listOf(learner("a"), learner("b")))

        assertEquals(2, state.outgoingLearners.size)
    }

    @Test
    fun `the outgoing list empties once every learner is awaiting approval`() {
        val state = TransferUiState(
            learners = listOf(learner("a"), learner("b")),
            pendingOutgoingTransfers = listOf(pending("a"), pending("b")),
        )

        assertEquals(emptyList<String>(), state.outgoingLearners.map { it.tei.uid() })
    }

    @Test
    fun `pending outgoing tab hides the transfer actions`() {
        val state = TransferUiState(selectedTab = TransferTab.PENDING_OUTGOING)

        assertFalse(state.showTransferActions)
    }
}
