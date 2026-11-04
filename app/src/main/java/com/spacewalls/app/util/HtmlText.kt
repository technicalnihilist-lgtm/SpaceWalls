package com.spacewalls.app.util

/**
 * Wikimedia metadata arrives as HTML fragments. The app never renders that
 * markup — it is flattened to readable text so credit lines stay honest and
 * safe to display.
 */
object HtmlText {

    private val TAG = Regex("<[^>]*>")
    private val WHITESPACE = Regex("\\s+")

    private val ENTITIES = mapOf(
        "&amp;" to "&", "&lt;" to "<", "&gt;" to ">", "&quot;" to "\"",
        "&#39;" to "'", "&apos;" to "'", "&nbsp;" to " ", "&ndash;" to "–",
        "&mdash;" to "—", "&hellip;" to "…", "&#160;" to " "
    )

    fun strip(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var text = TAG.replace(input, " ")
        for ((entity, replacement) in ENTITIES) {
            text = text.replace(entity, replacement)
        }
        text = Regex("&#\\d+;").replace(text, "")
        return WHITESPACE.replace(text, " ").trim()
    }

    /** Strips markup and truncates on a word boundary for card previews. */
    fun summarize(input: String?, maxChars: Int = 220): String {
        val clean = strip(input)
        if (clean.length <= maxChars) return clean
        val cut = clean.take(maxChars)
        val lastSpace = cut.lastIndexOf(' ')
        return (if (lastSpace > maxChars / 2) cut.take(lastSpace) else cut).trimEnd() + "…"
    }
}
