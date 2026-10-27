package org.saudigitus.semis.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShowCount(
    modifier: Modifier = Modifier,
    count: Int,
    imageVector: ImageVector = Icons.Outlined.Person,
    color: Color = Color.LightGray.copy(.35f),
) {
    Row(
        modifier = modifier.then(
            Modifier
                .background(
                    color = color,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "Image",
            tint = if (imageVector == Icons.Outlined.Person) Color.Black.copy(.5f) else Color.White,
        )
        Text(
            text = "$count",
            color = if (imageVector == Icons.Outlined.Person) Color.Black.copy(.5f) else Color.White,
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}