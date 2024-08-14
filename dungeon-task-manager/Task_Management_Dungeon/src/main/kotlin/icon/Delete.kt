package icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
fun deleteIcon(c: Color): ImageVector {
    return remember {
        ImageVector.Builder(
            name = "delete",
            defaultWidth = 40.0.dp,
            defaultHeight = 40.0.dp,
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
                moveTo(11.208f, 34.708f)
                quadToRelative(-1.041f, 0f, -1.833f, -0.77f)
                quadToRelative(-0.792f, -0.771f, -0.792f, -1.855f)
                verticalLineTo(9.25f)
                horizontalLineTo(8.25f)
                quadToRelative(-0.583f, 0f, -0.958f, -0.396f)
                reflectiveQuadToRelative(-0.375f, -0.937f)
                quadToRelative(0f, -0.542f, 0.375f, -0.917f)
                reflectiveQuadToRelative(0.958f, -0.375f)
                horizontalLineToRelative(6.458f)
                quadToRelative(0f, -0.583f, 0.375f, -0.958f)
                reflectiveQuadToRelative(0.959f, -0.375f)
                horizontalLineTo(24f)
                quadToRelative(0.583f, 0f, 0.938f, 0.375f)
                quadToRelative(0.354f, 0.375f, 0.354f, 0.958f)
                horizontalLineToRelative(6.5f)
                quadToRelative(0.541f, 0f, 0.937f, 0.375f)
                reflectiveQuadToRelative(0.396f, 0.917f)
                quadToRelative(0f, 0.583f, -0.396f, 0.958f)
                reflectiveQuadToRelative(-0.937f, 0.375f)
                horizontalLineToRelative(-0.334f)
                verticalLineToRelative(22.833f)
                quadToRelative(0f, 1.084f, -0.791f, 1.855f)
                quadToRelative(-0.792f, 0.77f, -1.875f, 0.77f)
                close()
                moveToRelative(0f, -25.458f)
                verticalLineToRelative(22.833f)
                horizontalLineToRelative(17.584f)
                verticalLineTo(9.25f)
                close()
                moveToRelative(4.125f, 18.042f)
                quadToRelative(0f, 0.583f, 0.396f, 0.958f)
                reflectiveQuadToRelative(0.938f, 0.375f)
                quadToRelative(0.541f, 0f, 0.916f, -0.375f)
                reflectiveQuadToRelative(0.375f, -0.958f)
                verticalLineTo(14f)
                quadToRelative(0f, -0.542f, -0.375f, -0.937f)
                quadToRelative(-0.375f, -0.396f, -0.916f, -0.396f)
                quadToRelative(-0.584f, 0f, -0.959f, 0.396f)
                quadToRelative(-0.375f, 0.395f, -0.375f, 0.937f)
                close()
                moveToRelative(6.709f, 0f)
                quadToRelative(0f, 0.583f, 0.396f, 0.958f)
                quadToRelative(0.395f, 0.375f, 0.937f, 0.375f)
                reflectiveQuadToRelative(0.937f, -0.375f)
                quadToRelative(0.396f, -0.375f, 0.396f, -0.958f)
                verticalLineTo(14f)
                quadToRelative(0f, -0.542f, -0.396f, -0.937f)
                quadToRelative(-0.395f, -0.396f, -0.937f, -0.396f)
                reflectiveQuadToRelative(-0.937f, 0.396f)
                quadToRelative(-0.396f, 0.395f, -0.396f, 0.937f)
                close()
                moveTo(11.208f, 9.25f)
                verticalLineToRelative(22.833f)
                verticalLineTo(9.25f)
                close()
            }
        }.build()
    }
}