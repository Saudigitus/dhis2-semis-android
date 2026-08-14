package org.saudigitus.semis.core.form.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.core.data.model.app_config.isEnabledAndConfigured
import org.saudigitus.semis.core.data.repository.AppConfigRepository
import org.saudigitus.semis.core.data.repository.OptionRepository
import org.saudigitus.semis.core.designsystem.attendance.model.AttendanceButtonModel
import org.saudigitus.semis.core.designsystem.utils.UiDefaults
import org.saudigitus.semis.core.designsystem.utils.UiDefaults.getAttendanceStatusColor
import org.saudigitus.semis.core.form.data.repository.AttendanceOptionRepository
import javax.inject.Inject

internal class AttendanceOptionRepositoryImpl @Inject constructor(
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

            val useAttendanceStatusFlow = config?.attendance?.attendanceStatus
                .isEnabledAndConfigured()
            val configuredOptions = options.mapNotNull {
                val status = config?.attendance?.statusOptions?.find { status ->
                    status?.code == it.code()
                }
                val isPresent = status?.key.equals(PRESENT_KEY, ignoreCase = true) ||
                    status?.code.equals(PRESENT_KEY, ignoreCase = true)

                if (status == null || useAttendanceStatusFlow && isPresent) {
                    return@mapNotNull null
                }

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
                    isAbsence = status.key.equals(ABSENT_KEY, ignoreCase = true),
                )
            }

            if (useAttendanceStatusFlow) {
                listOf(
                    AttendanceButtonModel(
                        key = PRESENT_KEY,
                        name = PRESENT_KEY.replaceFirstChar { it.uppercase() },
                        dataElement = config?.attendance?.status.orEmpty(),
                        iconName = PRESENT_ICON,
                        color = getAttendanceStatusColor(PRESENT_KEY),
                        order = -1,
                    )
                ) + configuredOptions
            } else {
                configuredOptions
            }
        }

    private companion object {
        const val PRESENT_KEY = "present"
        const val ABSENT_KEY = "absent"
        const val PRESENT_ICON = "correct_blue_fill"
    }
}
