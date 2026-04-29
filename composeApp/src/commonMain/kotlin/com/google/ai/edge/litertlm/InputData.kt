package com.google.ai.edge.litertlm

sealed class InputData {
    class Text(val text: String) : InputData()
    class Audio(val bytes: ByteArray) : InputData()
    class Image(val bytes: ByteArray) : InputData()
}
