package co.jp.tv.playlist.domain

/** Which file names the launcher treats as wallpaper media. */
object MediaFileKinds {
    private val videoExtensions = setOf("mp4", "webm", "mkv")
    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp")

    /** The kind of media [fileName] holds, or null for anything else (and for hidden files). */
    fun kindOf(fileName: String): MediaKind? {
        if (fileName.startsWith(".")) return null
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        return when (extension) {
            in videoExtensions -> MediaKind.VIDEO
            in imageExtensions -> MediaKind.IMAGE
            else -> null
        }
    }
}
