import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.se.stepquest.map.getPhotoFile
import com.github.se.stepquest.map.rotatePicture
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoFileTest {

  private val context = ApplicationProvider.getApplicationContext<Context>()

  @Test
  fun getPhotoFile_createsFileInExpectedLocation() {
    val photoFile = getPhotoFile(context)

    assertTrue(photoFile.exists())
    assertTrue(photoFile.absolutePath.contains("/storage/emulated/0/Android/data/"))
  }

  @Test
  fun rotatePicture_outputIsValid() {
    val photoFile = getPhotoFile(context)
    val uri = Uri.fromFile(photoFile)

    rotatePicture(context, uri, photoFile) { bitmap ->
      assertNotNull(bitmap)
      assertTrue(bitmap is Bitmap)
    }
  }
}
