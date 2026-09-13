package eu.kanade.tachiyomi.data.coil

import coil3.key.Keyer
import coil3.request.Options
import eu.kanade.domain.manga.model.hasCustomBackground
import eu.kanade.domain.manga.model.hasCustomCover
import eu.kanade.tachiyomi.data.cache.CoverCache
import tachiyomi.domain.manga.model.MangaCover
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import tachiyomi.domain.manga.model.Manga as DomainManga

class MangaKeyer : Keyer<DomainManga> {
    override fun key(data: DomainManga, options: Options): String {
        // AY -->
        return when {
            options.useBackground && data.hasCustomBackground() -> "${data.id};${data.backgroundLastModified}"
            // ANK -->
            options.useBackground ->
                "${data.backgroundUrl.orKeyOf("background-none", data.id)};${data.backgroundLastModified}"
            // ANK <--
            // <-- AY
            data.hasCustomCover() -> "${data.id};${data.coverLastModified}"
            // ANK -->
            else -> "${data.thumbnailUrl.orKeyOf("cover-none", data.id)};${data.coverLastModified}"
            // ANK <--
        }
    }
}

class MangaCoverKeyer(
    private val coverCache: CoverCache = Injekt.get(),
) : Keyer<MangaCover> {
    override fun key(data: MangaCover, options: Options): String {
        return if (coverCache.getCustomCoverFile(data.mangaId).exists()) {
            "${data.mangaId};${data.lastModified}"
        } else {
            // ANK -->
            "${data.url.orKeyOf("cover-none", data.mangaId)};${data.lastModified}"
            // ANK <--
        }
    }
}

// ANK -->
/**
 * Entries without a cover/background would all resolve to the very same cache key (ie. `"null;0"`),
 * so Coil's memory & disk cache would happily serve an unrelated entry's image for any of them.
 * Fall back to an id based key to keep every entry unique.
 */
private fun String?.orKeyOf(prefix: String, id: Long): String {
    return if (isNullOrEmpty()) "$prefix;$id" else this
}
// ANK <--
