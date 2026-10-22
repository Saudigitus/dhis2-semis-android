package org.saudigitus.semis.core.designsystem.components.cards

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.saudigitus.semis.core.data.model.person.PersonTag
import org.saudigitus.semis.core.designsystem.R
import org.saudigitus.semis.core.designsystem.components.avatar.AvatarInitials

/**
 * List card for a person: photo or initials avatar, name with an optional supporting line and the
 * person's coloured tags, a trailing slot for the module action and an optional area rendered
 * under the row.
 *
 * A blank [name] reads as "No name" rather than as an empty title: nothing was captured that names
 * the person, and the card says so.
 */
@Composable
fun LearnerCard(
    name: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    photoPath: String? = null,
    tags: List<PersonTag> = emptyList(),
    selected: Boolean = false,
    avatarColor: Color = MaterialTheme.colorScheme.primary,
    avatarSize: Dp = 52.dp,
    containerColor: Color = Color.Transparent,
    elevation: Dp = 0.dp,
    shadowElevation: Dp = 6.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val shownName = name.ifBlank { stringResource(R.string.person_no_name) }
    val photo = rememberPhoto(photoPath)
    val border by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
        },
        animationSpec = tween(120),
        label = "learner_border",
    )

    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = true,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        border = BorderStroke(if (selected) 1.dp else 0.25.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(avatarColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (photo != null) {
                        Image(
                            bitmap = photo,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(avatarSize),
                        )
                    } else {
                        Text(
                            text = AvatarInitials.of(shownName),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = shownName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    supportingText?.takeIf { it.isNotBlank() }?.let { value ->
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (tags.isNotEmpty()) {
                        PersonTags(tags = tags)
                    }
                }

                trailing?.invoke()
            }

            supportingContent?.invoke(this)
        }
    }
}

/** The person's coloured tags, each option name on its own colour. */
@Composable
fun PersonTags(
    tags: List<PersonTag>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            val coloured = tag.color?.toColorOrNull()
            Text(
                text = tag.label,
                modifier = Modifier
                    .background(
                        coloured ?: MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = if (coloured != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

/**
 * The photo stored on the device at [path], read off the main thread. Null while it loads and
 * when there is none, which is when the initials are drawn instead.
 */
@Composable
private fun rememberPhoto(path: String?): ImageBitmap? {
    val photo by produceState<ImageBitmap?>(initialValue = null, path) {
        value = path?.takeIf { it.isNotBlank() }?.let { file ->
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(file)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    return photo
}

private fun String.toColorOrNull(): Color? =
    runCatching { Color(android.graphics.Color.parseColor(this)) }.getOrNull()
