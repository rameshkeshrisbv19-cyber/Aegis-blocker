package com.aegis.appblocker.service

/**
 * Minimal DNS query name extractor. Reads an IPv4/UDP/DNS packet and returns the queried
 * hostname if present, so the VPN loop can decide whether to block it.
 */
object DnsParser {

    fun extractQueriedDomain(data: ByteArray, length: Int): String? {
        try {
            if (length < 28) return null
            // IPv4 header length
            val ipHeaderLen = (data[0].toInt() and 0x0F) * 4
            val protocol = data[9].toInt() and 0xFF
            if (protocol != 17) return null // UDP only

            val udpStart = ipHeaderLen
            val destPort = ((data[udpStart + 2].toInt() and 0xFF) shl 8) or
                (data[udpStart + 3].toInt() and 0xFF)
            if (destPort != 53) return null // DNS only

            val dnsStart = udpStart + 8
            // Skip 12-byte DNS header to reach the question section.
            var pos = dnsStart + 12
            val sb = StringBuilder()
            while (pos < length) {
                val len = data[pos].toInt() and 0xFF
                if (len == 0) break
                if (sb.isNotEmpty()) sb.append('.')
                pos++
                for (i in 0 until len) {
                    if (pos >= length) return null
                    sb.append((data[pos].toInt() and 0xFF).toChar())
                    pos++
                }
            }
            return sb.toString().lowercase().ifEmpty { null }
        } catch (e: Exception) {
            return null
        }
    }
}
