package com.github.se.stepquest.map

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocationAreaTest {

  private lateinit var locationArea: LocationArea
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var mockRouteID: DataSnapshot
  private lateinit var mockRouteDataSnapshot: DataSnapshot
  private lateinit var mockRoutes: DatabaseReference
  private lateinit var mockDatabaseReference: DatabaseReference
  private lateinit var mockDatabase: FirebaseDatabase
  private lateinit var mockTask: Task<DataSnapshot>
  private lateinit var context: Context

  private lateinit var emulatedDatabase: FirebaseDatabase

  @Before
  fun setUp() {
    context = mockk<Context>(relaxed = true)
    locationArea = LocationArea(context)

    firebaseAuth = mockk()
    mockRouteID = mockk(relaxed = true)
    mockRouteDataSnapshot = mockk(relaxed = true)
    mockRoutes = mockk(relaxed = true)
    mockDatabaseReference = mockk(relaxed = true)
    mockDatabase = mockk(relaxed = true)
    mockTask = mockk(relaxed = true)

    val context = ApplicationProvider.getApplicationContext<Context>()
    val options =
        FirebaseOptions.Builder()
            .setApplicationId("1:316177260128:android:d6da82112d5626348d2d05")
            .setApiKey("AIzaSyB7BOcOCQ5f-A3HtoXH6O8cynAryQ3zFjE")
            .setDatabaseUrl("http://127.0.0.1:9000/?ns=stepquest-4de5e")
            .build()
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context, options)
    }
  }
  /*
    @After
    fun cleanup() {
      emulatedDatabase.reference.setValue(null)
    }
  */
  @Test
  fun createArea_setsCenterAndRadius() {
    val locationDetails = LocationDetails(1.0, 1.0)
    locationArea.setArea(locationDetails)
    assertEquals(LatLng(1.0, 1.0), locationArea.center)
    assertEquals(1000.0, locationArea.radius, 0.0)
  }

  @Test
  fun checkInsideArea_returnsTrueWhenLocationIsInside() {
    val locationDetails = LocationDetails(0.0, 0.0)
    locationArea.setArea(LocationDetails(0.0, 0.0))
    assertTrue(locationArea.checkInsideArea(locationDetails))
  }

  @Test
  fun checkInsideArea_returnsFalseWhenLocationIsOutside() {
    val locationDetails = LocationDetails(10.0, 10.0)
    locationArea.setArea(LocationDetails(0.0, 0.0))
    assertFalse(locationArea.checkInsideArea(locationDetails))
  }

  @Test
  fun locationArea_initialization_setsCorrectValues() {
    val center = LocationDetails(1.0, 1.0)
    val radius = 1000.0
    locationArea.setArea(center, radius)

    assertEquals(center.latitude, locationArea.center.latitude, 0.0)
    assertEquals(center.longitude, locationArea.center.longitude, 0.0)
    assertEquals(radius, locationArea.radius, 0.0)
  }

  /*
    @Test
    fun drawRoutesOnMapTest() {
      val googleMap: GoogleMap = mockk(relaxed = true)
      val locationArea = LocationArea()
      locationArea.setArea(LocationDetails(0.0, 0.0), 1000.0)
      every { locationArea.routesAroundLocation(call) } answers
          {
            val callback = arg<(List<LocationDetails>) -> Unit>(0)
            callback.invoke(listOf(LocationDetails(0.0, 0.0)))
          }

      locationArea.drawRoutesOnMap(googleMap)

    }
  */
  /*
  @Test
  fun routesAroundLocation_returnsRoutesWhenLocationIsInside() {
    val locationDetails = LocationDetails(0.0, 0.0)
    val googleMap: GoogleMap = mockk(relaxed = true)

    val localRouteList = mutableListOf<LocationDetails>()
    locationArea.setArea(locationDetails)
    val dataSnapshot = mockk<DataSnapshot>(relaxed = true)
    every { mockDatabase.reference } returns mockk { every { child(any()) } returns mockRoutes }
    every { mockRoutes.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)

          listener.onDataChange(
              mockk {
                (every { dataSnapshot.children } returns
                        listOf(
                         mockk {
                          every { child(any()) } returns
                                  mockk { every { child(any()) } returns mockRouteDataSnapshot }
                          every { mockRouteDataSnapshot.child("latitude") } returns
                                  mockk { every { getValue(Double::class.java) } returns 0.0 }
                          every { mockRouteDataSnapshot.child("longitude") } returns
                                  mockk { every { getValue(Double::class.java) } returns 0.0 }
                        }
                      )).toString()
              })
        }

    locationArea.routesAroundLocation{ localRouteList.addAll(it) }
    // verify(exactly = 1) { mockRoutes.addListenerForSingleValueEvent(any()) }
    // assertTrue(localRouteList[0].latitude == 0.0)
  }
  */
  /*
    object FirebaseDatabaseInstance {
      val instance: FirebaseDatabase by lazy {
        val database = FirebaseDatabase.getInstance()
        database.useEmulator("10.0.2.2", 9000)
        database
      }
    }

  @Test
  fun database_emulator() {
    val database = Firebase.database
    val host = if (Platform.isAndroid) "10.0.2.2" else "localhost"
    try {
      database.useEmulator(host, 9000)
    } catch (e: IllegalStateException) {}

    locationArea.database = database

    val ref = database.reference

    val route = ref.child("routes").child("route").child("0")
    val lon = route.child("longitude")
    route.child("latitude").setValue(0.0)
    route.child("longitude").setValue(0.0)
    locationArea.setArea(LocationDetails(0.0, 0.0), 1000.0)
    runBlocking {
      // Wait for the value to be set
      while (true) {
        val snapshot = lon.get().await()
        if (snapshot.exists()) {
          // Value is set, break out of the loop
          break
        }
      }
    }
    val localRouteList = mutableListOf<LocationDetails>()
    val localRouteDetailList = mutableListOf<RouteDetails>()

    locationArea.routesAroundLocation { routeList, routeDetailList ->
      localRouteList.addAll(routeList)
      localRouteDetailList.addAll(routeDetailList)
    }
    assertTrue(localRouteList[0].latitude == 0.0)
    assertTrue(localRouteList[0].longitude == 0.0)
  }
  */
}
