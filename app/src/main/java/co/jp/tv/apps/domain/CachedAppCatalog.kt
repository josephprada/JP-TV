package co.jp.tv.apps.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach

/**
 * Shows the last known app list at once on a cold start (the PackageManager query takes seconds on weak
 * boxes) and then follows [delegate]. Fresh lists that differ from what is stored replace it.
 */
class CachedAppCatalog(private val delegate: AppCatalog, private val store: AppListStore) : AppCatalog {
    override val apps: Flow<List<TvApp>> = flow {
        var known = store.read()
        if (known.isNotEmpty()) emit(known)
        emitAll(
            delegate.apps.onEach { fresh ->
                if (fresh != known) {
                    known = fresh
                    store.write(fresh)
                }
            },
        )
    }.flowOn(Dispatchers.IO)

    override fun launch(app: TvApp) = delegate.launch(app)
}
