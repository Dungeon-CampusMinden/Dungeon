package icon

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun squarePlusIcon(c: Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "add_circle",
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
                moveTo(20.125f, 28.208f)
                quadToRelative(0.542f, 0f, 0.917f, -0.396f)
                quadToRelative(0.375f, -0.395f, 0.375f, -0.937f)
                verticalLineToRelative(-5.417f)
                horizontalLineToRelative(5.5f)
                quadToRelative(0.541f, 0f, 0.916f, -0.396f)
                quadToRelative(0.375f, -0.395f, 0.375f, -0.937f)
                reflectiveQuadToRelative(-0.396f, -0.937f)
                quadToRelative(-0.395f, -0.396f, -0.937f, -0.396f)
                horizontalLineToRelative(-5.458f)
                verticalLineToRelative(-5.709f)
                quadToRelative(0f, -0.541f, -0.375f, -0.916f)
                reflectiveQuadToRelative(-0.959f, -0.375f)
                quadToRelative(-0.541f, 0f, -0.916f, 0.396f)
                quadToRelative(-0.375f, 0.395f, -0.375f, 0.937f)
                verticalLineToRelative(5.667f)
                horizontalLineToRelative(-5.709f)
                quadToRelative(-0.541f, 0f, -0.916f, 0.396f)
                quadToRelative(-0.375f, 0.395f, -0.375f, 0.937f)
                reflectiveQuadToRelative(0.396f, 0.937f)
                quadToRelative(0.395f, 0.396f, 0.937f, 0.396f)
                horizontalLineToRelative(5.667f)
                verticalLineToRelative(5.459f)
                quadToRelative(0f, 0.541f, 0.375f, 0.916f)
                reflectiveQuadToRelative(0.958f, 0.375f)
                close()
                moveTo(20f, 36.375f)
                quadToRelative(-3.458f, 0f, -6.458f, -1.25f)
                reflectiveQuadToRelative(-5.209f, -3.458f)
                quadToRelative(-2.208f, -2.209f, -3.458f, -5.209f)
                quadToRelative(-1.25f, -3f, -1.25f, -6.458f)
                reflectiveQuadToRelative(1.25f, -6.437f)
                quadToRelative(1.25f, -2.98f, 3.458f, -5.188f)
                quadToRelative(2.209f, -2.208f, 5.209f, -3.479f)
                quadToRelative(3f, -1.271f, 6.458f, -1.271f)
                reflectiveQuadToRelative(6.438f, 1.271f)
                quadToRelative(2.979f, 1.271f, 5.187f, 3.479f)
                reflectiveQuadToRelative(3.479f, 5.188f)
                quadToRelative(1.271f, 2.979f, 1.271f, 6.437f)
                reflectiveQuadToRelative(-1.271f, 6.458f)
                quadToRelative(-1.271f, 3f, -3.479f, 5.209f)
                quadToRelative(-2.208f, 2.208f, -5.187f, 3.458f)
                quadToRelative(-2.98f, 1.25f, -6.438f, 1.25f)
                close()
                moveTo(20f, 20f)
                close()
                moveToRelative(0f, 13.75f)
                quadToRelative(5.667f, 0f, 9.708f, -4.042f)
                quadTo(33.75f, 25.667f, 33.75f, 20f)
                reflectiveQuadToRelative(-4.042f, -9.708f)
                quadTo(25.667f, 6.25f, 20f, 6.25f)
                reflectiveQuadToRelative(-9.708f, 4.042f)
                quadTo(6.25f, 14.333f, 6.25f, 20f)
                reflectiveQuadToRelative(4.042f, 9.708f)
                quadTo(14.333f, 33.75f, 20f, 33.75f)
                close()
            }
        }.build()
    }
}