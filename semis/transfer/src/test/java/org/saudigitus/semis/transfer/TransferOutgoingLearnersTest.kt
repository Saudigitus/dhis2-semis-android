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

    private fun transfer(
        teiUid: String,
        statusCode: String = "PENDING",
        isPending: Boolean = true,
    ) = OutgoingTeiTransfer(
        eventUid = "event-$teiUid",
        teiUid = teiUid,
        enrollmentUid = "enrollment-$teiUid",
        learnerName = "Learner $teiUid",
        firstAttributeValue = "",
        destinationOrgUnit = "destination",
        destinationSchoolName = "Destination School",
        statusCode = statusCode,
        isPending = isPending,
        effectiveDate = Date(),
    )

    @Test
    fun `a learner holding a transfer event leaves the outgoing list`() {
        val state = TransferUiState(
            learners = listOf(learner("a"), learner("b"), learner("c")),
            outgoingTransfers = listOf(transfer("b")),
        )

        assertEquals(listOf("a", "c"), state.outgoingLearners.map { it.tei.uid() })
    }

    @Test
    fun `a transfer that is no longer pending keeps its learner off the outgoing list`() {
        val state = TransferUiState(
            learners = listOf(learner("a"), learner("b")),
            outgoingTransfers = listOf(
                transfer("b", statusCode = "APPROVED", isPending = false),
            ),
        )

        assertEquals(listOf("a"), state.outgoingLearners.map { it.tei.uid() })
    }

    @Test
    fun `only pending requests are listed under pending outgoing`() {
        val state = TransferUiState(
            learners = listOf(learner("a"), learner("b"), learner("c")),
            outgoingTransfers = listOf(
                transfer("a"),
                transfer("b", statusCode = "APPROVED", isPending = false),
                transfer("c", statusCode = "REJECTED", isPending = false),
            ),
        )

        assertEquals(listOf("a"), state.pendingOutgoingTransfers.map { it.teiUid })
    }

    @Test
    fun `a settled transfer leaves the learner out of both tabs`() {
        val state = TransferUiState(
            learners = listOf(learner("a")),
            outgoingTransfers = listOf(
                transfer("a", statusCode = "APPROVED", isPending = false),
            ),
        )

        assertEquals(emptyList<String>(), state.outgoingLearners.map { it.tei.uid() })
        assertEquals(emptyList<String>(), state.pendingOutgoingTransfers.map { it.teiUid })
    }

    @Test
    fun `every learner stays outgoing while nothing was transferred`() {
        val state = TransferUiState(learners = listOf(learner("a"), learner("b")))

        assertEquals(2, state.outgoingLearners.size)
    }

    @Test
    fun `pending outgoing tab hides the transfer actions`() {
        assertFalse(TransferUiState(selectedTab = TransferTab.PENDING_OUTGOING).showTransferActions)
    }
}
