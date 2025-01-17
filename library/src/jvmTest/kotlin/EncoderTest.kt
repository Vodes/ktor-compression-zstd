import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import pw.vodes.zstd.ZstdEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals

class EncoderTest {

    @Test
    fun testZstdEncoder() {
        println("Testing zstd encoder.")
        val testString = "Test-Data-123"
        val outChannel = ByteChannel()
        runBlocking {
            val stringChannel = ByteReadChannel(testString, charset = Charsets.UTF_8)

            ZstdEncoder.encode(stringChannel).copyAndClose(outChannel)
            outChannel.flushAndClose()

            val decodedBytes = ZstdEncoder.decode(ByteReadChannel(outChannel.toByteArray()))
            val decoded = decodedBytes.readUTF8Line()
            assertEquals(testString, decoded)
            println("Assert successful: '$decoded' = '$testString'")
        }
    }

    @Test
    fun testGzipSpeed() = runBlocking {
        val client = HttpClient(Java)
        val dummyData = client.get("https://dummyjson.com/todos?limit=250").bodyAsText()

        val start = Instant.now()
        (1..500).forEach {
            runGzipRoundtrip(dummyData)
        }
        val end = Instant.now()
        println("Gzip: ${ChronoUnit.NANOS.between(start, end)}ns / ${ChronoUnit.MILLIS.between(start, end)}ms")
    }

    @Test
    fun testZstdSpeed() = runBlocking {
        val client = HttpClient(Java)
        val dummyData = client.get("https://dummyjson.com/todos?limit=250").bodyAsText()

        val start = Instant.now()
        (1..500).forEach {
            runZstdRoundtrip(dummyData)
        }
        val end = Instant.now()
        println("Zstd: ${ChronoUnit.NANOS.between(start, end)}ns / ${ChronoUnit.MILLIS.between(start, end)}ms")
    }

    private suspend fun runZstdRoundtrip(data: String) {
        val outChannel = ByteChannel()
        val stringChannel = ByteReadChannel(data, charset = Charsets.UTF_8)
        try {
            ZstdEncoder.encode(stringChannel).copyAndClose(outChannel)
            outChannel.flushAndClose()

            val encodedBytes = outChannel.toByteArray()
            val decodedBytes = ZstdEncoder.decode(ByteReadChannel(encodedBytes))
            val decoded = decodedBytes.readUTF8Line()
            assertEquals(data, decoded)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outChannel.flushAndClose()
        }
    }

    private suspend fun runGzipRoundtrip(data: String) {
        val outChannel = ByteChannel()
        val stringChannel = ByteReadChannel(data, charset = Charsets.UTF_8)
        try {
            GZipEncoder.encode(stringChannel).copyAndClose(outChannel)
            outChannel.flushAndClose()

            val encodedBytes = outChannel.toByteArray()
            val decodedBytes = GZipEncoder.decode(ByteReadChannel(encodedBytes))
            val decoded = decodedBytes.readUTF8Line()
            assertEquals(data, decoded)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outChannel.flushAndClose()
        }
    }
}