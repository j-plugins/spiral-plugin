package com.github.xepozz.spiral.common.index

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.DataExternalizer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.io.ObjectInput
import java.io.ObjectInputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream

class ObjectStreamDataExternalizer<T : Any> : DataExternalizer<T> {
    @Throws(IOException::class)
    override fun save(out: DataOutput, value: T?) {
        val stream = ByteArrayOutputStream()
        val output: ObjectOutput = ObjectOutputStream(stream)

        output.writeObject(value)

        out.writeInt(stream.size())
        out.write(stream.toByteArray())
    }

    @Throws(IOException::class)
    override fun read(`in`: DataInput): T? {
        val bufferSize = `in`.readInt()
        val buffer = ByteArray(bufferSize)
        `in`.readFully(buffer, 0, bufferSize)

        val stream = ByteArrayInputStream(buffer)
        val input: ObjectInput = ObjectInputStream(stream)

        return try {
            @Suppress("UNCHECKED_CAST")
            input.readObject() as T
        } catch (e: ClassNotFoundException) {
            LOG.warn("Failed to deserialize index value: class not found", e)
            null
        } catch (e: ClassCastException) {
            LOG.warn("Failed to deserialize index value: unexpected type", e)
            null
        }
    }

    companion object {
        private val LOG = Logger.getInstance(ObjectStreamDataExternalizer::class.java)
    }
}