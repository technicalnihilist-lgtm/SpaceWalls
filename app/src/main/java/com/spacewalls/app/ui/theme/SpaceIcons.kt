package com.spacewalls.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The handful of glyphs Material's core icon set does not ship. Declared as
 * vectors instead of pulling in the 30 MB extended icon artifact.
 */
object SpaceIcons {

    private fun vector(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = addPathNodes(pathData),
                fill = SolidColor(Color.Black)
            )
        }.build()

    val Download: ImageVector by lazy {
        vector("Download", "M5,20h14v-2H5V20zM19,9h-4V3H9v6H5l7,7L19,9z")
    }

    val OpenInNew: ImageVector by lazy {
        vector(
            "OpenInNew",
            "M19,19H5V5h7V3H5C3.89,3 3,3.9 3,5v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2V19z" +
                "M14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3H14z"
        )
    }

    val Explore: ImageVector by lazy {
        vector(
            "Explore",
            "M12,10.9c-0.61,0 -1.1,0.49 -1.1,1.1s0.49,1.1 1.1,1.1c0.61,0 1.1,-0.49 1.1,-1.1" +
                "S12.61,10.9 12,10.9zM12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10" +
                "S17.52,2 12,2zM14.19,14.19L6,18l3.81,-8.19L18,6L14.19,14.19z"
        )
    }

    val LibraryBooks: ImageVector by lazy {
        vector(
            "LibraryBooks",
            "M4,6H2v14c0,1.1 0.9,2 2,2h14v-2H4V6zM20,2H8C6.9,2 6,2.9 6,4v12c0,1.1 0.9,2 2,2h12" +
                "c1.1,0 2,-0.9 2,-2V4C22,2.9 21.1,2 20,2zM19,11H9V9h10V11zM15,15H9v-2h6V15z" +
                "M19,7H9V5h10V7z"
        )
    }
}
