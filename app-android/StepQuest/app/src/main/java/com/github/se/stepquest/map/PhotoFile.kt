package com.github.se.stepquest.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

fun getPhotoFile(context: Context): File {
  val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
  val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
  return File.createTempFile(
      "JPEG_${timeStamp}_", /* prefix */ ".jpg", /* suffix */ storageDir /* directory */)
}

fun rotatePicture(context: Context, uri: Uri, photoFile: File, callback: (Bitmap) -> Unit) {
  var inputStream: InputStream? = null
  try {
    val bounds = BitmapFactory.Options()
    bounds.inJustDecodeBounds = true
    BitmapFactory.decodeFile(photoFile.absolutePath, bounds)
    val opts = BitmapFactory.Options()
    var bm = BitmapFactory.decodeFile(photoFile.absolutePath, opts)

    inputStream = context.contentResolver.openInputStream(uri)
    // Declare an ExifInterface object
    val exifInterface = ExifInterface(inputStream!!)
    // Get the orientation of the image
    var rotation = 0f
    val orientation =
        exifInterface.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90f
      ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180f
      ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270f
    }
    val matrix = Matrix()
    matrix.setRotate(rotation, bm.getWidth().toFloat() / 2, bm.getHeight().toFloat() / 2)
    callback(Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true))
  } catch (e: Exception) {
    e.printStackTrace()
  } finally {
    inputStream?.close()
  }
}
