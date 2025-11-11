package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.theme.colorPrimary


@Immutable
data class FilterDetailsState(
    val academicYear: String = "Academic Year",
    val orgUnit: String = "School",
    val grade: String? = null,
    val section: String? = null,
    val count: Int = 0,
    val enable: Boolean = true,
    val enableCounter: Boolean = true,
    val enableBulk: Boolean = false
)


@Composable
fun FilterDetails(
    modifier: Modifier = Modifier,
    state: FilterDetailsState,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: CardColors = CardDefaults.cardColors(containerColor = Color.White),
    onBulk: (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Card(
        shape = shape,
        colors = colors,
        modifier = modifier.clickable(enabled = state.enable, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    tint = colorPrimary,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    val hasGradeOrSection =
                        !state.grade.isNullOrEmpty() || !state.section.isNullOrEmpty()

                    if (hasGradeOrSection) {
                        val gradeLabel = listOfNotNull(state.grade, state.section)
                            .filter { it.isNotEmpty() }
                            .joinToString(separator = ", ")

                        Text(
                            text = gradeLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${state.academicYear} | ${state.orgUnit}",
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (state.enableCounter) {
                ShowCount(count = state.count)
            }
            Spacer(Modifier.width(10.dp))
            if (onBulk != null) {
                IconButton(
                    onClick = onBulk,
                    enabled = state.enableBulk,
                    modifier = Modifier
                        .border(
                            width = (0.15).dp,
                            color = Color.LightGray,
                            shape = RoundedCornerShape(100.dp)
                        )
                        .size(45.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Rocket,
                        contentDescription = null,
                        tint = colorPrimary
                    )
                }
            }
        }
    }
}