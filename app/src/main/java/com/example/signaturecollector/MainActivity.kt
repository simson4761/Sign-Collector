package com.example.signaturecollector

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


import com.example.signaturecollector.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputLayout
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var name : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.eraseButton.setOnClickListener{
            binding.signatureView.clearCanvas()
        }

        binding.saveButton.setOnClickListener {
            if (binding.signatureView.isBitmapEmpty){
                Toast.makeText(this,"Empty Canvas",Toast.LENGTH_SHORT).show()
            }
            else{
                getSignatureName(this, binding.signatureView.signatureBitmap)

            }
        }
    }

    private fun getSignatureName(context: Context, signatureBitmap: Bitmap){
        val textInputLayout = TextInputLayout(context)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val input = EditText(context)
        textInputLayout.hint = "Name"
        textInputLayout.addView(input)

        val alert = AlertDialog.Builder(context)
            .setTitle("Name your Signature")
            .setView(textInputLayout)
            .setPositiveButton("Submit") { dialog, _ ->
                name = input.text.toString()
                saveSignature(name,signatureBitmap)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()


    }

    private fun checkWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun saveSignature(name: String, signatureBitmap: Bitmap) {
        if(checkWritePermission()){
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.TITLE, name)
                put(MediaStore.Images.Media.DESCRIPTION, "description")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

            }

            val resolver = this.contentResolver
            var outputStream: OutputStream? = null

            try {
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    outputStream = resolver.openOutputStream(uri)
                    signatureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream!!)
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                    binding.signatureView.clearCanvas()
                } ?: run {
                    Toast.makeText(this, "Failed to create new MediaStore record.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                outputStream?.close()
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            saveSignature(name,signatureBitmap)
        }
    }
}