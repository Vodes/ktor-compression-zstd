package pw.vodes.zstd

import io.ktor.client.plugins.compression.*
import io.ktor.server.plugins.compression.*
import io.ktor.util.*

expect object ZstdEncoder : ContentEncoder, Encoder

fun CompressionConfig.zstd(block: CompressionEncoderBuilder.() -> Unit = {}) {
    encoder(ZstdEncoder, block)
}

fun ContentEncodingConfig.zstd(quality: Float? = null) {
    customEncoder(ZstdEncoder, quality)
}