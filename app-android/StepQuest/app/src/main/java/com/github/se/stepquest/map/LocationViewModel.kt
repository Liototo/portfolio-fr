package com.github.se.stepquest.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.os.Looper
import androidx.lifecycle.*
import com.google.android.gms.location.*
import java.io.ByteArrayOutputStream

data class LocationDetails(val latitude: Double, val longitude: Double)

data class Checkpoint(
    val name: String,
    val location: LocationDetails,
    var image: ByteArray = byteArrayOf()
)

class LocationViewModel : ViewModel() {
  var locationCallback: LocationCallback? = null
  var fusedLocationClient: FusedLocationProviderClient? = null
  var currentLocation = MutableLiveData<LocationDetails>()
  var _allocations = MutableLiveData<List<LocationDetails>?>()
  var locationUpdated = MutableLiveData<Boolean>()
  var checkpoints = MutableLiveData<List<Checkpoint>>()
  var create_route_start = MutableLiveData<Boolean>()

  init {
    locationUpdated.postValue(false)
    create_route_start.postValue(false)
  }

  @SuppressLint("MissingPermission")
  fun startLocationUpdates(context: Context) {
    if (fusedLocationClient == null) {
      fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }
    locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(p0: LocationResult) {
            for (lo in p0.locations) {
              // Update UI with location data
              currentLocation.value = LocationDetails(lo.latitude, lo.longitude)

              if (create_route_start.value == true) {
                val updatedValues =
                    appendCurrentLocationToAllocations(
                        _allocations.value ?: emptyList(),
                        currentLocation.value!!,
                        locationUpdated.value!!)
                if (updatedValues != null) {
                  val (updatedAllocations, updatedLocation) = updatedValues
                  _allocations.postValue(updatedAllocations)
                  locationUpdated.postValue(updatedLocation)
                }
              }
            }
          }
        }

    locationCallback?.let {
      val locationInterval = 100
      val locationFastestInterval = 50
      val locationRequest =
          LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval.toLong())
              .setWaitForAccurateLocation(false)
              .setMinUpdateIntervalMillis(locationFastestInterval.toLong())
              .build()

      fusedLocationClient?.requestLocationUpdates(locationRequest, it, Looper.getMainLooper())
    }
  }

  fun appendCurrentLocationToAllocations(
      currentAllocations: List<LocationDetails>,
      current: LocationDetails,
      updatelocation: Boolean
  ): Pair<List<LocationDetails>, Boolean>? {
    val last = currentAllocations.lastOrNull()

    if ((last == null || calculateDistance(last, current) > 1) && !updatelocation) {
      val alllocation = currentAllocations + current
      val update = true
      return Pair(alllocation, update)
    }
    return null
  }

  fun getAllocations(): List<LocationDetails>? {
    return _allocations.value
  }

  fun addNewCheckpoint(name: String, image: Bitmap? = null): Boolean {
    val newCheckpointList = checkpoints.value?.toMutableList() ?: mutableListOf()
    val currLocation = currentLocation.value
    return if (currLocation == null) {
      false
    } else {
      val baos = ByteArrayOutputStream()
      var imageData = byteArrayOf()
      if (image != null) {
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        imageData = baos.toByteArray()
      }

      val newCheckpoint = Checkpoint(name, currentLocation.value!!, imageData)
      newCheckpointList.add(newCheckpoint)
      checkpoints.postValue(newCheckpointList)
      true
    }
  }

  fun cleanAllocations() {
    _allocations.postValue(null)
  }

  // stop update location
  fun onPause() {
    if (fusedLocationClient == null || locationCallback == null) {
      return
    } else {
      locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }
  }
}

// Function to calculate distance between two locations in meters
fun calculateDistance(location1: LocationDetails, location2: LocationDetails): Float {
  val results = FloatArray(1)
  Location.distanceBetween(
      location1.latitude, location1.longitude, location2.latitude, location2.longitude, results)
  return results[0]
}
