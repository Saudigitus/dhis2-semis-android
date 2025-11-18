package org.saudigitus.semis.core.form.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.getAttendanceStatusColor
import org.saudigitus.semis.core.form.data.repository.AttendanceOptionRepository
import javax.inject.Inject

class AttendanceOptionRepositoryImpl @Inject constructor(
    val optionRepository: OptionRepository,
    val appConfigRepository: AppConfigRepository
) : AttendanceOptionRepository {
    override suspend fun getAttendanceStatusOptions(program: String) =
        withContext(Dispatchers.IO) {
        val config = appConfigRepository.getAppConfig(program)
        val optionsCode = config?.attendance?.statusOptions
            ?.mapNotNull { it?.code?.trim() }
            ?.filterNot { it.isEmpty() } ?: emptyList()

        val options =
            optionRepository.getOptionsByCode(
                config?.attendance?.status.orEmpty(),
                optionsCode
            )

        options.mapNotNull {
            val status = config?.attendance?.statusOptions?.find { status ->
                status?.code == it.code()
            }

            if (status == null) return@mapNotNull null

            AttendanceButtonModel(
                key = status.key.orEmpty(),
                code = it.code(),
                name = it.displayName(),
                dataElement = config.attendance?.status.orEmpty(),
                icon = UiDefaults.dynamicIcons(status.icon.orEmpty()),
                iconName = status.icon.orEmpty(),
                color = getAttendanceStatusColor(
                    status.key.orEmpty(),
                    status.color.orEmpty()
                ),
                order = it.sortOrder() ?: -1,
            )
        }
    }
}