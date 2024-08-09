package icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun rectangleListIcon(c: Color): ImageVector {
        return remember {
            ImageVector.Builder(
                name = "list",
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
                    moveTo(7.875f, 34.75f)
                    quadToRelative(-1.042f, 0f, -1.833f, -0.792f)
                    quadToRelative(-0.792f, -0.791f, -0.792f, -1.833f)
                    verticalLineTo(7.875f)
                    quadToRelative(0f, -1.042f, 0.792f, -1.833f)
                    quadToRelative(0.791f, -0.792f, 1.833f, -0.792f)
                    horizontalLineToRelative(24.25f)
                    quadToRelative(1.042f, 0f, 1.833f, 0.792f)
                    quadToRelative(0.792f, 0.791f, 0.792f, 1.833f)
                    verticalLineToRelative(24.25f)
                    quadToRelative(0f, 1.042f, -0.792f, 1.833f)
                    quadToRelative(-0.791f, 0.792f, -1.833f, 0.792f)
                    close()
                    moveToRelative(0f, -2.625f)
                    horizontalLineToRelative(24.25f)
                    verticalLineTo(7.875f)
                    horizontalLineTo(7.875f)
                    verticalLineToRelative(24.25f)
                    close()
                    moveToRelative(5f, -4.167f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.375f)
                    reflectiveQuadToRelative(0.396f, -0.916f)
                    quadToRelative(0f, -0.542f, -0.396f, -0.938f)
                    quadToRelative(-0.396f, -0.396f, -0.896f, -0.396f)
                    quadToRelative(-0.542f, 0f, -0.937f, 0.396f)
                    quadToRelative(-0.396f, 0.396f, -0.396f, 0.938f)
                    quadToRelative(0f, 0.541f, 0.396f, 0.916f)
                    quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                    close()
                    moveToRelative(0f, -6.666f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.375f)
                    reflectiveQuadToRelative(0.396f, -0.917f)
                    quadToRelative(0f, -0.542f, -0.396f, -0.938f)
                    quadToRelative(-0.396f, -0.395f, -0.896f, -0.395f)
                    quadToRelative(-0.542f, 0f, -0.937f, 0.395f)
                    quadToRelative(-0.396f, 0.396f, -0.396f, 0.938f)
                    quadToRelative(0f, 0.542f, 0.396f, 0.917f)
                    quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                    close()
                    moveToRelative(0f, -6.667f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.396f)
                    reflectiveQuadToRelative(0.396f, -0.937f)
                    quadToRelative(0f, -0.542f, -0.396f, -0.917f)
                    reflectiveQuadTo(12.875f, 12f)
                    quadToRelative(-0.542f, 0f, -0.937f, 0.375f)
                    quadToRelative(-0.396f, 0.375f, -0.396f, 0.917f)
                    quadToRelative(0f, 0.541f, 0.396f, 0.937f)
                    quadToRelative(0.395f, 0.396f, 0.937f, 0.396f)
                    close()
                    moveTo(19.458f, 28f)
                    horizontalLineToRelative(7.334f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.396f)
                    quadToRelative(0.395f, -0.396f, 0.395f, -0.937f)
                    quadToRelative(0f, -0.542f, -0.395f, -0.917f)
                    quadToRelative(-0.396f, -0.375f, -0.896f, -0.375f)
                    horizontalLineToRelative(-7.334f)
                    quadToRelative(-0.541f, 0f, -0.937f, 0.375f)
                    reflectiveQuadToRelative(-0.396f, 0.917f)
                    quadToRelative(0f, 0.583f, 0.396f, 0.958f)
                    reflectiveQuadToRelative(0.937f, 0.375f)
                    close()
                    moveToRelative(0f, -6.708f)
                    horizontalLineToRelative(7.334f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.375f)
                    quadToRelative(0.395f, -0.375f, 0.395f, -0.917f)
                    quadToRelative(0f, -0.583f, -0.395f, -0.958f)
                    quadToRelative(-0.396f, -0.375f, -0.896f, -0.375f)
                    horizontalLineToRelative(-7.334f)
                    quadToRelative(-0.541f, 0f, -0.937f, 0.395f)
                    quadToRelative(-0.396f, 0.396f, -0.396f, 0.938f)
                    quadToRelative(0f, 0.542f, 0.396f, 0.917f)
                    reflectiveQuadToRelative(0.937f, 0.375f)
                    close()
                    moveToRelative(0f, -6.667f)
                    horizontalLineToRelative(7.334f)
                    quadToRelative(0.5f, 0f, 0.896f, -0.375f)
                    quadToRelative(0.395f, -0.375f, 0.395f, -0.917f)
                    quadToRelative(0f, -0.583f, -0.395f, -0.958f)
                    quadToRelative(-0.396f, -0.375f, -0.896f, -0.375f)
                    horizontalLineToRelative(-7.334f)
                    quadToRelative(-0.541f, 0f, -0.937f, 0.396f)
                    reflectiveQuadToRelative(-0.396f, 0.937f)
                    quadToRelative(0f, 0.542f, 0.396f, 0.917f)
                    reflectiveQuadToRelative(0.937f, 0.375f)
                    close()
                    moveToRelative(-11.583f, 17.5f)
                    verticalLineTo(7.875f)
                    verticalLineToRelative(24.25f)
                    close()
                }
            }.build()
        }
    }