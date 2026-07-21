package com.example.mesh

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Binary Packet Serialization & Frame Encodings for MeshLink.
 * Formats packets into compact binary byte buffers for low-overhead BLE/Wi-Fi transport.
 */
object BinaryPacketSerializer {

    private const val MAGIC_HEADER: Short = 0x4D4C // "ML" in ASCII
    private const val PROTOCOL_VERSION: Byte = 0x01

    fun serialize(packet: MeshPacket): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        // 1. Magic Header & Version
        dos.writeShort(MAGIC_HEADER.toInt())
        dos.writeByte(PROTOCOL_VERSION.toInt())

        // 2. Packet Headers
        dos.writeUTF(packet.packetId)
        dos.writeUTF(packet.sourceMeshId)
        dos.writeUTF(packet.destinationMeshId)
        dos.writeByte(packet.payloadType.ordinal)
        dos.writeInt(packet.ttl)
        dos.writeInt(packet.hopCount)
        dos.writeLong(packet.timestamp)
        dos.writeBoolean(packet.isStoreAndForward)

        // 3. Route Path
        dos.writeInt(packet.routePath.size)
        packet.routePath.forEach { dos.writeUTF(it) }

        // 4. Compress Payload Data
        val rawBytes = packet.encryptedData.toByteArray(Charsets.UTF_8)
        val compressedBytes = compress(rawBytes)
        dos.writeInt(compressedBytes.size)
        dos.write(compressedBytes)

        // 5. Signature
        dos.writeUTF(packet.signature)

        // 6. Checksum Calculation (SHA-256 truncated)
        val checksum = calculateChecksum(baos.toByteArray())
        dos.write(checksum)

        dos.flush()
        return baos.toByteArray()
    }

    fun deserialize(bytes: ByteArray): MeshPacket? {
        if (bytes.size < 16) return null
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)

        try {
            val magic = dis.readShort()
            if (magic != MAGIC_HEADER) return null

            val version = dis.readByte()
            if (version != PROTOCOL_VERSION) return null

            val packetId = dis.readUTF()
            val sourceMeshId = dis.readUTF()
            val destinationMeshId = dis.readUTF()
            val payloadTypeOrdinal = dis.readByte().toInt()
            val payloadType = PacketPayloadType.entries.getOrElse(payloadTypeOrdinal) { PacketPayloadType.SYSTEM_COMMAND }

            val ttl = dis.readInt()
            val hopCount = dis.readInt()
            val timestamp = dis.readLong()
            val isStoreAndForward = dis.readBoolean()

            val routeSize = dis.readInt()
            val routePath = mutableListOf<String>()
            for (i in 0 until routeSize) {
                routePath.add(dis.readUTF())
            }

            val compressedSize = dis.readInt()
            val compressedBytes = ByteArray(compressedSize)
            dis.readFully(compressedBytes)
            val decompressedBytes = decompress(compressedBytes)
            val encryptedData = String(decompressedBytes, Charsets.UTF_8)

            val signature = dis.readUTF()

            return MeshPacket(
                packetId = packetId,
                sourceMeshId = sourceMeshId,
                destinationMeshId = destinationMeshId,
                payloadType = payloadType,
                encryptedData = encryptedData,
                ttl = ttl,
                hopCount = hopCount,
                routePath = routePath,
                timestamp = timestamp,
                signature = signature,
                isStoreAndForward = isStoreAndForward
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun compress(input: ByteArray): ByteArray {
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(input)
        deflater.finish()

        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            baos.write(buffer, 0, count)
        }
        deflater.end()
        return baos.toByteArray()
    }

    private fun decompress(input: ByteArray): ByteArray {
        val inflater = Inflater()
        inflater.setInput(input)

        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            baos.write(buffer, 0, count)
        }
        inflater.end()
        return baos.toByteArray()
    }

    private fun calculateChecksum(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val fullHash = digest.digest(data)
        return fullHash.copyOf(4) // 4 byte checksum
    }
}
