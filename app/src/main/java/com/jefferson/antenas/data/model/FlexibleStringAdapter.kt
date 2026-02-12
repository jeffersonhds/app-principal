package com.jefferson.antenas.data.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class FlexibleStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(`in`: JsonReader): String {
        return when (`in`.peek()) {
            JsonToken.STRING -> `in`.nextString()
            JsonToken.NUMBER -> `in`.nextString()
            JsonToken.BOOLEAN -> `in`.nextBoolean().toString()
            JsonToken.NULL -> {
                `in`.nextNull()
                ""
            }
            else -> {
                `in`.skipValue()
                ""
            }
        }
    }
}
