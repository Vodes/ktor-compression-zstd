package pw.vodes.zstd

import com.github.luben.zstd.ZstdInputStream
import com.github.luben.zstd.ZstdOutputStream
import io.ktor.client.utils.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.io.readByteArray
import java.io.ByteArrayOutputStream
import kotlin.coroutines.CoroutineContext

actual object ZstdEncoder : ContentEncoder, Encoder by ZstdEncoder {
    override val name = "zstd"

    override fun decode(source: ByteReadChannel, coroutineContext: CoroutineContext): ByteReadChannel {
        return ZstdInputStream(source.toInputStream()).toByteReadChannel(coroutineContext)
    }

    override fun encode(source: ByteReadChannel, coroutineContext: CoroutineContext): ByteReadChannel {
        return source.encodeZstd()
    }

    override fun encode(source: ByteWriteChannel, coroutineContext: CoroutineContext): ByteWriteChannel {
        return source.encodeZstd()
    }
}

private suspend fun ByteReadChannel.encodeZstdTo(destination: ByteWriteChannel) {
    val outputStream = ByteArrayOutputStream()
    val zstdOS = ZstdOutputStream(outputStream)
    zstdOS.use { zstd ->
        while (!this.isClosedForRead && !destination.isClosedForWrite) {
            val packet = this.readRemaining(DEFAULT_HTTP_BUFFER_SIZE.toLong())
            while (!packet.exhausted()) {
                zstd.write(packet.readByteArray())
            }
        }
    }
    outputStream.use {
        destination.writeFully(it.toByteArray())
    }
}

@OptIn(DelicateCoroutinesApi::class)
private fun ByteReadChannel.encodeZstd(
    coroutineContext: CoroutineContext = Dispatchers.Unconfined
): ByteReadChannel = GlobalScope.writer(coroutineContext, autoFlush = true) {
    this@encodeZstd.encodeZstdTo(channel)
}.channel

@OptIn(DelicateCoroutinesApi::class)
private fun ByteWriteChannel.encodeZstd(
    coroutineContext: CoroutineContext = Dispatchers.Unconfined
): ByteWriteChannel = GlobalScope.reader(coroutineContext, autoFlush = true) {
    channel.encodeZstdTo(this@encodeZstd)
}.channel
