package icon


import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
fun addFileIcon(c: Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "file_open",
            defaultWidth = 128.dp,
            defaultHeight = 128.dp,
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
                moveTo(9.542f, 36.375f)
                quadToRelative(-1.042f, 0f, -1.834f, -0.771f)
                quadToRelative(-0.791f, -0.771f, -0.791f, -1.854f)
                verticalLineTo(6.25f)
                quadToRelative(0f, -1.083f, 0.791f, -1.854f)
                quadToRelative(0.792f, -0.771f, 1.834f, -0.771f)
                horizontalLineTo(22.25f)
                quadToRelative(0.542f, 0f, 1.042f, 0.208f)
                quadToRelative(0.5f, 0.209f, 0.875f, 0.584f)
                lineToRelative(8.125f, 8.125f)
                quadToRelative(0.375f, 0.375f, 0.583f, 0.854f)
                quadToRelative(0.208f, 0.479f, 0.208f, 1.021f)
                verticalLineToRelative(9.958f)
                horizontalLineToRelative(-2.625f)
                verticalLineToRelative(-9.417f)
                horizontalLineToRelative(-7.416f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.375f)
                reflectiveQuadToRelative(-0.375f, -0.958f)
                verticalLineTo(6.25f)
                horizontalLineTo(9.542f)
                verticalLineToRelative(27.5f)
                horizontalLineToRelative(16.5f)
                verticalLineToRelative(2.625f)
                close()
                moveToRelative(26.083f, -0.542f)
                lineToRelative(-4.292f, -4.291f)
                verticalLineToRelative(3.416f)
                quadToRelative(0f, 0.584f, -0.395f, 0.959f)
                quadToRelative(-0.396f, 0.375f, -0.938f, 0.375f)
                quadToRelative(-0.542f, 0f, -0.917f, -0.375f)
                reflectiveQuadToRelative(-0.375f, -0.959f)
                verticalLineToRelative(-6.666f)
                quadToRelative(0f, -0.542f, 0.354f, -0.917f)
                quadTo(29.417f, 27f, 30f, 27f)
                horizontalLineToRelative(6.625f)
                quadToRelative(0.583f, 0f, 0.958f, 0.396f)
                reflectiveQuadToRelative(0.375f, 0.937f)
                quadToRelative(0f, 0.542f, -0.375f, 0.917f)
                reflectiveQuadToRelative(-0.958f, 0.375f)
                horizontalLineToRelative(-3.458f)
                lineTo(37.5f, 34f)
                quadToRelative(0.417f, 0.417f, 0.417f, 0.938f)
                quadToRelative(0f, 0.52f, -0.375f, 0.895f)
                quadToRelative(-0.417f, 0.417f, -0.959f, 0.417f)
                quadToRelative(-0.541f, 0f, -0.958f, -0.417f)
                close()
                moveTo(9.542f, 33.75f)
                verticalLineTo(6.25f)
                verticalLineToRelative(27.5f)
                close()
            }
        }.build()
    }
}