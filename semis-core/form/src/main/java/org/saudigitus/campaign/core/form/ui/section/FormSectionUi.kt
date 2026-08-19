package org.saudigitus.campaign.core.form.ui.section

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.common.ValueType
import org.saudigitus.campaign.core.designsystem.templates.SimpleScaffold
import org.saudigitus.campaign.core.designsystem.theme.FormSurfaces
import org.saudigitus.campaign.core.designsystem.theme.light_success
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.ui.component.FormInfo
import org.saudigitus.campaign.core.form.ui.component.MandatoryFieldWrapper
import org.saudigitus.campaign.core.form.ui.fields.DateField
import org.saudigitus.campaign.core.form.ui.fields.OuField
import org.saudigitus.campaign.core.form.ui.state.FormEvent
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.ui.state.FormSectionUiState
import org.saudigitus.campaign.core.form.utils.completionPercentage
import org.saudigitus.campaign.core.form.utils.firstBlockingFieldIndex
import org.saudigitus.campaign.core.utils.location.rememberCoordinateState
import org.saudigitus.campaign.core.utils.location.state.CoordinateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormSectionUi(
    modifier: Modifier = Modifier,
    state: FormSectionUiState.HasFormSection,
    onEvent: (FormEvent) -> Unit,
) {
    val visibleSections = state.formSections.filter { section ->
        section.rendered && section.formFields.any { field -> field.rendered == true }
    }
    val hasVisibleSections = visibleSections.isNotEmpty()
    val listState = rememberLazyListState()
    val scrollScope = rememberCoroutineScope()

    val animatedProgress by animateFloatAsState(
        targetValue = visibleSections.completionPercentage(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    var activeCoordinateFieldUid by rememberSaveable { mutableStateOf<String?>(null) }
    var finishedCoordinateFields by remember { mutableStateOf(emptySet<String>()) }
    var coordinateStatesByField by remember { mutableStateOf(emptyMap<String, CoordinateState>()) }

    val currentCoordinateCandidate = visibleSections.firstNotNullOfOrNull { section ->
        section.formFields
            .filter { field ->
                field.rendered == true && field.valueType == ValueType.COORDINATE
            }
            .firstOrNull { field ->
                field.uid !in finishedCoordinateFields && !field.hasValidCoordinates()
            }
            ?.let { field -> section to field }
    }
    val activeCoordinateTarget = activeCoordinateFieldUid?.let { uid ->
        visibleSections.firstNotNullOfOrNull { section ->
            section.formFields
                .firstOrNull { field ->
                    field.rendered == true &&
                        field.valueType == ValueType.COORDINATE &&
                        field.uid == uid
                }
                ?.let { field -> section to field }
        }
    }
    val activeCoordinateField = activeCoordinateTarget?.second
    val coordinateState = rememberCoordinateState(
        captureKey = activeCoordinateFieldUid,
        enabled = activeCoordinateField != null
    )
    val coordinateValue = coordinateState.toFormValue()

    LaunchedEffect(currentCoordinateCandidate?.second?.uid, activeCoordinateFieldUid) {
        if (activeCoordinateFieldUid == null) {
            activeCoordinateFieldUid = currentCoordinateCandidate?.second?.uid
        }
    }

    LaunchedEffect(
        activeCoordinateFieldUid,
        coordinateValue,
        coordinateState.accuracy,
        coordinateState.isLoading,
        coordinateState.error,
        activeCoordinateField?.value
    ) {
        val target = activeCoordinateTarget ?: return@LaunchedEffect
        val section = target.first
        val field = target.second

        coordinateStatesByField = coordinateStatesByField + (field.uid to coordinateState)

        if (coordinateValue != null && coordinateValue != field.value) {
            onEvent(FormEvent.UpdateField(section, field.uid, coordinateValue))
        }

        if (!coordinateState.isLoading && (coordinateValue != null || coordinateState.error != null)) {
            finishedCoordinateFields = finishedCoordinateFields + field.uid
            activeCoordinateFieldUid = null
        }
    }

    var hasUnfilledMandatoryFields by rememberSaveable {
        mutableStateOf(
            (state.formType == FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION ||
                state.formType == FormSectionType.NEW_ENROLLMENT) && state.orgUnit == null
        )
    }

    val showsBaseFields = state.formType == FormSectionType.NEW_ENROLLMENT ||
        state.formType == FormSectionType.EDIT_ENROLLMENT ||
        state.formType == FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION
    val pendingMandatorySection = visibleSections.firstOrNull { it.hasUnfilledMandatoryFields() }

    // The header sits inside the list, so the sections start right after it.
    val sectionsOffset = if (showsBaseFields || pendingMandatorySection != null) 1 else 0

    fun isFormValid(): Boolean {
        hasUnfilledMandatoryFields = hasBaseMandatoryFields(state) ||
            visibleSections.any { it.hasUnfilledMandatoryFields() }

        val blockingSection = visibleSections.indexOfFirst { it.firstBlockingFieldIndex() != -1 }

        if (blockingSection != -1) {
            scrollScope.launch {
                listState.animateScrollToItem(blockingSection + sectionsOffset)
            }

            return false
        }

        return !hasUnfilledMandatoryFields
    }

    SimpleScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            stringResource(R.string.form_title),
                            style = MaterialTheme.typography.titleLarge
                        )

                        if (hasVisibleSections) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                trackColor = Color.White,
                                color = light_success,
                                progress = { animatedProgress }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FormSurfaces.HeaderBlue,
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                navigationIcon = {
                    IconButton(onClick = { onEvent(FormEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .imePadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (isFormValid()) {
                            onEvent(FormEvent.ConfirmSave)
                        }
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(FormSurfaces.HeaderBlue)
                .clip(FormSurfaces.ScreenShape)
                .background(FormSurfaces.ScreenBackground)
                .then(modifier),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        ) {
            if (sectionsOffset == 1) {
                item(key = "form_header") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (showsBaseFields) {
                            MandatoryFieldWrapper(hasUnfilledMandatoryFields) {
                                OuField(
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = stringResource(R.string.administrative_area),
                                    leadingIcon = Icons.Default.PinDrop,
                                    selectedOrgUnit = state.orgUnit,
                                    program = state.program.orEmpty()
                                ) {
                                    hasUnfilledMandatoryFields = false
                                    onEvent(FormEvent.SelectedOU(it))
                                }
                            }

                            DateField(
                                isEnabled = false,
                                date = visibleSections.firstOrNull()?.registrationDate,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                onEvent(FormEvent.SelectedDate(it))
                            }
                        }

                        pendingMandatorySection?.let { section ->
                            FormInfo(
                                modifier = Modifier.fillMaxWidth(),
                                formSectionModel = section
                            )
                        }
                    }
                }
            }

            items(visibleSections, key = { it.uid }) { section ->
                FormSectionCard(
                    section = section,
                    showMandatoryError = hasUnfilledMandatoryFields,
                    coordinateStates = coordinateStatesByField,
                    onEvent = onEvent,
                )
            }
        }
    }
}

private fun CoordinateState.toFormValue(): String? {
    val currentLongitude = longitude ?: return null
    val currentLatitude = latitude ?: return null

    if (currentLongitude == 0.0 && currentLatitude == 0.0) return null

    return "[$currentLongitude,$currentLatitude]"
}

private fun hasBaseMandatoryFields(state: FormSectionUiState.HasFormSection): Boolean {
    return (
        state.formType == FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION ||
            state.formType == FormSectionType.NEW_ENROLLMENT
        ) && state.orgUnit == null
}
