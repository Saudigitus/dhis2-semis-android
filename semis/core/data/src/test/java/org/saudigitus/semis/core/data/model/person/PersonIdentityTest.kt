package org.saudigitus.semis.core.data.model.person

import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonIdentityTest {

    @Test
    fun `names a learner by the text attributes shown in the list and identifies them by the unique one`() {
        val identity = personIdentity(
            listOf(
                attribute("sid", "System ID", "2026-00000171", unique = true),
                attribute("iid", "Learner Internal ID", "2026-bE039635", unique = true, displayInList = false),
                attribute("fn", "First name", "Alfara"),
                attribute("sn", "Surname", "Bet"),
                attribute("ge", "Gender", "Female", displayInList = false, hasOptionSet = true),
                attribute("gn", "Guardian's Name", "Maria Bet", displayInList = false),
            ),
        )

        assertEquals("Alfara Bet", identity.name)
        assertEquals("2026-00000171", identity.identifier)
        assertEquals(setOf("sid", "fn", "sn"), identity.usedAttributes)
    }

    @Test
    fun `reads the name whatever the labels say`() {
        val identity = personIdentity(
            listOf(
                attribute("pr", "Prénom", "Awa"),
                attribute("no", "Nom", "Diallo"),
                attribute("mat", "Matricule", "MAT-12", unique = true),
            ),
        )

        assertEquals("Awa Diallo", identity.name)
        assertEquals("MAT-12", identity.identifier)
    }

    @Test
    fun `identifies a staff member by the first unique attribute when none is shown in the list`() {
        val identity = personIdentity(
            listOf(
                attribute("emp", "Employment number", "EMP-00412", unique = true, displayInList = false),
                attribute("pay", "Payroll Number", "PR-2231", displayInList = false),
                attribute("fn", "First name", "Mariama"),
                attribute("sn", "Surname", "Jallow"),
            ),
        )

        assertEquals("Mariama Jallow", identity.name)
        assertEquals("EMP-00412", identity.identifier)
    }

    @Test
    fun `leaves out option set and non text attributes from the name`() {
        val identity = personIdentity(
            listOf(
                attribute("fn", "First name", "Lamin"),
                attribute("ge", "Gender", "Male", hasOptionSet = true),
                attribute("dob", "Date of birth", "02/04/1989", valueType = ValueType.DATE),
            ),
        )

        assertEquals("Lamin", identity.name)
    }

    @Test
    fun `falls back to the first value shown in the list, then to the identifier, never to an id`() {
        assertEquals(
            "Grade 1",
            personIdentity(
                listOf(attribute("gr", "Grade", "Grade 1", hasOptionSet = true)),
            ).name,
        )
        assertEquals(
            "EMP-00412",
            personIdentity(
                listOf(attribute("emp", "Employment number", "EMP-00412", unique = true, displayInList = false)),
            ).name,
        )
        assertEquals("", personIdentity(emptyList()).name)
    }

    @Test
    fun `skips attributes without a value`() {
        val identity = personIdentity(
            listOf(
                attribute("sid", "System ID", "", unique = true),
                attribute("iid", "Internal ID", "2026-K7000000", unique = true, displayInList = false),
                attribute("fn", "First name", "QA RC Ana"),
                attribute("sn", "Surname", " "),
            ),
        )

        assertEquals("QA RC Ana", identity.name)
        assertEquals("2026-K7000000", identity.identifier)
    }

    @Test
    fun `takes the first image as the photo and the coloured options as tags`() {
        val identity = personIdentity(
            listOf(
                attribute("fn", "First name", "Alfara"),
                attribute("ph", "Photo", "/files/photo.png", displayInList = false, valueType = ValueType.IMAGE),
                attribute(
                    "ge",
                    "Gender",
                    "Female",
                    hasOptionSet = true,
                    tag = PersonTag("Female", "#8e44ad"),
                ),
                attribute("na", "Nationality", "Gambian", hasOptionSet = true),
            ),
        )

        assertEquals("/files/photo.png", identity.photoPath)
        assertEquals(listOf(PersonTag("Female", "#8e44ad")), identity.tags)
    }

    @Test
    fun `has no photo without an image attribute`() {
        assertNull(personIdentity(listOf(attribute("fn", "First name", "Alfara"))).photoPath)
    }

    private fun attribute(
        uid: String,
        label: String,
        value: String,
        unique: Boolean = false,
        displayInList: Boolean = true,
        hasOptionSet: Boolean = false,
        valueType: ValueType = ValueType.TEXT,
        tag: PersonTag? = null,
    ) = PersonAttribute(
        uid = uid,
        label = label,
        value = value,
        valueType = valueType,
        unique = unique,
        displayInList = displayInList,
        hasOptionSet = hasOptionSet,
        tag = tag,
    )
}
