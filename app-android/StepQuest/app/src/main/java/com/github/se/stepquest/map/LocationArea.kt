package com.github.se.stepquest.map

import android.content.Context
import android.util.Log
import com.github.se.stepquest.services.cacheRouteData
import com.github.se.stepquest.services.getcacheRouteData
import com.github.se.stepquest.services.isOnline
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

class LocationArea(context: Context) {
  var firebaseAuth: FirebaseAuth
  var database: FirebaseDatabase
  var storage: FirebaseStorage

  lateinit var center: LatLng
  var radius: Double = 1000.0
  val context = context

  init {
    firebaseAuth = FirebaseAuth.getInstance()
    database = FirebaseDatabase.getInstance()
    storage = Firebase.storage
  }

  fun setArea(centerLocation: LocationDetails, radius: Double = 1000.0) {
    this.radius = radius
    this.center = LatLng(centerLocation.latitude, centerLocation.longitude)
  }

  fun routesAroundLocation(callback: (List<LocationDetails>, List<RouteDetails>) -> Unit) {
    val routeList = mutableListOf<LocationDetails>()
    val routeDetailList = mutableListOf<RouteDetails>()
    if (!isOnline(context)) {
      getcacheRouteData(context).let { (routeList, routeDetailList) ->
        callback(routeList, routeDetailList)
      }
    } else {
      val routes = database.reference.child("routes")
      var insideArea = false
      routes.addListenerForSingleValueEvent(
          object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
              for (routeID in snapshot.children) {
                // Retrieve the routeID key
                val routeKey = routeID.key.toString()

                val routeDataSnapshot_start = routeID.child("route").child("0")
                val latitude = routeDataSnapshot_start.child("latitude").getValue<Double>()
                val longitude = routeDataSnapshot_start.child("longitude").getValue<Double>()
                if (latitude != null && longitude != null) {
                  val routeData = LocationDetails(latitude, longitude)
                  if (checkInsideArea(routeData)) {
                    Log.d("LocationArea", "Route is inside area")
                    routeList.add(routeData)
                    insideArea = true
                  }
                }

                if (insideArea) {
                  // Retrieve the list of LocationDetails for the route
                  val routeDataSnapshot = routeID.child("route")
                  val routeDetailsList = mutableListOf<LocationDetails>()
                  for (locationSnapshot in routeDataSnapshot.children) {
                    val latitude = locationSnapshot.child("latitude").getValue<Double>()
                    val longitude = locationSnapshot.child("longitude").getValue<Double>()
                    if (latitude != null && longitude != null) {
                      val locationDetails = LocationDetails(latitude, longitude)
                      routeDetailsList.add(locationDetails)
                    }
                  }

                  // Retrieve the list of Checkpoints
                  val checkpointsSnapshot = routeID.child("checkpoints")
                  val checkpointsList = mutableListOf<Checkpoint>()
                  val imageDownloadTasks = mutableListOf<Task<ByteArray>>()

                  for (checkpointSnapshot in checkpointsSnapshot.children) {
                    val name = checkpointSnapshot.child("name").getValue<String>().orEmpty()
                    val locationSnapshot = checkpointSnapshot.child("location")
                    val latitude = locationSnapshot.child("latitude").getValue<Double>()
                    val longitude = locationSnapshot.child("longitude").getValue<Double>()
                    val imageURL = checkpointSnapshot.child("imageURL").getValue<String>().orEmpty()

                    if (latitude != null && longitude != null) {
                      val locationDetails = LocationDetails(latitude, longitude)
                      checkpointsList.add(Checkpoint(name, locationDetails))
                      // Group tasks to download the iamges

                      if (imageURL.isNotEmpty()) {
                        val imageRef = storage.getReferenceFromUrl(imageURL)
                        imageDownloadTasks.add(imageRef.getBytes(1024 * 1024))
                      }
                    }
                  }
                  // Retrieve all images from database
                  Tasks.whenAllSuccess<ByteArray>(imageDownloadTasks).addOnSuccessListener { images
                    ->
                    for ((index, image) in images.withIndex()) {
                      checkpointsList[index].image = image
                    }
                  }
                  // Retrieve the userID
                  val userID = routeID.child("userid").getValue<String>().orEmpty()

                  // Create RouteDetails object
                  val routeDetailData =
                      RouteDetails(routeKey, routeDetailsList, checkpointsList, userID)

                  // Add to routeDetailList
                  routeDetailList.add(routeDetailData)
                }
                insideArea = false
              }
              Log.i("Location Area", "RouteList: $routeList")
              Log.i("Location Area", "RouteDetailList: $routeDetailList")

              cacheRouteData(context, routeList, routeDetailList)

              // Call the callback with the collected data
              callback(routeList, routeDetailList)
            }

            override fun onCancelled(error: DatabaseError) {
              // Handle error here
            }
          })
    }
  }

  fun checkInsideArea(newLocation: LocationDetails): Boolean {
    val km = radius / 1000
    val kx = cos(Math.PI * center.latitude / 180) * 111
    val dx = abs(center.longitude - newLocation.longitude) * kx
    val dy = abs(center.latitude - newLocation.latitude) * 111
    return sqrt(dx * dx + dy * dy) <= km
  }

  fun drawRoutesOnMap(googleMap: GoogleMap) {
    routesAroundLocation { routes, routedetails ->
      routes.zip(routedetails).forEach { (route, routedetail) ->
        val marker =
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(route.latitude, route.longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("Route"))
        marker?.tag =
            routedetail // Storing the RouteDetail object in the tag of the marker, for displying
        // route detail
      }
    }
  }
}

data class RouteDetails(
    val routeID: String,
    val routeDetails: List<LocationDetails>?,
    val checkpoints: List<Checkpoint>?,
    val userID: String
)
