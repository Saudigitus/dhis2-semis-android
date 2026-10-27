package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClassFiltersTest {

    @Test
    fun `the filters decide the fields even when the registration settings disagree`() {
        val staff = config(
            filters = listOf(filter("class", "typeOfStaff"), filter("grade", "employmentType")),
            registration = registration(grade = "yesOnly", section = "yesOnly"),
        )

        assertEquals("employmentType", staff.gradeFilterDataElement())
        assertEquals("typeOfStaff", staff.sectionFilterDataElement())
    }

    @Test
    fun `every section code names the section filter`() {
        listOf("section", "section/class", "class").forEach { code ->
            assertEquals("section", config(filters = listOf(filter(code, "section"))).sectionFilterDataElement())
        }
    }

    @Test
    fun `a program without filters falls back to the registration settings`() {
        val student = config(filters = null, registration = registration(grade = "grade", section = "class"))

        assertEquals("grade", student.gradeFilterDataElement())
        assertEquals("class", student.sectionFilterDataElement())
    }

    @Test
    fun `a blank filter data element falls back too, and nothing configured gives nothing`() {
        val blank = config(filters = listOf(filter("grade", " ")), registration = registration(grade = "grade"))

        assertEquals("grade", blank.gradeFilterDataElement())
        assertNull(config(filters = null, registration = null).gradeFilterDataElement())
        assertNull(config(filters = null, registration = null).sectionFilterDataElement())
    }

    private fun filter(code: String, dataElement: String) = DataElement(
        code = code,
        dataElement = dataElement,
        label = null,
        order = null,
        ulrParam = null,
    )

    private fun registration(grade: String? = null, section: String? = null) = Registration(
        enabled = true,
        grade = grade,
        lastUpdate = null,
        programStage = "registration",
        section = section,
    )

    private fun config(
        filters: List<DataElement>?,
        registration: Registration? = null,
    ) = SEMISConfigItem(
        absenteeism = null,
        attendance = null,
        defaults = null,
        filters = filters?.let { Filters(dataElements = it) },
        finalResult = null,
        key = null,
        lastUpdate = null,
        performance = null,
        program = null,
        reenroll = null,
        registration = registration,
        socioEconomics = null,
        trackedEntityType = null,
        transfer = null,
    )
}
