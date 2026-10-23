package org.saudigitus.semis.core.data.model.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OwnershipTransferTest {

    @Test
    fun `2_42 and later are sent orgUnit, earlier servers ou`() {
        assertEquals("orgUnit", ownershipOrgUnitParameter(serverIsAtLeast242 = true))
        assertEquals("ou", ownershipOrgUnitParameter(serverIsAtLeast242 = false))
    }

    @Test
    fun `the owner is read for the program asked`() {
        val json = """
            {"programOwners":[
              {"orgUnit":"D47G3eqPvL9","trackedEntity":"Dn331WX5urx","program":"other"},
              {"orgUnit":"qRFsqNP8ooP","trackedEntity":"Dn331WX5urx","program":"wQaiD2V27Dp"}
            ]}
        """.trimIndent()

        assertEquals("qRFsqNP8ooP", programOwnerOf(json, "wQaiD2V27Dp"))
        assertNull(programOwnerOf(json, "missing"))
    }

    @Test
    fun `an answer without owners or not json has no owner`() {
        assertNull(programOwnerOf("{}", "wQaiD2V27Dp"))
        assertNull(programOwnerOf("<html>Bad Request</html>", "wQaiD2V27Dp"))
    }

    @Test
    fun `the server message is read from an http error`() {
        val error = "Client request(PUT https://x/api/tracker/ownership/transfer) invalid: " +
            "400 . Text: \"{\"httpStatus\":\"Bad Request\",\"httpStatusCode\":400," +
            "\"status\":\"ERROR\",\"message\":\"Required parameter 'orgUnit' is not present\"}\""

        assertEquals("Required parameter 'orgUnit' is not present", serverMessageOf(error))
    }

    @Test
    fun `an error without a message has none`() {
        assertNull(serverMessageOf("<!doctype html><title>HTTP Status 400</title>"))
        assertNull(serverMessageOf(null))
    }
}
