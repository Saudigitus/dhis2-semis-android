package org.saudigitus.semis.enrollment.ui.form.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.saudigitus.campaign.core.form.R as FormRes
import org.saudigitus.campaign.core.form.ui.component.FormStepHeader
import org.saudigitus.campaign.core.form.ui.state.FormStepProgress
import org.saudigitus.semis.core.data.model.transfer.learnerIdentity
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials
import org.saudigitus.semis.core.designsystem.components.buttons.PrimaryActionButton
import org.saudigitus.semis.core.designsystem.components.cards.DetailRows
import org.saudigitus.semis.core.designsystem.components.cards.DetailSectionCard
import org.saudigitus.semis.core.designsystem.components.cards.LearnerCard
import org.saudigitus.semis.core.designsystem.theme.SemisAccent
import org.saudigitus.semis.core.designsystem.theme.SemisPalette
import org.saudigitus.semis.enrollment.R

/**
 * Closes the enrollment as its final step, summarising what was recorded.
 *
 * It carries the header and markers of the steps before it, so finishing reads as arriving at the
 * end of the flow rather than being dropped somewhere else. What is shown here is what the user
 * would otherwise have to go and look up: who the learner now is, read exactly as the listings
 * read them, and what the registration stage recorded about them.
 *
 * @param learnerAttributes everything captured about the learner, in configured order.
 * @param registrationDetails what the registration stage recorded.
 * @param teiUid the record just created, which decides the colour of its avatar.
 * @param stepCount how many steps the user filled in, this summary excluded.
 * @param onAddAnother starts another enrollment without leaving the flow.
 * @param onBackToList returns to the listing the enrollment was started from.
 */
@Composable
internal fun EnrollmentSummaryScreen(
    learnerAttributes: List<Pair<String, String>>,
    registrationDetails: List<Pair<String, String>>,
    teiUid: String,
    stepCount: Int,
    onAddAnother: () -> Unit,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SemisPalette.ScreenBackground),
    ) {
        FormStepHeader(
            title = stringResource(FormRes.string.form_title),
            stepName = stringResource(R.string.enrollment_complete_step),
            // The flow has the same steps it always had; reaching here means every one of them is
            // behind the user, so they all read as done.
            progress = FormStepProgress(stepNumber = stepCount + 1, stepCount = stepCount),
            modifier = Modifier.background(SemisPalette.HeaderBlue).statusBarsPadding(),
            contentColor = SemisPalette.OnHeaderPrimary,
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SemisAccent.Green,
                    modifier = Modifier.size(56.dp),
                )
            }

            item {
                Text(
                    text = stringResource(R.string.enrollment_complete_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }

            if (learnerAttributes.isNotEmpty()) {
                item {
                    // The same reading of a learner the listings use, so the person just enrolled
                    // is recognised here exactly as they will be on the list.
                    val identity = learnerIdentity(learnerAttributes) {
                        learnerAttributes.firstOrNull()?.second.orEmpty()
                    }

                    LearnerCard(
                        name = identity.name,
                        supportingText = identity.firstAttributeValue,
                        avatarColor = AvatarInitials.colorFor(teiUid),
                        containerColor = SemisPalette.CardSurface,
                    )
                }
            }

            if (registrationDetails.isNotEmpty()) {
                item {
                    DetailSectionCard(
                        icon = Icons.Outlined.School,
                        title = stringResource(R.string.enrollment_summary_registration),
                        description = stringResource(R.string.enrollment_summary_registration_hint),
                    ) {
                        DetailRows(rows = registrationDetails)
                    }
                }
            }
        }

        SummaryActions(onAddAnother = onAddAnother, onBackToList = onBackToList)
    }
}

/**
 * The two ways out, offered where every other screen of the flow offers its action.
 *
 * Registering tends to happen in a sitting, so carrying on with the next learner leads, and
 * returning to the listing closes the sitting.
 */
@Composable
private fun SummaryActions(
    onAddAnother: () -> Unit,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SemisPalette.CardSurface,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.enrollment_add_another),
                modifier = Modifier.fillMaxWidth(),
                imageVector = Icons.Rounded.Add,
                onClick = onAddAnother,
            )

            PrimaryActionButton(
                text = stringResource(R.string.enrollment_back_to_list),
                modifier = Modifier.fillMaxWidth(),
                imageVector = Icons.AutoMirrored.Filled.ListAlt,
                containerColor = SemisPalette.CardSurface,
                contentColor = SemisPalette.ActionBlue,
                onClick = onBackToList,
            )
        }
    }
}
