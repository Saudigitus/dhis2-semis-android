package org.dhis2.data.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionCodeTest {

    @Test
    fun `a higher version code offers the update`() {
        assertTrue("160".isNewerVersionCodeThan(159))
        assertTrue(" 161 ".isNewerVersionCodeThan(160))
    }

    @Test
    fun `the same or a lower version code offers nothing`() {
        assertFalse("159".isNewerVersionCodeThan(159))
        assertFalse("158".isNewerVersionCodeThan(159))
    }

    @Test
    fun `a value that is not a version code offers nothing`() {
        assertFalse("2.1.0-rc4".isNewerVersionCodeThan(159))
        assertFalse("".isNewerVersionCodeThan(159))
        assertFalse(null.isNewerVersionCodeThan(159))
    }
}
