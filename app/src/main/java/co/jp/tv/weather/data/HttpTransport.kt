package co.jp.tv.weather.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The one thing the weather adapters need from the network: GET a URL and return the body text. */
fun interface HttpTransport {
    /** Throws [IOException] on timeouts, non-200 answers and anything else that is not a body. */
    suspend fun get(url: String): String
}

/** [HttpTransport] over [HttpURLConnection] on [Dispatchers.IO], with short timeouts so a dead link never hangs a refresh. */
class HttpUrlConnectionTransport(
    private val connectTimeoutMs: Int = 8_000,
    private val readTimeoutMs: Int = 10_000,
) : HttpTransport {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "JP-TV-launcher")
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) throw IOException("HTTP $code")
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { readCapped(it) }
        } finally {
            connection.disconnect()
        }
    }

    private fun readCapped(reader: java.io.Reader): String {
        val out = StringBuilder()
        val buffer = CharArray(4096)
        while (true) {
            val n = reader.read(buffer)
            if (n < 0) break
            out.append(buffer, 0, n)
            if (out.length > MAX_CHARS) throw IOException("Response too large")
        }
        return out.toString()
    }

    private companion object {
        /** Both endpoints answer with a few hundred bytes; anything past this is not ours. */
        const val MAX_CHARS = 256 * 1024
    }
}
