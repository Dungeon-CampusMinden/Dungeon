package icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun exitIcon(c: Color): ImageVector {
        return remember {
            ImageVector.Builder(
                name = "end",
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
                    moveTo(26.417f, 26.5f)
                    quadToRelative(-0.375f, -0.417f, -0.375f, -0.958f)
                    quadToRelative(0f, -0.542f, 0.375f, -0.917f)
                    lineToRelative(3.25f, -3.25f)
                    horizontalLineTo(16.833f)
                    quadToRelative(-0.541f, 0f, -0.937f, -0.375f)
                    reflectiveQuadToRelative(-0.396f, -0.917f)
                    quadToRelative(0f, -0.583f, 0.396f, -0.958f)
                    reflectiveQuadToRelative(0.937f, -0.375f)
                    horizontalLineToRelative(12.792f)
                    lineToRelative(-3.292f, -3.292f)
                    quadToRelative(-0.375f, -0.333f, -0.354f, -0.875f)
                    quadToRelative(0.021f, -0.541f, 0.396f, -0.958f)
                    quadToRelative(0.375f, -0.375f, 0.937f, -0.375f)
                    quadToRelative(0.563f, 0f, 0.98f, 0.375f)
                    lineToRelative(5.5f, 5.542f)
                    quadToRelative(0.208f, 0.208f, 0.312f, 0.437f)
                    quadToRelative(0.104f, 0.229f, 0.104f, 0.479f)
                    quadToRelative(0f, 0.292f, -0.104f, 0.5f)
                    quadToRelative(-0.104f, 0.209f, -0.312f, 0.417f)
                    lineToRelative(-5.5f, 5.542f)
                    quadToRelative(-0.375f, 0.375f, -0.917f, 0.354f)
                    quadToRelative(-0.542f, -0.021f, -0.958f, -0.396f)
                    close()
                    moveToRelative(-18.5f, 8.375f)
                    quadToRelative(-1.084f, 0f, -1.855f, -0.792f)
                    quadToRelative(-0.77f, -0.791f, -0.77f, -1.875f)
                    verticalLineTo(7.917f)
                    quadToRelative(0f, -1.084f, 0.77f, -1.854f)
                    quadToRelative(0.771f, -0.771f, 1.855f, -0.771f)
                    horizontalLineToRelative(10.541f)
                    quadToRelative(0.542f, 0f, 0.938f, 0.375f)
                    quadToRelative(0.396f, 0.375f, 0.396f, 0.916f)
                    quadToRelative(0f, 0.584f, -0.396f, 0.959f)
                    reflectiveQuadToRelative(-0.938f, 0.375f)
                    horizontalLineTo(7.917f)
                    verticalLineToRelative(24.291f)
                    horizontalLineToRelative(10.541f)
                    quadToRelative(0.542f, 0f, 0.938f, 0.396f)
                    quadToRelative(0.396f, 0.396f, 0.396f, 0.938f)
                    quadToRelative(0f, 0.541f, -0.396f, 0.937f)
                    reflectiveQuadToRelative(-0.938f, 0.396f)
                    close()
                }
            }.build()
        }
    }