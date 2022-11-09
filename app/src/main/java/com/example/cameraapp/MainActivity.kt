package com.example.cameraapp

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var imageView: ImageView
    private lateinit var imageFileName: File
    private val REQUEST_CAPTURE_IMAGE = 100
    private val MY_CAMERA_REQUEST_CODE = 101
    private var mCurrentPhotoPath: String? = null
    private var startForResult=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById<Button>(R.id.button)
        imageView = findViewById<ImageView>(R.id.imageView)
        setupPermissions()
        button.setOnClickListener {
            openCameraIntent()
        }

    }
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
            Log.i(TAG, "Permission to record denied")
        }
    }
    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA),
            MY_CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun openCameraIntent() {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )
        if (pictureIntent.resolveActivity(packageManager) != null) {
            //Create a file to store the image
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: Exception) {
                // Error occurred while creating the File
                //...
                ex.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID.toString() + ".provider",
                    photoFile
                )
                pictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    photoURI
                )
                mCurrentPhotoPath = photoFile.absolutePath


/*                startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
                { result: ActivityResult ->
                    if ( result.resultCode == RESULT_OK) {
                        //  you will get result here in result.data

                        if (imageFileName.exists()) {
                            //Resize Bitmap
                            try {
                                var mImageBitmap: Bitmap? = null

                                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                //    ImageDecoder.Source source = ImageDecoder.createSource(new File(mCurrentPhotoPath));
                                //    mImageBitmap = ImageDecoder.decodeBitmap(source);
                                //} else {
                                mImageBitmap = MediaStore.Images.Media.getBitmap(
                                    this.contentResolver, Uri.parse(
                                        "file://$mCurrentPhotoPath"
                                    )
                                )
                                //}
                                try {
                                    val outputStream: FileOutputStream = FileOutputStream(imageFileName)
                                    val _maxImageSize: Int =800
                                    val _jpgQuality: Int =80

                                    mImageBitmap = createScaledImage(mImageBitmap, _maxImageSize, _maxImageSize)
                                    mImageBitmap?.compress(Bitmap.CompressFormat.JPEG, _jpgQuality, outputStream)
                                    outputStream.flush()
                                    outputStream.close()
                                    binding.imageView.setImageBitmap(mImageBitmap)
                                    Log.i("File Saved", "File Saved on device")
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                                //mImageView.setImageBitmap(mImageBitmap);
                                //mImageView.setImageBitmap(mImageBitmap);
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                startForResult.launch(pictureIntent)*/
            }


            startActivityForResult(
                pictureIntent,
                REQUEST_CAPTURE_IMAGE
            )
        }
    }

    private fun createScaledImage(bitmap: Bitmap, _maxWidth: Int, _maxHeight: Int): Bitmap? {
        val scale = Math.min(
            _maxHeight.toFloat() / bitmap.width,
            _maxWidth.toFloat() / bitmap.height
        )
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    private fun createImageFile(): File? {
        val picturePath: String?
        val uuid = UUID.randomUUID()
        picturePath = getImgPath(
            "IMG_$uuid.png"
        )
        val pictureFile = File(picturePath)
        imageFileName= pictureFile
        return pictureFile
    }
    fun getImgPath(name: String): String? {
        val sdCard: File? =applicationContext.getExternalFilesDir(null)
        val dir = File(sdCard?.absolutePath + "/" + "images")
        if (!dir.exists()) dir.mkdirs()
        return (applicationContext.getExternalFilesDir(null)?.getAbsolutePath()
            .toString() + "/" + "images"
                + "/" + name)

    }
    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        val returnIntent = Intent()
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE &&
            resultCode == RESULT_OK
        ) {
            if (imageFileName.exists()) {
                //Resize Bitmap
                try {
                    var mImageBitmap: Bitmap? = null

                    mImageBitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver, Uri.parse(
                            "file://$mCurrentPhotoPath"
                        )
                    )
                    //}
                    try {
                        val outputStream: FileOutputStream = FileOutputStream(imageFileName)
                        val _maxImageSize: Int =800
                        val _jpgQuality: Int =80

                        mImageBitmap = createScaledImage(mImageBitmap, _maxImageSize, _maxImageSize)
                        mImageBitmap?.compress(Bitmap.CompressFormat.PNG, _jpgQuality, outputStream)
                        outputStream.flush()
                        outputStream.close()
                        imageView.setImageBitmap(mImageBitmap)

                        Log.i("File Saved", "File Saved on device")
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    //mImageView.setImageBitmap(mImageBitmap);
                    //mImageView.setImageBitmap(mImageBitmap);
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return
            }
        }
    }



}