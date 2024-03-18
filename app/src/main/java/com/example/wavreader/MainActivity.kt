package com.example.wavreader

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var btnPickFile: Button
    private lateinit var textHeaderInfo: TextView
    private val PICK_FILE_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPickFile = findViewById(R.id.btn_pick_file)
        textHeaderInfo = findViewById(R.id.text_header_info)

        btnPickFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val headerInfo = readWaveHeader(uri)
                textHeaderInfo.text = headerInfo
            }
        }
    }

    private fun readWaveHeader(path: Uri): String {
        val inputStream = contentResolver.openInputStream(path)
        val header = ByteArray(44)
        inputStream?.read(header)
        inputStream?.close()

        val marker = String(header.copyOfRange(0, 4))
        val fileSize = ByteBuffer.wrap(header.copyOfRange(4, 8)).order(ByteOrder.LITTLE_ENDIAN).int
        val fileTypeHeader = String(header.copyOfRange(8, 12))
        val formatChunkMarker = String(header.copyOfRange(12, 16))
        val formatDataLength = ByteBuffer.wrap(header.copyOfRange(16, 20)).order(ByteOrder.LITTLE_ENDIAN).int
        val formatType = ByteBuffer.wrap(header.copyOfRange(20, 22)).order(ByteOrder.LITTLE_ENDIAN).short
        val numChannels = ByteBuffer.wrap(header.copyOfRange(22, 24)).order(ByteOrder.LITTLE_ENDIAN).short
        val sampleRate = ByteBuffer.wrap(header.copyOfRange(24, 28)).order(ByteOrder.LITTLE_ENDIAN).int
        val byteRate = ByteBuffer.wrap(header.copyOfRange(28, 32)).order(ByteOrder.LITTLE_ENDIAN).int
        val blockAlign = ByteBuffer.wrap(header.copyOfRange(32, 34)).order(ByteOrder.LITTLE_ENDIAN).short
        val bitsPerSample = ByteBuffer.wrap(header.copyOfRange(34, 36)).order(ByteOrder.LITTLE_ENDIAN).short
        val dataChunkHeader = String(header.copyOfRange(36, 40))
        val dataSize = ByteBuffer.wrap(header.copyOfRange(40, 44)).order(ByteOrder.LITTLE_ENDIAN).int

        return """
            Marker: $marker
            File Size: $fileSize bytes
            File Type Header: $fileTypeHeader
            Format Chunk Marker: $formatChunkMarker
            Format Data Length: $formatDataLength
            Format Type: $formatType
            Number of Channels: $numChannels
            Sample Rate: $sampleRate Hz
            Byte Rate: $byteRate
            Block Align: $blockAlign
            Bits Per Sample: $bitsPerSample
            Data Chunk Header: $dataChunkHeader
            Data Size: $dataSize bytes
        """
    }
}
