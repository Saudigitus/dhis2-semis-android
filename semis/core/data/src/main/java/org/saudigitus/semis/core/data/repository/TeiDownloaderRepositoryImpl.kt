package org.saudigitus.semis.core.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.saudigitus.semis.core.data.R
import org.saudigitus.semis.core.utils.Result
import javax.inject.Inject

class TeiDownloaderRepositoryImpl
@Inject constructor(
    val d2: D2,
    val networkUtils: NetworkUtils,
    val resourceManager: ResourceManager
) : TeiDownloaderRepository {

    override suspend fun downloadTei(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>
    ) = withContext(Dispatchers.IO) {
        validateInputs(ou, program, dataElementIds, dataValues)

        try {
            val teiUids = searchTrackedEntityInstances(ou, program, dataElementIds, dataValues)

            if (teiUids.isEmpty()) {
                Result.Failure(
                    Exception(
                        resourceManager
                            .getString(R.string.tei_not_found)
                    )
                )
            }

            async {
                d2.trackedEntityModule().trackedEntityInstanceDownloader()
                    .byUid().`in`(teiUids)
                    .byProgramUid(program)
                    .blockingDownload()
            }.await()

            val downloadedTeis = d2.trackedEntityModule().trackedEntityInstances()
                .byUid().`in`(teiUids)
                .blockingIsEmpty()

            if (downloadedTeis) {
                Result.Failure(
                    IllegalStateException(
                        resourceManager
                            .getString(R.string.download_completed_not_found)
                    )
                )
            }

            Result.Success(true)
        } catch (e: Exception) {
            Result.Failure(
                IllegalStateException(
                    resourceManager.getString(
                        R.string.download_failed,
                        e.message.orEmpty()
                    ), e
                )
            )
        }
    }

    private fun searchTrackedEntityInstances(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>
    ): List<String> {
        if (!networkUtils.isOnline()) {
            throw IllegalStateException(
                resourceManager
                    .getString(R.string.error_no_internet_connection)
            )
        }

        try {
            var repository = d2.trackedEntityModule().trackedEntitySearch()
                .onlineFirst()
                .allowOnlineCache()
                .eq(true)
                .byOrgUnits().eq(ou)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq(program)

            dataElementIds.zip(dataValues).forEach { (elementId, value) ->
                repository = repository.byDataValue(elementId).eq(value)
            }

            return repository.blockingGet()
                .map { it.uid() }
                .distinct()
        } catch (e: Exception) {
            throw IllegalStateException(
                resourceManager.getString(
                    R.string.failed_to_search_tei,
                    e.message.orEmpty()
                ), e
            )
        }
    }

    private fun validateInputs(
        ou: String,
        program: String,
        dataElementIds: List<String>,
        dataValues: List<String>
    ) {
        require(ou.isNotBlank()) { "Organization Unit UID cannot be empty" }
        require(program.isNotBlank()) { "Program UID cannot be empty" }
        require(dataElementIds.size == dataValues.size) {
            "Data element IDs and values must have the same size"
        }
        require(dataElementIds.isNotEmpty()) { "At least one data element ID is required" }
        require(dataValues.isNotEmpty()) { "At least one data value is required" }
    }
}