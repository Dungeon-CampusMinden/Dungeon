package icon


import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
fun projectIcon(c : Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "project",
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
                moveTo(7.208f, 34.917f)
                quadToRelative(-1.583f, 0f, -2.687f, -1.105f)
                quadToRelative(-1.104f, -1.104f, -1.104f, -2.687f)
                quadToRelative(0f, -1.625f, 1.104f, -2.729f)
                reflectiveQuadToRelative(2.687f, -1.104f)
                quadToRelative(0.25f, 0f, 0.604f, 0.041f)
                quadToRelative(0.355f, 0.042f, 0.688f, 0.167f)
                lineToRelative(7.333f, -11.375f)
                quadToRelative(-1f, -0.833f, -1.562f, -2.062f)
                quadToRelative(-0.563f, -1.23f, -0.563f, -2.646f)
                quadToRelative(0f, -2.667f, 1.834f, -4.521f)
                quadTo(17.375f, 5.042f, 20f, 5.042f)
                reflectiveQuadToRelative(4.479f, 1.854f)
                quadToRelative(1.854f, 1.854f, 1.854f, 4.479f)
                quadToRelative(0f, 1.458f, -0.583f, 2.688f)
                quadToRelative(-0.583f, 1.229f, -1.583f, 2.062f)
                lineTo(31.5f, 27.5f)
                quadToRelative(0.333f, -0.125f, 0.688f, -0.167f)
                quadToRelative(0.354f, -0.041f, 0.604f, -0.041f)
                quadToRelative(1.583f, 0f, 2.708f, 1.104f)
                quadToRelative(1.125f, 1.104f, 1.125f, 2.729f)
                quadToRelative(0f, 1.583f, -1.125f, 2.687f)
                quadToRelative(-1.125f, 1.105f, -2.708f, 1.105f)
                quadToRelative(-1.584f, 0f, -2.709f, -1.105f)
                quadToRelative(-1.125f, -1.104f, -1.125f, -2.687f)
                quadToRelative(0f, -0.708f, 0.271f, -1.354f)
                quadToRelative(0.271f, -0.646f, 0.771f, -1.271f)
                lineToRelative(-7.333f, -11.375f)
                quadToRelative(-0.417f, 0.208f, -0.855f, 0.333f)
                quadToRelative(-0.437f, 0.125f, -0.895f, 0.209f)
                verticalLineToRelative(9.75f)
                quadToRelative(1.25f, 0.416f, 2.083f, 1.416f)
                quadToRelative(0.833f, 1f, 0.833f, 2.292f)
                quadToRelative(0f, 1.583f, -1.125f, 2.687f)
                quadToRelative(-1.125f, 1.105f, -2.708f, 1.105f)
                quadToRelative(-1.583f, 0f, -2.688f, -1.105f)
                quadToRelative(-1.104f, -1.104f, -1.104f, -2.687f)
                quadToRelative(0f, -1.292f, 0.813f, -2.313f)
                quadToRelative(0.812f, -1.02f, 2.104f, -1.395f)
                verticalLineToRelative(-9.75f)
                quadToRelative(-0.5f, -0.084f, -0.937f, -0.209f)
                quadToRelative(-0.438f, -0.125f, -0.855f, -0.333f)
                lineTo(10f, 28.5f)
                quadToRelative(0.5f, 0.625f, 0.771f, 1.271f)
                quadToRelative(0.271f, 0.646f, 0.271f, 1.354f)
                quadToRelative(0f, 1.583f, -1.104f, 2.687f)
                quadToRelative(-1.105f, 1.105f, -2.73f, 1.105f)
                close()
                moveToRelative(0f, -2.625f)
                quadToRelative(0.5f, 0f, 0.854f, -0.354f)
                quadToRelative(0.355f, -0.355f, 0.355f, -0.813f)
                quadToRelative(0f, -0.5f, -0.355f, -0.854f)
                quadToRelative(-0.354f, -0.354f, -0.854f, -0.354f)
                quadToRelative(-0.458f, 0f, -0.812f, 0.354f)
                quadToRelative(-0.354f, 0.354f, -0.354f, 0.854f)
                quadToRelative(0f, 0.458f, 0.354f, 0.813f)
                quadToRelative(0.354f, 0.354f, 0.812f, 0.354f)
                close()
                moveTo(20f, 15.042f)
                quadToRelative(1.542f, 0f, 2.625f, -1.063f)
                quadToRelative(1.083f, -1.062f, 1.083f, -2.604f)
                reflectiveQuadToRelative(-1.083f, -2.604f)
                quadTo(21.542f, 7.708f, 20f, 7.708f)
                quadToRelative(-1.5f, 0f, -2.583f, 1.063f)
                quadToRelative(-1.084f, 1.062f, -1.084f, 2.604f)
                reflectiveQuadToRelative(1.084f, 2.604f)
                quadTo(18.5f, 15.042f, 20f, 15.042f)
                close()
                moveToRelative(0f, 17.25f)
                quadToRelative(0.5f, 0f, 0.854f, -0.354f)
                quadToRelative(0.354f, -0.355f, 0.354f, -0.813f)
                quadToRelative(0f, -0.5f, -0.354f, -0.854f)
                reflectiveQuadTo(20f, 29.917f)
                quadToRelative(-0.5f, 0f, -0.833f, 0.354f)
                quadToRelative(-0.334f, 0.354f, -0.334f, 0.854f)
                quadToRelative(0f, 0.458f, 0.334f, 0.813f)
                quadToRelative(0.333f, 0.354f, 0.833f, 0.354f)
                close()
                moveToRelative(12.792f, 0f)
                quadToRelative(0.5f, 0f, 0.833f, -0.354f)
                quadToRelative(0.333f, -0.355f, 0.333f, -0.813f)
                quadToRelative(0f, -0.5f, -0.333f, -0.854f)
                reflectiveQuadToRelative(-0.833f, -0.354f)
                quadToRelative(-0.5f, 0f, -0.834f, 0.354f)
                quadToRelative(-0.333f, 0.354f, -0.333f, 0.854f)
                quadToRelative(0f, 0.458f, 0.333f, 0.813f)
                quadToRelative(0.334f, 0.354f, 0.834f, 0.354f)
                close()
            }
        }.build()
    }
}