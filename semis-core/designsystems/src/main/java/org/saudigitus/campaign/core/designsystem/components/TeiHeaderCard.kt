package org.saudigitus.campaign.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.saudigitus.campaign.core.data.models.SearchTeiModel
import org.saudigitus.campaign.core.designsystem.theme.text_secondary
import org.saudigitus.campaign.core.designsystem.R

@Composable
fun TeiHeaderCard(tei: SearchTeiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            TeiIdentityRow(
                header = tei.header ?: tei.attributeValues.values.firstOrNull()?.value().toString(),
                teiUid = tei.enrolledOrgUnit
            )

            Spacer(modifier = Modifier.height(20.dp))
            tei.attributeValues.forEach { (label, attrValue) ->
                InfoRow(
                    icon = Icons.Outlined.Info,
                    label = label.uppercase(),
                    value = attrValue.value() ?: "-"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (tei.attributeValues.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_attributes_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = text_secondary
                )
            }
        }
    }
}

@Composable
private fun TeiIdentityRow(header: String, teiUid: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(SurfaceColor.Primary.copy(.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Group,
                contentDescription = null,
                tint = SurfaceColor.Primary,
                modifier = Modifier.size(32.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = header,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            Text(
                text = teiUid,
                fontSize = 12.sp,
                color = text_secondary,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = text_secondary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = text_secondary,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}