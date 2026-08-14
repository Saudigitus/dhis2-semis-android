package org.saudigitus.campaign.core.form.ui.section

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.designsystem.templates.SimpleScaffold
import org.saudigitus.campaign.core.designsystem.theme.light_success
import org.saudigitus.campaign.core.form.R
import org.saudigitus.campaign.core.form.ui.FormFieldItem
import org.saudigitus.campaign.core.form.ui.component.FormInfo
import org.saudigitus.campaign.core.form.ui.component.MandatoryFieldWrapper
import org.saudigitus.campaign.core.form.ui.fields.DateField
import org.saudigitus.campaign.core.form.ui.fields.OuField
import org.saudigitus.campaign.core.form.ui.state.FormEvent
import org.saudigitus.campaign.core.form.ui.state.FormSectionType
import org.saudigitus.campaign.core.form.ui.state.FormSectionUiState
import org.saudigitus.campaign.core.form.utils.checkUnfilledMandatoryFields
import org.saudigitus.campaign.core.form.utils.completionPercentage
import org.saudigitus.campaign.core.form.utils.firstBlockingFieldIndex
import org.saudigitus.campaign.core.utils.IdGenerator
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
    val pagerState = rememberPagerState { maxOf(visibleSections.size, 1) }
    val scrollScope = rememberCoroutineScope()
    val lazyListState = remember(pagerState.pageCount) {
        List(pagerState.pageCount) { LazyListState() }
    }
    val currentPage = if (hasVisibleSections) {
        pagerState.currentPage.coerceIn(0, visibleSections.lastIndex)
    } else {
        0
    }

    val animatedProgress by animateFloatAsState(
        targetValue = visibleSections.completionPercentage(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

    var activeCoordinateFieldUid by rememberSaveable { mutableStateOf<String?>(null) }
    var finishedCoordinateFields by remember { mutableStateOf(emptySet<String>()) }
    var coordinateStatesByField by remember { mutableStateOf(emptyMap<String, CoordinateState>()) }

    val currentCoordinateCandidate = visibleSections.getOrNull(currentPage)?.let { section ->
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

    fun isCurrentPageValid(): Boolean {
        hasUnfilledMandatoryFields = hasBaseMandatoryFields(state) ||
            (hasVisibleSections && visibleSections[currentPage].hasUnfilledMandatoryFields())

        val firstBlockingFieldIndex = visibleSections
            .getOrNull(currentPage)
            ?.firstBlockingFieldIndex()
            ?: -1

        if (firstBlockingFieldIndex != -1) {
            scrollScope.launch {
                lazyListState[currentPage].animateScrollToItem(firstBlockingFieldIndex)
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
                        if (hasVisibleSections) {
                            Text(
                                visibleSections[currentPage].name.orEmpty(),
                                style = MaterialTheme.typography.titleLarge
                            )
                            visibleSections[currentPage].description?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.titleMedium,
                                    softWrap = true,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (hasVisibleSections) {
                                Text(
                                    "${currentPage + 1} of ${visibleSections.size}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    trackColor = Color.White,
                                    color = light_success,
                                    progress = { animatedProgress }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor.Primary,
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
                if (pagerState.pageCount == 1) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (isCurrentPageValid()) {
                                onEvent(FormEvent.ConfirmSave)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                } else {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = pagerState.canScrollBackward,
                        onClick = {
                            if (pagerState.canScrollBackward) {
                                scrollScope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.previous))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val isValid = isCurrentPageValid()

                            if (pagerState.canScrollForward && isValid) {
                                scrollScope.launch {
                                    pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                                }
                            } else if (isValid) {
                                onEvent(FormEvent.ConfirmSave)
                            }
                        }
                    ) {
                        Text(
                            text = if (pagerState.canScrollForward) {
                                stringResource(R.string.next)
                            } else stringResource(R.string.save)
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        Column(
            modifier = Modifier
                .then(modifier)
        ) {
            if (
                state.formType == FormSectionType.NEW_ENROLLMENT ||
                state.formType == FormSectionType.EDIT_ENROLLMENT ||
                state.formType == FormSectionType.NEW_EVENT_WITHOUT_REGISTRATION
            ) {
                MandatoryFieldWrapper(hasUnfilledMandatoryFields) {
                    OuField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                ) {
                    onEvent(FormEvent.SelectedDate(it))
                }
            }

            if (hasVisibleSections && visibleSections.checkUnfilledMandatoryFields()) {
                FormInfo(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    formSectionModel = visibleSections[currentPage]
                )
            }

            if (hasVisibleSections) {
                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    userScrollEnabled = false,
                    verticalAlignment = Alignment.Top,
                    key = {
                        visibleSections.getOrNull(it)?.uid
                            ?: IdGenerator.generateDhis2PatternId()
                    }
                ) { page ->
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        state = lazyListState[page],
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        items(
                            visibleSections[page].formFields.filter { it.rendered == true },
                            key = { it.uid }
                        ) { field ->
                            if (field.mandatory == true) {
                                MandatoryFieldWrapper(hasUnfilledMandatoryFields) {
                                    FormFieldItem(
                                        modifier = Modifier.fillMaxWidth(),
                                        field = field,
                                        enabled = field.enabled,
                                        coordinateState = coordinateStatesByField[field.uid],
                                        onValueChange = { value ->
                                            onEvent(
                                                FormEvent.UpdateField(
                                                    visibleSections[page],
                                                    field.uid,
                                                    value
                                                )
                                            )
                                        },
                                        onQuery = { fieldModel, query ->
                                            onEvent(
                                                FormEvent.SearchFieldQuery(
                                                    visibleSections[page],
                                                    fieldModel.uid,
                                                    query
                                                )
                                            )
                                        }
                                    )
                                }
                            } else {
                                FormFieldItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    field = field,
                                    enabled = field.enabled,
                                    coordinateState = coordinateStatesByField[field.uid],
                                    onValueChange = { value ->
                                        onEvent(
                                            FormEvent.UpdateField(
                                                visibleSections[page],
                                                field.uid,
                                                value
                                            )
                                        )
                                    },
                                    onQuery = { fieldModel, query ->
                                        onEvent(
                                            FormEvent.SearchFieldQuery(
                                                visibleSections[page],
                                                fieldModel.uid,
                                                query
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
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
