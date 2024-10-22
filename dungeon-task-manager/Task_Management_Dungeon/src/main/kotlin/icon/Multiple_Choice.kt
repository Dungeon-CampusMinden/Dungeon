package icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun multipleChopiceIcon(c: Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "checklist_rtl",
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
                moveTo(4.75f, 14.625f)
                quadToRelative(-0.583f, 0f, -0.958f, -0.375f)
                reflectiveQuadToRelative(-0.375f, -0.917f)
                quadToRelative(0f, -0.541f, 0.375f, -0.937f)
                reflectiveQuadTo(4.75f, 12f)
                horizontalLineToRelative(12.208f)
                quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                quadToRelative(0.396f, 0.396f, 0.396f, 0.937f)
                quadToRelative(0f, 0.542f, -0.396f, 0.917f)
                reflectiveQuadToRelative(-0.938f, 0.375f)
                close()
                moveToRelative(0f, 13.333f)
                quadToRelative(-0.583f, 0f, -0.958f, -0.375f)
                reflectiveQuadToRelative(-0.375f, -0.916f)
                quadToRelative(0f, -0.542f, 0.375f, -0.938f)
                quadToRelative(0.375f, -0.396f, 0.958f, -0.396f)
                horizontalLineToRelative(12.208f)
                quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                quadToRelative(0.396f, 0.396f, 0.396f, 0.938f)
                quadToRelative(0f, 0.541f, -0.396f, 0.916f)
                reflectiveQuadToRelative(-0.938f, 0.375f)
                close()
                moveTo(26.5f, 16.917f)
                lineToRelative(-4f, -3.959f)
                quadToRelative(-0.375f, -0.416f, -0.375f, -0.937f)
                quadToRelative(0f, -0.521f, 0.417f, -0.938f)
                quadToRelative(0.375f, -0.375f, 0.896f, -0.375f)
                quadToRelative(0.52f, 0f, 0.937f, 0.375f)
                lineToRelative(3.042f, 3.042f)
                lineToRelative(6.375f, -6.417f)
                quadToRelative(0.416f, -0.416f, 0.958f, -0.395f)
                quadToRelative(0.542f, 0.02f, 0.917f, 0.395f)
                quadToRelative(0.375f, 0.417f, 0.395f, 0.959f)
                quadToRelative(0.021f, 0.541f, -0.395f, 0.916f)
                lineToRelative(-7.334f, 7.334f)
                quadToRelative(-0.375f, 0.375f, -0.916f, 0.375f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.375f)
                close()
                moveToRelative(0f, 13.333f)
                lineToRelative(-4f, -3.958f)
                quadToRelative(-0.375f, -0.417f, -0.375f, -0.938f)
                quadToRelative(0f, -0.521f, 0.417f, -0.937f)
                quadToRelative(0.375f, -0.375f, 0.896f, -0.375f)
                quadToRelative(0.52f, 0f, 0.937f, 0.375f)
                lineToRelative(3.042f, 3.041f)
                lineToRelative(6.375f, -6.416f)
                quadToRelative(0.416f, -0.417f, 0.958f, -0.396f)
                quadToRelative(0.542f, 0.021f, 0.917f, 0.396f)
                quadToRelative(0.375f, 0.416f, 0.395f, 0.958f)
                quadToRelative(0.021f, 0.542f, -0.395f, 0.917f)
                lineToRelative(-7.334f, 7.333f)
                quadToRelative(-0.375f, 0.375f, -0.916f, 0.375f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.375f)
                close()
            }
        }.build()
    }
}