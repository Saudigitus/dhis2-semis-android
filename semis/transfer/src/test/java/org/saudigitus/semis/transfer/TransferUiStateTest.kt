package org.saudigitus.semis.transfer

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.OrgUnit
import org.saudigitus.semis.core.data.model.SearchTeiModel
import org.saudigitus.semis.core.data.model.transfer.TeiTransfer
import org.saudigitus.semis.core.data.model.transfer.TransferStatus
import org.saudigitus.semis.transfer.model.TransferStatusFilter
import org.saudigitus.semis.transfer.model.TransferStep
import org.saudigitus.semis.transfer.model.TransferTab
import org.saudigitus.semis.transfer.state.TransferUiState
import java.util.Date

class TransferUiStateTest {

    @Test
    fun `a record awaiting a decision cannot be part of a second request`() {
        val state = TransferUiState(
            records = listOf(record("a"), record("b"), record("c")),
            outgoingTransfers = listOf(transfer("b", TransferStatus.PENDING)),
        )

        assertEquals(listOf("a", "c"), state.availableRecords.map { it.tei.uid() })
    }

    @Test
    fun `a rejected record can be requested again`() {
        val state = TransferUiState(
            records = listOf(record("a"), record("b")),
            outgoingTransfers = listOf(
                transfer("b", TransferStatus.REJECTED),
                transfer("a", TransferStatus.APPROVED),
            ),
        )

        assertEquals(listOf("a", "b"), state.availableRecords.map { it.tei.uid() })
    }

    @Test
    fun `each tab reads its own list`() {
        val state = TransferUiState(
            outgoingTransfers = listOf(transfer("a", TransferStatus.PENDING)),
            incomingTransfers = listOf(
                transfer("b", TransferStatus.PENDING),
                transfer("c", TransferStatus.APPROVED),
            ),
        )

        assertEquals(1, state.tabTransfers.size)
        assertEquals(2, state.copy(selectedTab = TransferTab.INCOMING).tabTransfers.size)
    }

    @Test
    fun `the status chips narrow the visible list`() {
        val state = TransferUiState(
            outgoingTransfers = listOf(
                transfer("a", TransferStatus.PENDING),
                transfer("b", TransferStatus.APPROVED),
                transfer("c", TransferStatus.PENDING),
            ),
            statusFilter = TransferStatusFilter.PENDING,
        )

        assertEquals(listOf("a", "c"), state.visibleTransfers.map { it.teiUid })
        assertEquals(3, state.copy(statusFilter = null).visibleTransfers.size)
    }

    @Test
    fun `a request carrying an unknown status is only reachable unfiltered`() {
        val state = TransferUiState(
            outgoingTransfers = listOf(transfer("a", TransferStatus.UNKNOWN)),
        )

        assertEquals(1, state.visibleTransfers.size)
        TransferStatusFilter.entries.forEach { filter ->
            assertTrue(state.copy(statusFilter = filter).visibleTransfers.isEmpty())
        }
    }

    @Test
    fun `only the destination school is offered a decision`() {
        val state = TransferUiState()

        assertFalse(state.showsDecisionActions)
        assertTrue(state.copy(selectedTab = TransferTab.INCOMING).showsDecisionActions)
    }

    @Test
    fun `each step states what it needs before the request can move on`() {
        val entities = TransferUiState(requestStep = TransferStep.ENTITIES)
        assertFalse(entities.canContinue)
        assertTrue(entities.copy(selectedRecordUids = setOf("a")).canContinue)

        val destination = TransferUiState(requestStep = TransferStep.DESTINATION)
        assertFalse(destination.canContinue)
        assertFalse(destination.copy(isRequestFormValid = true).canContinue)
        assertTrue(
            destination.copy(
                isRequestFormValid = true,
                destinationOrgUnit = orgUnit,
            ).canContinue,
        )

        val review = TransferUiState(
            requestStep = TransferStep.REVIEW,
            destinationOrgUnit = orgUnit,
        )
        assertTrue(review.canContinue)
        assertFalse(review.copy(isSubmitting = true).canContinue)
    }

    @Test
    fun `the lists are only shown while no request is being raised`() {
        assertFalse(TransferUiState().isRequesting)
        assertTrue(TransferUiState(requestStep = TransferStep.ENTITIES).isRequesting)
    }

    private val orgUnit = OrgUnit(uid = "destination", displayName = "Destination School")

    private fun record(uid: String) = SearchTeiModel().apply {
        tei = TrackedEntityInstance.builder().uid(uid).build()
    }

    private fun transfer(teiUid: String, status: TransferStatus) = TeiTransfer(
        eventUid = "event-$teiUid",
        teiUid = teiUid,
        enrollmentUid = "enrollment-$teiUid",
        recordName = "Record $teiUid",
        firstAttributeValue = "",
        originOrgUnit = "origin",
        originSchoolName = "Origin School",
        destinationOrgUnit = "destination",
        destinationSchoolName = "Destination School",
        grade = "Grade 1",
        reason = "Change of residence",
        status = status,
        requestedAt = Date(),
    )
}
