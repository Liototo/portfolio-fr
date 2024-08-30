package com.github.se.stepquest.map
// Map.kt

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import com.github.se.stepquest.BuildConfig
import com.github.se.stepquest.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay

data class PlaceSuggestion(val name: String, val placeId: String)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun Map(locationViewModel: LocationViewModel) {
  val context = LocalContext.current
  var stopCreatingRoute = false
  var showDialog by remember { mutableStateOf(false) }
  var checkpointTitle by remember { mutableStateOf("") }
  var routeEndMarker: Marker? = null
  val storeRoute = StoreRoute()
  var allroutes by remember { mutableStateOf("") }
  val followRoute = FollowRoute.getInstance()
  val locationArea = LocationArea(context)

  // Instantiate all necessary variables to take pictures
  var currentCheckpointHasPicture by remember { mutableStateOf(false) }
  val cameraActionPermission = remember { mutableStateOf(false) }
  val currentImage = remember { mutableStateOf<Bitmap?>(null) }

  var photoFile = getPhotoFile(context)
  val fileProvider =
      FileProvider.getUriForFile(context, "com.github.se.stepquest.map.fileprovider", photoFile)
  val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
  takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
  val resultLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
        ->
        if (result.resultCode == Activity.RESULT_OK) {
          var takenImage: Bitmap? = null
          // Sometimes the picture in portrait mode is rotated
          rotatePicture(context, fileProvider, photoFile) { takenImage = it }
          currentImage.value = takenImage
          currentCheckpointHasPicture = true
        }
      }

  var showProgression by remember { mutableStateOf(false) }
  var numCheckpoints by rememberSaveable { mutableIntStateOf(0) }

  var makingRoute by remember { mutableStateOf(false) }
  var displayButtons by remember { mutableStateOf(true) }
  val followingRoute by followRoute.followingRoute.observeAsState()
  val show_follow_route_button by followRoute.show_follow_route_button.observeAsState()

  val launcherMultiplePermissions =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
          permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        println("launcherMultiplePermissions")
        if (areGranted) {
          println("Permission Granted")
          // Start location update only if the permission asked comes from a map action
          if (!cameraActionPermission.value) {
            locationViewModel.startLocationUpdates(context)
          } else {
            cameraActionPermission.value = false
            resultLauncher.launch(takePicture)
          }
          Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
          println("Permission Denied")
          Toast.makeText(
                  context,
                  "Go to settings and activate GPS permission and camera",
                  Toast.LENGTH_SHORT)
              .show()
        }
      }
  val permissions =
      arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

  val map = remember { mutableStateOf<GoogleMap?>(null) }
  val locationUpdated by locationViewModel.locationUpdated.observeAsState()

  val apiKey = BuildConfig.MAPS_API_KEY
  Places.initialize(context.applicationContext, apiKey)
  val placesClient = Places.createClient(context)
  var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
  var searchable by remember { mutableStateOf(false) }
  var searchableLocation by remember { mutableStateOf<LatLng?>(null) }
  var currentMarker: Marker? by remember { mutableStateOf(null) }

  var onStartUp by remember { mutableStateOf(true) }

  val keyboardController = LocalSoftwareKeyboardController.current

  // Define the function with the click logic of Go Back Button
  val onGoBackBUttonClick: () -> Unit = {
    // Your click logic here
    locationViewModel.create_route_start.postValue(false)
    locationViewModel.locationUpdated.postValue(false)
    stopCreatingRoute = true
    makingRoute = false
    followRoute.show_follow_route_button.value = false
    if (followingRoute == true) {
      followRoute.stopCheckIfOnRoute()
      Log.d("FollowRoute", "stop check following route")
    }
    followRoute.followingRoute.value = false
    followRoute.clickedCheckpoints = mutableListOf()
    displayButtons = true
    locationViewModel.cleanAllocations()
    cleanGoogleMap(map.value!!, onClear = { currentMarker = null })
    Log.i("clean", "cleaned")
    numCheckpoints = 0
    currentImage.value = null
  }

  Scaffold(
      content = {
        Box(modifier = Modifier.fillMaxSize().testTag("MapScreen")) {
          // Google Map
          AndroidView(
              factory = { context ->
                MapView(context).apply {
                  onCreate(null) // Lifecycle integration
                  // Get the GoogleMap asynchronously
                  getMapAsync { googleMap ->
                    map.value = googleMap
                    Log.i("LOOKATME", "init map")
                    initMap(map.value!!)
                    locationPermission(
                        locationViewModel, context, launcherMultiplePermissions, permissions, {})
                  }
                }
              },
              modifier = Modifier.fillMaxSize().testTag("GoogleMap"))

          LaunchedEffect(locationUpdated) {
            Log.i("LOOKATME", locationUpdated.toString())
            if (locationUpdated == true) {

              // Update the map content
              updateMap(map.value!!, locationViewModel)
              locationViewModel.locationUpdated.value = false
            }
          }
          LaunchedEffect(followingRoute) {
            if (followingRoute == true) {
              Log.d("FollowRoute", "start check if on route")
              followRoute.show_follow_route_button.value = false
              followRoute.checkIfOnRoute(locationViewModel, context, onGoBackBUttonClick)
            }
          }

          LaunchedEffect(show_follow_route_button) {
            if (show_follow_route_button == true) {
              Log.d("FollowRoute", "show follow route button")
              displayButtons = false
            }
          }

          if (!makingRoute && displayButtons) {
            FloatingActionButton(
                onClick = {
                  // Before start creating route, make sure map is clean and route list (allocation)
                  // is
                  // empty too
                  cleanGoogleMap(map.value!!, routeEndMarker, onClear = { currentMarker = null })
                  locationViewModel.cleanAllocations()
                  locationPermission(
                      locationViewModel,
                      context,
                      launcherMultiplePermissions,
                      permissions,
                      { makingRoute = true })
                  locationViewModel.create_route_start.postValue(true)
                  // makingRoute = true
                },
                modifier =
                    Modifier.size(85.dp)
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = (-150).dp)
                        .testTag("createRouteButton")) {
                  Image(
                      painter = painterResource(id = R.drawable.addbutton),
                      contentDescription = "image description",
                      contentScale = ContentScale.None)
                }
            // Button for searching for routes
            FloatingActionButton(
                onClick = {
                  // CALL FUNCTIONS TO SEARCH FOR NEARBY ROUTES
                  locationArea.setArea(locationViewModel.currentLocation.value!!)
                  locationArea.drawRoutesOnMap(map.value!!)
                  map.value!!.moveCamera(
                      CameraUpdateFactory.newLatLngZoom(
                          LatLng(
                              locationViewModel.currentLocation.value!!.latitude,
                              locationViewModel.currentLocation.value!!.longitude),
                          15f))
                  followRoute.drawRouteDetail(
                      map.value!!, context, onClear = { currentMarker = null }, locationViewModel)
                },
                modifier =
                    Modifier.padding(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = (-90).dp)
                        .size(54.dp)
                        .testTag("routeSearchButton")) {
                  Box(
                      modifier = Modifier.size(48.dp).background(Color(0xff00b3ff), CircleShape),
                      contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.magnifying_icon),
                            contentDescription = "Button to search for neaby routes",
                            tint = Color.Black,
                            modifier = Modifier.size(40.dp))
                      }
                }
          } else if (makingRoute) {

            // Check point button
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier =
                    Modifier.padding(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = (-150).dp)
                        .size(48.dp)
                        .testTag("addCheckpointButton")) {
                  Box(
                      modifier = Modifier.size(48.dp).background(Color(0xff00b3ff), CircleShape),
                      contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.map_marker),
                            contentDescription = "Add checkpoint",
                            tint = Color.Red)
                      }
                }

            // Button for stopping a route
            FloatingActionButton(
                onClick = { showProgression = true },
                modifier =
                    Modifier.size(85.dp)
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = (-90).dp)
                        .testTag("stopRouteButton"),
                content = {
                  Image(
                      painter = painterResource(id = R.drawable.stopbutton),
                      contentDescription = "stop button to stop create route",
                      contentScale = ContentScale.None)
                })
          }

          // Search bar
          Column(Modifier.align(Alignment.TopCenter).offset(y = 16.dp)) {
            Box(Modifier.testTag("SearchBar")) {
              BasicTextField(
                  value = allroutes,
                  onValueChange = { searchText ->
                    searchable = false
                    allroutes = searchText
                    fetchPlaceSuggestions(placesClient, searchText, { suggestions = it }, {})
                  },
                  textStyle =
                      TextStyle(
                          fontSize = 25.sp,
                          fontWeight = FontWeight(300),
                          color = Color.Black,
                      ),
                  keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                  keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                  modifier =
                      Modifier.align(Alignment.CenterStart)
                          .background(Color.White)
                          .padding(horizontal = 12.dp)
                          .width(200.dp)
                          .height(40.dp)
                          .offset(y = 3.dp)
                          .testTag("SearchBarTextField"))
              IconButton(
                  onClick = {
                    searchable = false
                    allroutes = ""
                    suggestions = listOf()
                  },
                  modifier =
                      Modifier.align(Alignment.CenterEnd)
                          .testTag("SearchCleanButton")
                          .size(25.dp)) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.clear),
                        contentDescription = "Clear search",
                    )
                  }
              if (searchable) {
                IconButton(
                    onClick = {
                      map.value!!.moveCamera(
                          CameraUpdateFactory.newLatLngZoom(searchableLocation!!, 15f))
                      suggestions = listOf()
                      locationArea.setArea(
                          LocationDetails(
                              searchableLocation!!.latitude, searchableLocation!!.longitude))
                      locationArea.drawRoutesOnMap(map.value!!)
                      followRoute.drawRouteDetail(
                          map.value!!,
                          context,
                          onClear = { currentMarker = null },
                          locationViewModel)
                    },
                    modifier =
                        Modifier.align(Alignment.CenterEnd)
                            .offset(x = 45.dp)
                            .background(Color.White, shape = CircleShape)
                            .size(35.dp)
                            .testTag("SearchButton")) {
                      androidx.compose.material3.Icon(
                          painter = painterResource(R.drawable.search_route),
                          contentDescription = "SearchButton",
                      )
                    }
              }
            }
            DropDownMenu(
                suggestions = suggestions,
                onSuggestionSelected = { placeSuggestion ->
                  // Handle suggestion selection
                  // You might want to set the selected suggestion as the value of the text field
                  allroutes = placeSuggestion.name
                  searchable = true
                  fetchCoordinates(
                      placesClient, placeSuggestion.placeId, { searchableLocation = it }, {})
                },
            )
          }

          if (makingRoute || !displayButtons || followingRoute == true) {
            // Button for going back to default map
            FloatingActionButton(
                onClick = onGoBackBUttonClick,
                modifier =
                    Modifier.size(70.dp)
                        .padding(18.dp)
                        .offset(y = 1.dp)
                        .align(Alignment.TopStart)
                        .testTag("gobackbutton"),
                containerColor = Color.White,
                content = {
                  Image(
                      painter = painterResource(id = R.drawable.goback),
                      modifier = Modifier.size(20.dp),
                      contentDescription = "go back button")
                })
          }
          if (show_follow_route_button == true) {
            Log.d("FollowRoute", "show follow route button")
            FloatingActionButton(
                onClick = {
                  followRoute.followingRoute.value = true
                  followRoute.show_follow_route_button.value = false
                  Toast.makeText(context, "Start Following Route! Have Fun!", Toast.LENGTH_LONG)
                      .show()
                },
                modifier =
                    Modifier.size(85.dp)
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                        .offset(y = (-90).dp)
                        .testTag("followRouteButton"),
                containerColor = Color.White,
                content = {
                  Image(
                      painter = painterResource(id = R.drawable.follow_route_button),
                      modifier = Modifier.size(30.dp),
                      contentDescription = "follow route button")
                })
          }
        }
      },
      floatingActionButton = {
        if (showDialog) {
          AlertDialog(
              shape = RoundedCornerShape(16.dp),
              onDismissRequest = {
                showDialog = false
                checkpointTitle = ""
              },
              title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      "New Checkpoint",
                      style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                      modifier = Modifier.weight(1f))
                  IconButton(
                      onClick = {
                        showDialog = false
                        checkpointTitle = ""
                        currentCheckpointHasPicture = false
                        currentImage.value = null
                      },
                      modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                      }
                }
              },
              text = {
                Column(modifier = Modifier.padding(16.dp)) {
                  Text(
                      "Checkpoint name",
                      style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                      modifier = Modifier.padding(bottom = 8.dp))
                  TextField(
                      value = checkpointTitle,
                      shape = RoundedCornerShape(8.dp),
                      onValueChange = { checkpointTitle = it },
                      label = { Text("Name:") },
                      modifier = Modifier.fillMaxWidth())
                  Spacer(modifier = Modifier.height(36.dp))

                  if (currentCheckpointHasPicture) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Image(
                          bitmap = currentImage.value!!.asImageBitmap(),
                          contentDescription = "checkpoint_image",
                          modifier = Modifier.size(200.dp))
                    }
                  } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      Text(
                          text = "Take a picture",
                          style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Button to take picture
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                      IconButton(
                          onClick = {
                            if (PermissionChecker.checkSelfPermission(
                                context, Manifest.permission.CAMERA) ==
                                PermissionChecker.PERMISSION_GRANTED) {
                              resultLauncher.launch(takePicture)
                            } else {
                              cameraActionPermission.value = true
                              launcherMultiplePermissions.launch(
                                  arrayOf(Manifest.permission.CAMERA))
                            }
                          },
                          modifier = Modifier.size(70.dp)) {
                            Icon(
                                painterResource(R.drawable.camera_icon),
                                contentDescription = "camera_icon",
                                modifier = Modifier.size(50.dp))
                          }
                    }
                  }
                }
              },
              confirmButton = {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp)) {
                  Button(
                      onClick = {
                        if (locationViewModel.addNewCheckpoint(
                            checkpointTitle,
                            if (currentCheckpointHasPicture) currentImage.value else null)) {
                          // Increase checkpoint number
                          numCheckpoints++
                          // Show picture button for next checkpoint
                          currentCheckpointHasPicture = false
                        } else {
                          Toast.makeText(context, "Could not save checkpoint", Toast.LENGTH_SHORT)
                              .show()
                        }
                        showDialog = false
                        checkpointTitle = ""
                      },
                      enabled = checkpointTitle.isNotEmpty(),
                      shape = RoundedCornerShape(12.dp),
                      colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xff00b3ff)),
                      modifier = Modifier.width(150.dp).align(Alignment.Center)) {
                        Text(
                            "Confirm",
                            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
                            color = Color.White)
                      }
                }
              },
              dismissButton = { Spacer(modifier = Modifier.height(36.dp)) },
              modifier = Modifier.width(300.dp))
        }
      })

  // Open the progression screen
  if (showProgression) {
    val routeLength = calculateRouteLength(locationViewModel.getAllocations() ?: emptyList())

    RouteProgression(
        stopRoute = {
          showProgression = false
          locationViewModel.create_route_start.postValue(false)
          locationViewModel.locationUpdated.postValue(false)
          Log.i("finish locationupdate", "finish locationupdate")
          stopCreatingRoute = true
          makingRoute = false
          displayButtons = false
          routeEndMarker = updateMap(map.value!!, locationViewModel, stopCreatingRoute)
          storeRoute.addRoute(
              storeRoute.getUserid(),
              locationViewModel.getAllocations(),
              locationViewModel.checkpoints.value?.toMutableList() ?: mutableListOf())
          locationViewModel.checkpoints.postValue(mutableListOf())
          numCheckpoints = 0
        },
        closeProgression = { showProgression = false },
        routeLength,
        numCheckpoints)
  }

  LaunchedEffect(Unit) {
    while (true) {
      if (map.value != null && locationViewModel.currentLocation.value != null) {
        val coordinates =
            LatLng(
                locationViewModel.currentLocation.value!!.latitude,
                locationViewModel.currentLocation.value!!.longitude)

        if (onStartUp) {
          map.value!!.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f))
          onStartUp = false
        }

        if (currentMarker == null) {
          val customIcon = BitmapFactory.decodeResource(context.resources, R.drawable.location_dot)
          val customIconScaled = Bitmap.createScaledBitmap(customIcon, 320, 320, false)
          val icon = BitmapDescriptorFactory.fromBitmap(customIconScaled)

          currentMarker =
              map.value!!.addMarker(
                  MarkerOptions().position(coordinates).anchor(0.5f, 0.5f).icon(icon))
        } else {

          currentMarker!!.position = coordinates
        }
      }
      delay(100)
    }
  }
}

fun fetchPlaceSuggestions(
    placesClient: PlacesClient,
    query: String,
    onSuccess: (List<PlaceSuggestion>) -> Unit,
    onFailure: (Exception) -> Unit
) {
  val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()

  placesClient
      .findAutocompletePredictions(request)
      .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
        val suggestions =
            response.autocompletePredictions.map {
              PlaceSuggestion(it.getPrimaryText(null).toString(), it.placeId)
            }
        onSuccess(suggestions)
      }
      .addOnFailureListener { exception: Exception -> onFailure(exception) }
}

fun fetchCoordinates(
    placesClient: PlacesClient,
    placeId: String,
    onSuccess: (LatLng) -> Unit,
    onFailure: (Exception) -> Unit
) {
  val placeRequest = FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.LAT_LNG))

  placesClient
      .fetchPlace(placeRequest)
      .addOnSuccessListener { response: FetchPlaceResponse ->
        val place = response.place
        val latLng = place.latLng
        onSuccess(latLng!!)
      }
      .addOnFailureListener { exception: Exception -> onFailure(exception) }
}
/*
fun drawRoute(
    map: GoogleMap,
    context: GeoApiContext,
    lvm: LocationViewModel,
    destination: LatLng,
    polylineList: MutableList<Polyline>
) {
  val start = lvm.currentLocation.value!!
  val request =
      DirectionsApi.newRequest(context)
          .mode(TravelMode.WALKING)
          .origin("${start.latitude},${start.longitude}")
          .destination("${destination.latitude},${destination.longitude}")

  val directionsResult: DirectionsResult = request.await()

  if (directionsResult.routes.isNotEmpty()) {
    val route = directionsResult.routes[0]
    val polylineOptions = PolylineOptions().color(Color.Blue.toArgb()).width(5f)

    for (step in route.legs[0].steps) {
      polylineOptions.add(LatLng(step.startLocation.lat, step.startLocation.lng))
    }

    val polyline = map.addPolyline(polylineOptions)
    polylineList.add(polyline)
  }
}
*/
fun updateMap(
    googleMap: GoogleMap,
    locationViewModel: LocationViewModel,
    stopCreatingRoute: Boolean = false
): Marker? {
  val allocations = locationViewModel.getAllocations() ?: emptyList()
  println("all locations in map: $allocations")
  var routeEndMarker: Marker? = null
  if (allocations.size == 1) {
    // Add marker for the start allocation
    googleMap.addMarker(MarkerOptions().position(allocations.first().toLatLng()))
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(allocations.first().toLatLng(), 20f))
  } else if (allocations.size > 1) {
    if (!stopCreatingRoute) {

      val lastAllocation = allocations.last()
      val secondLastAllocation = allocations[allocations.size - 2]
      val polylineOptions =
          PolylineOptions().apply {
            color(Color.Blue.toArgb())
            width(10f)
            add(lastAllocation.toLatLng())
            add(secondLastAllocation.toLatLng())
          }
      googleMap.addPolyline(polylineOptions)
    } else {
      // Add marker for the end allocation
      routeEndMarker =
          googleMap.addMarker(
              MarkerOptions()
                  .position(allocations.last().toLatLng())
                  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
    }
  }
  return routeEndMarker
}

fun cleanGoogleMap(googleMap: GoogleMap, routeEndMarker: Marker? = null, onClear: () -> Unit) {
  googleMap.clear()
  onClear()
  if (routeEndMarker != null) {
    routeEndMarker.remove()
  }
}

// Extension function to convert LocationDetails to LatLng
fun LocationDetails.toLatLng(): LatLng {
  return LatLng(latitude, longitude)
}

fun initMap(googleMap: GoogleMap) {
  googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID)
  googleMap.uiSettings.isZoomControlsEnabled = true
}

fun locationPermission(
    locationViewModel: LocationViewModel,
    context: Context,
    launcherMultiplePermissions: ActivityResultLauncher<Array<String>>,
    permissions: Array<String>,
    onSucess: () -> Unit,
) {
  if (permissions.all {
    PermissionChecker.checkSelfPermission(context, it) == PermissionChecker.PERMISSION_GRANTED
  }) {
    println("Permission successful")
    // Get the location
    onSucess()
    locationViewModel.startLocationUpdates(context)
  } else {
    println("Ask Permission")
    launcherMultiplePermissions.launch(permissions)
  }
}
