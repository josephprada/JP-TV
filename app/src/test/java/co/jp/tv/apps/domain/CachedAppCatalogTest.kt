package co.jp.tv.apps.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CachedAppCatalogTest {
    private val cached = listOf(TvApp("old.app", "Old", true))
    private val fresh = listOf(TvApp("new.app", "New", true))

    private class MemoryStore(var saved: List<TvApp>) : AppListStore {
        var writes = 0
        override fun read(): List<TvApp> = saved
        override fun write(apps: List<TvApp>) { saved = apps; writes++ }
    }

    private fun delegate(vararg lists: List<TvApp>) = object : AppCatalog {
        override val apps: Flow<List<TvApp>> = flow { lists.forEach { emit(it) } }
        override fun launch(app: TvApp) = Unit
    }

    @Test
    fun `the cached list is emitted first, then the fresh one`() = runTest {
        val store = MemoryStore(cached)
        CachedAppCatalog(delegate(fresh), store).apps.test {
            assertEquals(cached, awaitItem())
            assertEquals(fresh, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `without a cache only the fresh list is emitted`() = runTest {
        CachedAppCatalog(delegate(fresh), MemoryStore(emptyList())).apps.test {
            assertEquals(fresh, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `the fresh list is saved, and an unchanged one is not rewritten`() = runTest {
        val store = MemoryStore(fresh)
        CachedAppCatalog(delegate(fresh, fresh), store).apps.test {
            awaitItem(); awaitItem(); awaitItem(); awaitComplete()
        }
        assertEquals(0, store.writes)
        val other = MemoryStore(cached)
        CachedAppCatalog(delegate(fresh), other).apps.test {
            awaitItem(); awaitItem(); awaitComplete()
        }
        assertEquals(fresh, other.saved)
        assertEquals(1, other.writes)
    }
}
