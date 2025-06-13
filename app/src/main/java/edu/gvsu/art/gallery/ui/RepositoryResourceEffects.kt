package edu.gvsu.art.gallery.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import edu.gvsu.art.gallery.lib.Async
import edu.gvsu.art.gallery.lib.generateKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun <T> useRepositoryResource(fetch: suspend () -> Result<T>, key: Any = Unit): Async<T> {
    val state = produceState<Async<T>>(initialValue = Async.Uninitialized, key) {
        value = Async.Loading
        withContext(Dispatchers.IO) {
            try {
                value = fetch().fold(
                    onSuccess = { Async.Success(it) },
                    onFailure = { Async.Failure(it) }
                )
            } catch (e: Throwable) {
                value = Async.Failure(e)
            }
        }
    }
    return state.value
}

@Composable
fun <T> useKeyedRepositoryResource(fetch: suspend () -> Result<T>): Pair<Async<T>, Refresh> {
    val (key, refresh) = useUniqueKey()
    val resource = useRepositoryResource(fetch = fetch, key)
    return Pair(resource, refresh)
}

@Composable
fun useUniqueKey(): Pair<String, Refresh> {
    val (key, setKey) = rememberSaveable { mutableStateOf(generateKey()) }
    return Pair(key, { setKey(generateKey()) })
}

typealias Refresh = () -> Unit
