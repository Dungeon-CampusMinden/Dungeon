package icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun assignIcon(c : Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "compare_arrows",
            defaultWidth = 128.0.dp,
            defaultHeight = 128.0.dp,
            viewportWidth = 40.0f,
            viewportHeight = 40.0f
        ).apply {
            path(
                fill = SolidColor(c),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(25.792f, 21.875f)
                lineToRelative(-6.167f, -6.125f)
                quadToRelative(-0.208f, -0.208f, -0.292f, -0.437f)
                quadToRelative(-0.083f, -0.23f, -0.083f, -0.48f)
                quadToRelative(0f, -0.25f, 0.083f, -0.479f)
                quadToRelative(0.084f, -0.229f, 0.292f, -0.437f)
                lineToRelative(6.167f, -6.167f)
                quadToRelative(0.375f, -0.375f, 0.896f, -0.375f)
                quadToRelative(0.52f, 0f, 0.937f, 0.417f)
                quadToRelative(0.375f, 0.375f, 0.375f, 0.916f)
                quadToRelative(0f, 0.542f, -0.375f, 0.959f)
                lineToRelative(-3.875f, 3.875f)
                horizontalLineToRelative(11.417f)
                quadToRelative(0.541f, 0f, 0.916f, 0.375f)
                reflectiveQuadToRelative(0.375f, 0.916f)
                quadToRelative(0f, 0.584f, -0.375f, 0.959f)
                reflectiveQuadToRelative(-0.916f, 0.375f)
                horizontalLineTo(23.75f)
                lineToRelative(3.917f, 3.916f)
                quadToRelative(0.375f, 0.417f, 0.375f, 0.917f)
                reflectiveQuadToRelative(-0.417f, 0.917f)
                quadToRelative(-0.417f, 0.375f, -0.937f, 0.375f)
                quadToRelative(-0.521f, 0f, -0.896f, -0.417f)
                close()
                moveTo(12.417f, 32.208f)
                quadToRelative(0.375f, 0.375f, 0.916f, 0.396f)
                quadToRelative(0.542f, 0.021f, 0.917f, -0.396f)
                lineToRelative(6.167f, -6.166f)
                quadToRelative(0.208f, -0.209f, 0.291f, -0.417f)
                quadToRelative(0.084f, -0.208f, 0.084f, -0.5f)
                quadToRelative(0f, -0.25f, -0.084f, -0.479f)
                quadToRelative(-0.083f, -0.229f, -0.291f, -0.438f)
                lineToRelative(-6.167f, -6.166f)
                quadToRelative(-0.375f, -0.375f, -0.917f, -0.354f)
                quadToRelative(-0.541f, 0.02f, -0.916f, 0.395f)
                reflectiveQuadToRelative(-0.375f, 0.917f)
                quadToRelative(0f, 0.542f, 0.375f, 0.917f)
                lineToRelative(3.875f, 3.916f)
                horizontalLineTo(4.875f)
                quadToRelative(-0.542f, 0f, -0.937f, 0.375f)
                quadToRelative(-0.396f, 0.375f, -0.396f, 0.917f)
                quadToRelative(0f, 0.583f, 0.396f, 0.958f)
                quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                horizontalLineToRelative(11.417f)
                lineToRelative(-3.917f, 3.917f)
                quadToRelative(-0.375f, 0.375f, -0.375f, 0.896f)
                reflectiveQuadToRelative(0.417f, 0.937f)
                close()
            }
        }.build()
    }
}