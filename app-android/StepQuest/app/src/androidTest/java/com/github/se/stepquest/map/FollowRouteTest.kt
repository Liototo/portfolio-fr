package com.github.se.stepquest.map

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any

class FollowRouteTest {
  private lateinit var followRoute: FollowRoute
  private lateinit var context: Context

  @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
  private val testDispatcher = TestCoroutineDispatcher()
  private lateinit var locationViewModel: LocationViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    followRoute = FollowRoute.getInstance()
    context = ApplicationProvider.getApplicationContext<Context>()
    locationViewModel = LocationViewModel()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    testDispatcher.cleanupTestCoroutines()
  }

  @Test
  fun testDrawRouteDetail_route() {
    // Mock dependencies
    val googleMap = mockk<GoogleMap>(relaxed = true)
    //        val context = mockk<Context>(relaxed = true)
    val clickedMarker = mockk<Marker>(relaxed = true)
    val routeDetails =
        RouteDetails(
            routeID = "1",
            routeDetails = listOf(LocationDetails(34.0, -118.0), LocationDetails(35.0, -119.0)),
            userID = "user123",
            checkpoints = listOf(Checkpoint("Checkpoint 1", LocationDetails(34.5, -118.5))))

    // Set expectations
    every { clickedMarker.title } returns "Route"
    every { clickedMarker.tag } returns routeDetails
    mockkStatic(BitmapDescriptorFactory::class)
    every { BitmapDescriptorFactory.defaultMarker(any()) } returns mockk()

    // Mocking setOnMarkerClickListener using slot
    val listenerSlot = slot<GoogleMap.OnMarkerClickListener>()
    every { googleMap.setOnMarkerClickListener(capture(listenerSlot)) } answers {}

    // Invoke function
    followRoute.drawRouteDetail(
        googleMap,
        context,
        onClear = {
          var currentMarker = null
        },
        locationViewModel)
    // Invoke the lambda with the mocked marker
    assert(listenerSlot.isCaptured)
    listenerSlot.captured.onMarkerClick(clickedMarker)
    followRoute.show_follow_route_button.value = false
  }

  @Test
  fun createTrackpoint_returnsNonEmptyTrackpoints_whenRouteIsNotEmpty() {
    // Arrange
    val routeDetails =
        listOf(
            LocationDetails(0.0, 0.0),
            LocationDetails(0.1, 0.1),
            LocationDetails(0.2, 0.2),
            LocationDetails(0.3, 0.3),
            LocationDetails(0.4, 0.4))
    val route =
        RouteDetails(routeID = "1", routeDetails = routeDetails, emptyList(), userID = "user123")
    followRoute.RouteDetail.postValue(route)

    // Act
    val trackpoints = followRoute.createTrackpoint(interval = 0.15)

    assertTrue(trackpoints.isNotEmpty())
  }

  @Test
  fun createTrackpoint_returnsEmptyList_whenRouteIsEmpty() {
    // Arrange
    val routeDetails = emptyList<LocationDetails>()
    val route =
        RouteDetails(routeID = "1", routeDetails = routeDetails, emptyList(), userID = "user123")
    followRoute.RouteDetail.postValue(route)

    // Act
    val trackpoints = followRoute.createTrackpoint(interval = 0.15)

    // Assert
    assertTrue(trackpoints.isEmpty())
  }

  @Test
  fun test_show_follow_route_isTrueIfValidPoints() {
    // Mock dependencies
    assertEquals(false, followRoute.show_follow_route_button.value)

    val googleMap = mockk<GoogleMap>(relaxed = true)
    val clickedMarker = mockk<Marker>(relaxed = true)
    val routeDetails =
        RouteDetails(
            routeID = "1",
            routeDetails = listOf(LocationDetails(34.0, -118.0), LocationDetails(35.0, -119.0)),
            userID = "user123",
            checkpoints = listOf(Checkpoint("Checkpoint 1", LocationDetails(34.5, -118.5))))

    // Set expectations
    every { clickedMarker.title } returns "Route"
    every { clickedMarker.tag } returns routeDetails
    mockkStatic(BitmapDescriptorFactory::class)
    every { BitmapDescriptorFactory.defaultMarker(any()) } returns mockk()

    // Mocking setOnMarkerClickListener using slot
    val listenerSlot = slot<GoogleMap.OnMarkerClickListener>()
    every { googleMap.setOnMarkerClickListener(capture(listenerSlot)) } answers {}

    // Invoke function
    followRoute.drawRouteDetail(
        googleMap,
        context,
        onClear = {
          var currentMarker = null
        },
        locationViewModel)

    // Simulate marker click
    listenerSlot.captured.onMarkerClick(clickedMarker)

    assertEquals(routeDetails, followRoute.RouteDetail.value)
    assertEquals(true, followRoute.show_follow_route_button.value)
    followRoute.show_follow_route_button.value = false
  }

  //  @Test
  //  fun TestcheckIfOnRoute_condition1() = runBlockingTest {
  //    // Mock dependencies
  //    val locationViewModel = mockk<LocationViewModel>(relaxed = true)
  //    val trackpoints =
  //      listOf(
  //        LocationDetails(0.0, 0.0), LocationDetails(0.0, 21.00001), LocationDetails(0.0, 40.0))
  //    val latch = CountDownLatch(1)
  //    val currentLocationLiveData = MutableLiveData<LocationDetails>()
  //    every { locationViewModel.currentLocation } returns currentLocationLiveData
  //
  //    // Set the initial location
  //    currentLocationLiveData.postValue(LocationDetails(0.0, 0.0))
  //
  //    // Start the function
  //    followRoute.checkIfOnRoute(locationViewModel, trackpoints)
  //
  //    latch.await(1, TimeUnit.SECONDS)
  //    // Verify the state of userOnRoute
  //    assertEquals(true, followRoute.userOnRoute.value)
  //
  //    // Stop the coroutine
  //    followRoute.stopCheckIfOnRoute()
  //  }
  //
  //  @Test
  //  fun TestcheckIfOnRoute_condition2() = runBlockingTest {
  //    // Mock dependencies
  //    val locationViewModel = mockk<LocationViewModel>(relaxed = true)
  //    val trackpoints = listOf(LocationDetails(0.0, 21.00001), LocationDetails(0.0, 40.0))
  //    val latch = CountDownLatch(1)
  //    val currentLocationLiveData = MutableLiveData<LocationDetails>()
  //    every { locationViewModel.currentLocation } returns currentLocationLiveData
  //
  //    // Start the function
  //    followRoute.checkIfOnRoute(locationViewModel, trackpoints)
  //
  //    // Change location to simulate moving closer to the next point
  //    currentLocationLiveData.value = LocationDetails(0.0, 21.0)
  //
  //    latch.await(1, TimeUnit.SECONDS)
  //    // Verify the state of userOnRoute
  //    assertEquals(true, followRoute.userOnRoute.value)
  //
  //    // Stop the coroutine
  //    followRoute.stopCheckIfOnRoute()
  //  }
  //
  //  @Test
  //  fun TestcheckIfOnRoute_condition3() = runBlockingTest {
  //    // Mock dependencies
  //    val locationViewModel = mockk<LocationViewModel>(relaxed = true)
  //    val trackpoints =
  //      listOf(
  //        LocationDetails(0.0, 0.0), LocationDetails(0.0, 21.00001), LocationDetails(0.0, 40.0))
  //    val latch = CountDownLatch(1)
  //    val currentLocationLiveData = MutableLiveData<LocationDetails>()
  //    every { locationViewModel.currentLocation } returns currentLocationLiveData
  //
  //    // Start the function
  //    followRoute.checkIfOnRoute(locationViewModel, trackpoints)
  //
  //    // Change location to simulate moving off the route
  //    currentLocationLiveData.value = LocationDetails(0.0, 100.0)
  //
  //    latch.await(1, TimeUnit.SECONDS)
  //    // Verify the state of userOnRoute
  //    assertEquals(false, followRoute.userOnRoute.value)
  //
  //    // Stop the coroutine
  //    followRoute.stopCheckIfOnRoute()
  //  }
  //
  //  @Test
  //  fun TestcreateTrackpoint() {
  //    val route =
  //      listOf(LocationDetails(0.0, 0.0), LocationDetails(0.0, 1.0), LocationDetails(0.0, 2.0))
  //    val followRoute = FollowRoute()
  //    mockkStatic(RouteDetails::class)
  //    every { RouteDetail.value?.routeDetails } returns route
  //    val trackpoints = followRoute.createTrackpoint(1.0)
  //    assert(trackpoints.size == 2)
  //    assert(trackpoints[0] == LocationDetails(0.0, 1.0))
  //    assert(trackpoints[1] == LocationDetails(0.0, 2.0))
  //  }

  //  @Test
  //  fun testCreateTrackpoint() {
  //    // Mock the RouteDetail and calculateDistance function
  //    val RouteDetail = mockk<MutableLiveData<RouteDetails>>(relaxed = true)
  //    val mockLocationDetailsList = listOf(
  //      LocationDetails(0.0, 0.0),
  //      LocationDetails(0.1, 0.1),
  //      LocationDetails(0.2, 0.2),
  //      LocationDetails(0.3, 0.3),
  //      LocationDetails(0.4, 0.4)
  //    )
  //
  //    every { RouteDetail.value?.routeDetails } returns mockLocationDetailsList
  //
  //    // Call the function
  //    val trackpoints = followRoute.createTrackpoint(interval = 0.15)
  //    println("trackpoints: $trackpoints")
  //
  //    // Verify the result
  //    val expectedTrackpoints = listOf(
  //      LocationDetails(0.0, 0.0),
  //      LocationDetails(0.1, 0.1),
  //      LocationDetails(0.2, 0.2),
  //      LocationDetails(0.3, 0.3),
  //      LocationDetails(0.4, 0.4)
  //    )
  //
  //    assertEquals(expectedTrackpoints, trackpoints)
  //  }
}
