package com.spacewalls.app.data.license

import com.spacewalls.app.data.model.RightsStatus
import com.spacewalls.app.util.HtmlText

/**
 * The gatekeeper for usage rights.
 *
 * Product rule: an image is only offered as a downloadable wallpaper when its
 * licence is explicitly stated by the source and recognised here. Anything
 * ambiguous — no licence, an unrecognised licence, trademark or personality
 * restrictions, an explicit copyright notice — is rejected outright rather
 * than guessed at.
 */
object LicensePolicy {

    data class Decision(
        val licenseName: String,
        val licenseUrl: String,
        val usageTerms: String,
        val rightsStatus: RightsStatus,
        val note: String?
    )

    /** Reuse guidance pages, used only when the API itself omits a licence URL. */
    private const val COMMONS_REUSE = "https://commons.wikimedia.org/wiki/Commons:Reusing_content_outside_Wikimedia"
    private const val NASA_GUIDELINES = "https://www.nasa.gov/nasa-brand-center/images-and-media/"

    /** Wallpapers need enough pixels to survive a phone or tablet screen. */
    private const val MIN_LONG_EDGE = 1600
    private const val MIN_SHORT_EDGE = 900

    private val ACCEPTED_MIME = setOf("image/jpeg", "image/png", "image/webp")

    private val CC_URL = Regex(
        "^https?://creativecommons\\.org/(licenses/(by|by-sa)/\\d\\.\\d|publicdomain/(zero|mark)/\\d\\.\\d)/?$",
        RegexOption.IGNORE_CASE
    )

    /** Licences that restrict commercial use or derivatives are not accepted. */
    private val REJECTED_CC = Regex(
        "^https?://creativecommons\\.org/licenses/(by-nc|by-nd|by-nc-sa|by-nc-nd)/",
        RegexOption.IGNORE_CASE
    )

    private val COPYRIGHT_MARKERS = listOf(
        "©", "copyright", "all rights reserved", "getty images", "reuters",
        "associated press", "afp", "shutterstock", "alamy", "fair use", "non-free"
    )

    private val PARTNER_MARKERS = listOf(
        "esa", "eso", "jaxa", "csa", "aura", "stsci", "noirlab", "cxc",
        "chandra x-ray center", "harvard-smithsonian", "cfa", "nrao", "sofia"
    )

    fun hasUsableResolution(width: Int, height: Int): Boolean {
        if (width <= 0 || height <= 0) return false
        val long = maxOf(width, height)
        val short = minOf(width, height)
        return long >= MIN_LONG_EDGE && short >= MIN_SHORT_EDGE
    }

    fun isAcceptedMime(mime: String?): Boolean =
        mime != null && ACCEPTED_MIME.contains(mime.lowercase())

    /**
     * Wikimedia Commons: the licence must be machine-readable in extmetadata.
     */
    fun evaluateWikimedia(
        licenseShortName: String?,
        licenseUrl: String?,
        usageTerms: String?,
        restrictions: String?,
        artist: String?,
        descriptionPageUrl: String?,
        mime: String?,
        width: Int,
        height: Int
    ): Decision? {
        if (!isAcceptedMime(mime)) return null
        if (!hasUsableResolution(width, height)) return null
        if (descriptionPageUrl.isNullOrBlank()) return null

        val short = HtmlText.strip(licenseShortName)
        val url = HtmlText.strip(licenseUrl)
        val terms = HtmlText.strip(usageTerms)
        val restrictionText = HtmlText.strip(restrictions)

        // Trademark, personality or other restrictions flagged by Commons itself.
        if (restrictionText.isNotEmpty()) return null
        if (REJECTED_CC.containsMatchIn(url)) return null
        if (terms.lowercase().contains("non-free") || terms.lowercase().contains("fair use")) return null

        val isPublicDomain = short.contains("public domain", true) ||
            short.contains("cc0", true) ||
            url.contains("publicdomain", true)
        val isCcBy = CC_URL.containsMatchIn(url)

        val licenseName: String
        val licenseUrl: String
        when {
            isCcBy -> {
                licenseName = short.ifBlank { "Creative Commons" }
                licenseUrl = url
            }
            isPublicDomain -> {
                licenseName = short.ifBlank { "Public domain" }
                licenseUrl = url.ifBlank { COMMONS_REUSE }
            }
            else -> return null // Unrecognised licence — never offered.
        }

        val creator = HtmlText.strip(artist)
        // CC BY and CC BY-SA require attribution, so an anonymous file cannot be credited.
        if (!isPublicDomain && creator.isBlank()) return null

        val note = when {
            short.contains("by-sa", true) ->
                "Share-alike: adaptations must carry the same licence."
            isPublicDomain -> null
            else -> "Attribution required — credit the creator as shown."
        }

        return Decision(
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            usageTerms = terms.ifBlank { licenseName },
            rightsStatus = RightsStatus.VERIFIED,
            note = note
        )
    }

    /**
     * NASA Image and Video Library. NASA media is generally not copyrighted, but
     * NASA's own guidelines warn that individual items may embed third-party
     * material — so explicit copyright notices disqualify an item.
     */
    fun evaluateNasa(
        center: String?,
        secondaryCreator: String?,
        title: String?,
        description: String?,
        originalUrl: String?,
        width: Int,
        height: Int
    ): Decision? {
        if (center.isNullOrBlank()) return null // no attributable NASA centre
        if (originalUrl.isNullOrBlank()) return null
        if (!originalUrl.startsWith("https://images-assets.nasa.gov") &&
            !originalUrl.startsWith("https://images.nasa.gov")
        ) return null
        if (!hasUsableResolution(width, height)) return null

        val haystack = listOfNotNull(title, description, secondaryCreator)
            .joinToString(" ")
            .lowercase()
        if (COPYRIGHT_MARKERS.any { haystack.contains(it) }) return null

        val partner = PARTNER_MARKERS.any { marker ->
            (secondaryCreator ?: "").lowercase().contains(marker) ||
                (description ?: "").lowercase().contains(" $marker") ||
                (description ?: "").lowercase().startsWith("$marker")
        }

        return Decision(
            licenseName = "NASA Media Usage Guidelines",
            licenseUrl = NASA_GUIDELINES,
            usageTerms = "NASA content is generally not subject to copyright in the United States. " +
                "Third-party material may be protected.",
            rightsStatus = if (partner) RightsStatus.THIRD_PARTY_CREDIT else RightsStatus.VERIFIED,
            note = if (partner) {
                "Credit line names a partner institution — check their terms before commercial use."
            } else {
                "Credit NASA. Do not imply NASA endorsement."
            }
        )
    }
}
