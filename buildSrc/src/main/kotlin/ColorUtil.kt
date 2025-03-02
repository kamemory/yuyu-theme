import kotlin.math.floor

class ColorMap(
    val color100: String,
    val color200: String,
    val color300: String,
    val color400: String,
    val color500: String,
    val color600: String,
    val color700: String,
    val color800: String,
    val color900: String
)

fun createColorMap(baseColor: String): ColorMap {
    val r = baseColor.substring(1..2).toInt(16)
    val g = baseColor.substring(3..4).toInt(16)
    val b = baseColor.substring(5..6).toInt(16)
    return ColorMap(
        color100 = adjustLowerValue(r, g, b, 1),
        color200 = adjustLowerValue(r, g, b, 2),
        color300 = adjustLowerValue(r, g, b, 3),
        color400 = adjustLowerValue(r, g, b, 4),
        color500 = baseColor,
        color600 = adjustUpperValue(r, g, b, 6),
        color700 = adjustUpperValue(r, g, b, 7),
        color800 = adjustUpperValue(r, g, b, 8),
        color900 = adjustUpperValue(r, g, b, 9),
    )
}

private fun adjustLowerValue(r: Int, g: Int, b: Int, rate: Int): String {
    val rv = floor(r / 5.0 * rate).toInt()
    val gv = floor(g / 5.0 * rate).toInt()
    val bv = floor(b / 5.0 * rate).toInt()
    return rgbToString(rv, gv, bv)
}

private fun adjustUpperValue(r: Int, g: Int, b: Int, rate: Int): String {
    val rv = floor((255 - r) / 5.0 * (rate - 5) + r + 0.5).toInt()
    val gv = floor((255 - g) / 5.0 * (rate - 5) + g + 0.5).toInt()
    val bv = floor((255 - b) / 5.0 * (rate - 5) + b + 0.5).toInt()
    return rgbToString(rv, gv, bv)
}

private fun rgbToString(r: Int, g: Int, b: Int): String {
    return "#%02x%02x%02x".format(r, g, b)
}