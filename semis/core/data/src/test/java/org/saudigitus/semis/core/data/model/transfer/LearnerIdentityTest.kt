package org.saudigitus.semis.core.data.model.transfer

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Test
import org.saudigitus.semis.core.data.model.SearchTeiModel

class LearnerIdentityTest {

    @Test
    fun `uses learner name and first attribute value instead of card header`() {
        val learner = learner(
            header = "STUDENT-1007",
            attributes = linkedMapOf(
                "First name" to "Amelia",
                "Surname" to "Moyo",
                "System ID" to "STUDENT-1007",
                "School name" to "Central School",
            ),
        )

        assertEquals(
            LearnerIdentity("Amelia Moyo", "Amelia"),
            learner.learnerIdentity(),
        )
    }

    @Test
    fun `keeps the first available tei attribute value`() {
        val learner = learner(
            header = "Samuel Dube",
            attributes = linkedMapOf("Guardian name" to "Maria Dube"),
        )

        assertEquals(
            LearnerIdentity("Samuel Dube", "Maria Dube"),
            learner.learnerIdentity(),
        )
    }

    private fun learner(
        header: String,
        attributes: LinkedHashMap<String, String>,
    ): SearchTeiModel = SearchTeiModel().apply {
        tei = TrackedEntityInstance.builder()
            .uid("tei-uid")
            .trackedEntityType("type")
            .organisationUnit("org-unit")
            .build()
        this.header = header
        attributeValues = LinkedHashMap(
            attributes.mapValues { (label, value) ->
                TrackedEntityAttributeValue.builder()
                    .trackedEntityAttribute(label)
                    .trackedEntityInstance("tei-uid")
                    .value(value)
                    .build()
            }
        )
    }
}
