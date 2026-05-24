package com.github.xepozz.spiral.common.index

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.Serializable

class ObjectStreamDataExternalizerTest {

    private data class SerializablePayload(val value: String, val count: Int) : Serializable

    @Test
    fun saveAndReadRoundTripsValue() {
        val externalizer = ObjectStreamDataExternalizer<SerializablePayload>()
        val original = SerializablePayload("hello", 42)

        val bytes = ByteArrayOutputStream()
        externalizer.save(DataOutputStream(bytes), original)

        val read = externalizer.read(DataInputStream(ByteArrayInputStream(bytes.toByteArray())))

        assertEquals(original, read)
    }

    @Test
    fun saveAndReadRoundTripsString() {
        val externalizer = ObjectStreamDataExternalizer<String>()
        val original = "spiral-test"

        val bytes = ByteArrayOutputStream()
        externalizer.save(DataOutputStream(bytes), original)

        val read = externalizer.read(DataInputStream(ByteArrayInputStream(bytes.toByteArray())))

        assertEquals(original, read)
    }

    @Test
    fun readSwallowsNullValue() {
        val externalizer = ObjectStreamDataExternalizer<String>()
        val bytes = ByteArrayOutputStream()
        externalizer.save(DataOutputStream(bytes), null)

        val read = externalizer.read(DataInputStream(ByteArrayInputStream(bytes.toByteArray())))

        assertNull(read)
    }
}
