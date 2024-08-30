package com.github.se.stepquest.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.github.se.stepquest.R
import com.github.se.stepquest.services.addPoints
import com.github.se.stepquest.services.getUsername
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class FollowRoute private constructor() {

  var userOnRoute = MutableLiveData<Boolean>()
  // Define a Job to manage the coroutine
  private var checkRouteJob: Job? = null
  var followingRoute = MutableLiveData<Boolean>()
  var show_follow_route_button = MutableLiveData<Boolean>()
  var RouteDetail = MutableLiveData<RouteDetails>()
  var clickedCheckpoints = mutableListOf<LocationDetails>()
  private var currentToast: Toast? = null
  private lateinit var clickedMarker: Marker
  private lateinit var locationViewModel: LocationViewModel
  private lateinit var context: Context
  private var checkpointDialog: AlertDialog? = null

  init {
    followingRoute.postValue(false)
    show_follow_route_button.postValue(false)
    stopCheckIfOnRoute()
  }

  companion object {
    const val REQUEST_IMAGE_CAPTURE = 1

    @SuppressLint("StaticFieldLeak") @Volatile private var INSTANCE: FollowRoute? = null

    fun getInstance(): FollowRoute {
      return INSTANCE
          ?: synchronized(this) {
            val instance = FollowRoute()
            INSTANCE = instance
            instance
          }
    }
  }

  @SuppressLint("PotentialBehaviorOverride", "InflateParams")
  fun drawRouteDetail(
      googleMap: GoogleMap,
      context: Context,
      onClear: () -> Unit,
      locationViewModel: LocationViewModel
  ) {
    googleMap.setOnMarkerClickListener { clickedMarker ->
      if (clickedMarker.title == "Route") {

        val route = clickedMarker.tag as? RouteDetails
        route?.let { it ->
          RouteDetail.postValue(it)
          val routeID = it.routeID
          val routedetail = it.routeDetails
          val routeUserID = it.userID
          val checkpoints = it.checkpoints
          val points = routedetail?.map { LatLng(it.latitude, it.longitude) }
          if (points != null) {
            show_follow_route_button.postValue(true)
            // clean up the map
            cleanGoogleMap(googleMap, onClear = onClear)
            if (points.isNotEmpty()) {
              // Add a red marker at the start point
              googleMap.addMarker(
                  MarkerOptions()
                      .position(points.first())
                      .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                      .title("Start Point"))

              // Add a green marker at the end point if there are multiple points
              if (points.size > 1) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(points.last())
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_GREEN))
                        .title("End Point"))

                // Draw a polyline connecting all the points
                googleMap.addPolyline(PolylineOptions().addAll(points).color(Color.BLUE).width(5f))
              }
            }
          }

          // Add checkpoints to the map
          checkpoints?.forEach { checkpoint ->
            googleMap
                .addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(checkpoint.location.latitude, checkpoint.location.longitude))
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_ORANGE))
                        .title("Checkpoint"))
                ?.tag = checkpoint
          }
        }
      } else if (clickedMarker.title == "Checkpoint") {
        // Handle checkpoint click, show image and title
        val checkpoint = clickedMarker.tag as? Checkpoint

        val checkpointLocation =
            LocationDetails(clickedMarker.position.latitude, clickedMarker.position.longitude)
        val currentLocation = locationViewModel.currentLocation.value

        // Display checkpoint dialog if user is following a route
        if (followingRoute.value!!) {
          checkpoint?.let {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view: android.view.View
            // Check if the checkpoint has an image
            if (it.image.isEmpty()) {
              view = inflater.inflate(R.layout.checkpoint_without_image, null)
            } else {
              // Check if the user is close to the checkpoint
              if (compareCheckpoints(checkpointLocation, currentLocation!!, 20f) == -1f) {
                // Don't display camera button
                view = inflater.inflate(R.layout.checkpoint_with_image_far, null)
                val imageView: ImageView = view.findViewById(R.id.dialog_image)
                BitmapFactory.decodeStream(it.image.inputStream()).also { bitmap ->
                  imageView.setImageBitmap(bitmap)
                }
              } else {
                // Display camera button
                view = inflater.inflate(R.layout.checkpoint_with_image, null)
                val imageView: ImageView = view.findViewById(R.id.dialog_image)
                BitmapFactory.decodeStream(it.image.inputStream()).also { bitmap ->
                  imageView.setImageBitmap(bitmap)
                }
                val button: Button = view.findViewById(R.id.dialog_button)
                button.setOnClickListener {
                  if (clickedCheckpoints.contains(
                      LocationDetails(checkpointLocation.latitude, checkpointLocation.longitude))) {
                    Toast.makeText(
                            context,
                            "You have already taken a picture of this checkpoint",
                            Toast.LENGTH_SHORT)
                        .show()
                  } else {
                    this.clickedMarker = clickedMarker
                    this.locationViewModel = locationViewModel
                    this.context = context
                    dispatchTakePictureIntent(context as Activity)
                  }
                }
              }
            }

            checkpointDialog =
                builder
                    .setView(view)
                    .setTitle(it.name) // Set the title of the dialog to the checkpoint title
                    .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                    .create()
            checkpointDialog?.show()
          }
        }
      }
      true // Return true to indicate that we have handled the event
    }
  }
  // taking the points on the route with the interval of 20 meters
  fun createTrackpoint(interval: Double = 20.0): List<LocationDetails> {
    val route = RouteDetail.value?.routeDetails
    val trackpoints = mutableListOf<LocationDetails>()
    if (route is List<LocationDetails> && route.isNotEmpty()) {
      Log.d("FollowRoute", "Route: $route")
      Log.d("FollowRoute", "final point: ${route.last()}")
      trackpoints.add(route.first())
      var currentLocation = route[0]
      var totaldistance = 0.0
      for (i in 1 until route.size) {
        val distance = calculateDistance(currentLocation, route[i])
        totaldistance += distance
        if (totaldistance >= interval) {
          trackpoints.add(route[i])
          totaldistance = 0.0
        }
        currentLocation = route[i]
      }

      trackpoints.add(route.last())
      Log.d("FollowRoute", "trackpoints: $trackpoints")
    }
    return trackpoints
  }

  fun checkIfOnRoute(
      locationViewModel: LocationViewModel,
      context: Context,
      onGoBackBUttonClick: () -> Unit,
      threshold_onRoute: Double = 30.0,
      threshold_arrived: Double = 5.0,
      threshold_outRoute: Double = 50.0
  ) {
    // Cancel any existing job before starting a new one
    checkRouteJob?.cancel()
    val trackpoints = createTrackpoint()
    // Launch a new coroutine in the background
    checkRouteJob =
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
          var remainingTrackpoints = trackpoints
          while (remainingTrackpoints.isNotEmpty() &&
              isActive) { // Check if the coroutine is still active
            val trackpoint = remainingTrackpoints[0]
            val finish_point = trackpoints.last()
            val currentLocation = locationViewModel.currentLocation.value
            if (currentLocation == null) {
              withContext(Dispatchers.Main) {
                Log.d("FollowRoute", "Current location is null, stopping the coroutine")
              }
            } else {
              val distance = calculateDistance(trackpoint, currentLocation)
              Log.d("FollowRoute", "Distance: $distance\n")
              val finish_distance = calculateDistance(finish_point, currentLocation)
              Log.d("FollowRoute", "Finish Distance: $finish_distance\n")
              if (finish_distance <= threshold_arrived) {
                // check reach final point or not
                withContext(Dispatchers.Main) {
                  userOnRoute.value = true
                  // Give points to the user
                  val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
                  val routeLength = calculateRouteLength(RouteDetail.value!!.routeDetails!!)
                  val reward = routeLength.toInt().floorDiv(100) + clickedCheckpoints.size * 5 + 10
                  getUsername(currentUser) { addPoints(it, reward) }

                  AlertDialog.Builder(context)
                      .apply {
                        setTitle("Finish route")
                        setMessage(
                            "Congratulation! You have reached the finish point. You have earned $reward points.")
                        setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                      }
                      .create()
                      .show()
                  Log.d("FollowRoute", "You have reached the finish point. Congrats")
                  onGoBackBUttonClick()
                }
              } else {
                // If the distance is less than 5 meters, the user has passed this point
                if (distance <= threshold_arrived) {
                  withContext(Dispatchers.Main) {
                    userOnRoute.value = true
                    Log.d("FollowRoute", "You have reached the next point")
                  }
                  remainingTrackpoints = remainingTrackpoints.drop(1)
                } else if (distance <= threshold_onRoute && distance > threshold_arrived) {
                  withContext(Dispatchers.Main) {
                    userOnRoute.postValue(true)
                    Log.d("FollowRoute", "You are on the route")
                  }
                } else if (distance <= threshold_outRoute && distance > threshold_onRoute) {
                  withContext(Dispatchers.Main) {
                    userOnRoute.postValue(false)

                    // Check if there is already a Toast being shown and cancel it
                    currentToast?.cancel()
                    // Show a new Toast
                    currentToast =
                        Toast.makeText(
                            context,
                            "Warning: You are off the route, please go back to route.",
                            Toast.LENGTH_LONG)
                    currentToast?.show()

                    Log.d("FollowRoute", "Warning: You are off the route")
                  }
                } else if (distance > threshold_outRoute) {
                  withContext(Dispatchers.Main) {
                    userOnRoute.postValue(false)
                    AlertDialog.Builder(context)
                        .apply {
                          setTitle("Too far from route")
                          setMessage(
                              "You are too far from the route, please come closer to the start point (red marker) and start the route again. Bye!")
                          setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
                        }
                        .create()
                        .show()
                    Log.d("FollowRoute", "You are off the route, Bye")
                    onGoBackBUttonClick()
                  }
                }
              }
            }
            // Optionally, add a delay to prevent continuous checking
            delay(1000)
          }
        }
  }

  // Function to stop the checkIfOnRoute coroutine
  fun stopCheckIfOnRoute() {
    checkRouteJob?.cancel()
  }

  private fun dispatchTakePictureIntent(activity: Activity) {
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) !=
        PackageManager.PERMISSION_GRANTED) {
      // Permission is not granted
      ActivityCompat.requestPermissions(
          activity, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
    } else {
      // Permission has already been granted
      Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        takePictureIntent.resolveActivity(activity.packageManager)?.also {
          activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
      }
    }
  }

  fun onPictureTaken() {
    val marker = this.clickedMarker

    val checkpointLocation = LocationDetails(marker.position.latitude, marker.position.longitude)
    val pictureLocation = locationViewModel.currentLocation.value
    val distance = compareCheckpoints(checkpointLocation, pictureLocation!!)
    if (distance == -1f) {
      Toast.makeText(context, "The pictures don't match! Try again", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "The picture match! Congratulations!", Toast.LENGTH_SHORT).show()
      clickedCheckpoints.add(checkpointLocation)
      checkpointDialog?.dismiss()
    }
  }
}
