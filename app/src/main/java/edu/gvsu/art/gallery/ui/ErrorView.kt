package edu.gvsu.art.gallery.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.gvsu.art.gallery.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Composable
fun ErrorView(error: Throwable, onRetryClick: () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(error.asReadableError())
        if (onRetryClick !== {}) {
            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = { onRetryClick() }
            ) {
                Text(stringResource(R.string.error_view_retry_text))
            }
        }
    }
}

@Composable
fun Throwable.asReadableError(): String {
    return when (this) {
        is SocketTimeoutException -> stringResource(R.string.error_view_server_timeout)
        is UnknownHostException -> stringResource(R.string.error_view_offline)
        else -> stringResource(R.string.error_view_unknown_error)
    }
}
