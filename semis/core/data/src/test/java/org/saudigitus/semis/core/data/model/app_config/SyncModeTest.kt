package org.saudigitus.semis.core.data.model.app_config

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncModeTest {

    @Test
    fun `each configured mode is read`() {
        assertEquals(SyncMode.DEFAULT, syncModeOf("DEFAULT"))
        assertEquals(SyncMode.PROMPT, syncModeOf("PROMPT"))
        assertEquals(SyncMode.AUTO, syncModeOf("AUTO"))
    }

    @Test
    fun `the mode is read whatever case it was written in`() {
        assertEquals(SyncMode.AUTO, syncModeOf("auto"))
        assertEquals(SyncMode.PROMPT, syncModeOf("Prompt"))
    }

    @Test
    fun `surrounding blanks do not hide the mode`() {
        assertEquals(SyncMode.AUTO, syncModeOf("  AUTO  "))
    }

    @Test
    fun `a deployment that said nothing gets the default`() {
        assertEquals(SyncMode.DEFAULT, syncModeOf(null))
        assertEquals(SyncMode.DEFAULT, syncModeOf(""))
        assertEquals(SyncMode.DEFAULT, syncModeOf("   "))
    }

    @Test
    fun `a mode this version does not know gets the default`() {
        assertEquals(SyncMode.DEFAULT, syncModeOf("BACKGROUND"))
        assertEquals(SyncMode.DEFAULT, syncModeOf("1"))
    }
}
