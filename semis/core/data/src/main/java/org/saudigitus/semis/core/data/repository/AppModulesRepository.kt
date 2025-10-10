package org.saudigitus.semis.core.data.repository

import org.saudigitus.semis.core.data.model.Module

interface AppModulesRepository {
    suspend fun getModules(program: String): List<Module>
}
