package org.saudigitus.semis.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.saudigitus.semis.core.data.model.transfer.IncomingTeiTransfer
import org.saudigitus.semis.transfer.state.TransferUiState
import java.util.Date

class TransferIncomingSelectionTest {

    private fun incoming(eventUid: String) = IncomingTeiTransfer(
        eventUid = eventUid,
        teiUid = "tei-$eventUid",
        enrollmentUid = "enrollment-$eventUid",
        learnerName = "Learner $eventUid",
        firstAttributeValue = "",
        originOrgUnit = "origin",
        originSchoolName = "Origin School",
        destinationOrgUnit = "destination",
        effectiveDate = Date(),
    )

    private val state = TransferUiState(
        incomingTransfers = listOf(incoming("a"), incoming("b"), incoming("c")),
    )

    @Test
    fun `nothing is selected until a request is picked`() {
        assertFalse(state.hasIncomingSelection)
        assertEquals(emptyList<String>(), state.selectedIncomingTransfers.map { it.eventUid })
    }

    @Test
    fun `only the picked requests take part in a bulk decision`() {
        val selected = state.copy(selectedIncomingEventUids = setOf("a", "c"))

        assertTrue(selected.hasIncomingSelection)
        assertEquals(
            listOf("a", "c"),
            selected.selectedIncomingTransfers.map { it.eventUid },
        )
    }

    @Test
    fun `a selection that no longer has a request is ignored`() {
        val selected = state.copy(selectedIncomingEventUids = setOf("a", "gone"))

        assertEquals(listOf("a"), selected.selectedIncomingTransfers.map { it.eventUid })
    }
}
