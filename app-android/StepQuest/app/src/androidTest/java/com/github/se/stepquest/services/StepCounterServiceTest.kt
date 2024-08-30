package com.github.se.stepquest.services

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.mockk.every
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StepCounterServiceTest {

  @get:Rule val mockkRule = MockKRule(this)

  private lateinit var stepCounterService: StepCounterService
  private lateinit var sensorManager: SensorManager
  private lateinit var stepSensor: Sensor
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var database: FirebaseDatabase
  private lateinit var stepsRefTotal: DatabaseReference
  private lateinit var stepsRefDaily: DatabaseReference
  private lateinit var stepsRefWeek: DatabaseReference
  private lateinit var event: SensorEvent
  private lateinit var userRef: DatabaseReference

  private fun mockSensorEvent(): SensorEvent {
    val event = mockk<SensorEvent>()
    val constructor = SensorEvent::class.java.getDeclaredConstructor(Int::class.java)
    constructor.isAccessible = true
    constructor.newInstance(3)
    return event
  }

  @Before
  fun setUp() {
    sensorManager = mockk(relaxed = true)
    stepSensor = mockk(relaxed = true)
    firebaseAuth = mockk()
    database = mockk(relaxed = true)
    stepsRefTotal = mockk(relaxed = true)
    stepsRefDaily = mockk(relaxed = true)
    stepsRefWeek = mockk(relaxed = true)
    event = mockSensorEvent()
    event.sensor = stepSensor
    userRef = mockk(relaxed = true)

    every { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) } returns stepSensor
    every { stepSensor.type } returns Sensor.TYPE_STEP_DETECTOR

    stepCounterService =
        StepCounterService(
            sensorManager,
            firebaseAuth,
            database,
            "testUserId",
            InstrumentationRegistry.getInstrumentation().targetContext)

    every { database.reference } returns
        mockk {
          every { child("users") } returns mockk { every { child("testUserId") } returns userRef }
          every { child(any()) } returns
              mockk {
                every { child(any()) } returns
                    mockk {
                      every { child(any()) } returns
                          stepsRefTotal andThen
                          stepsRefDaily andThen
                          stepsRefWeek
                    }
              }
        }
    every { stepsRefTotal.setValue(any()) } returns mockk()
    every { stepsRefDaily.setValue(any()) } returns mockk()
    every { stepsRefWeek.setValue(any()) } returns mockk()
  }

  @Test
  fun testOnCreate() {

    val testnodeKey = "dailySteps April 11, 2024"
    val dataSnapshot = mockk<DataSnapshot>(relaxed = true)
    every { database.reference } returns userRef
    every { userRef.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(
              mockk(
                  (every { dataSnapshot.children } returns
                          listOf(
                              mockk {
                                every { key } returns testnodeKey
                                every { userRef.child(any()).removeValue() } returns mockk()
                              }))
                      .toString()))
        }
    stepCounterService.onCreate()
    verify {
      sensorManager.registerListener(
          stepCounterService, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
  }

  @Test
  fun testOnSensorChangedWithSuccess() {
    every { stepsRefTotal.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(Int::class.java) } returns 0 })
        }
    every { stepsRefDaily.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(Int::class.java) } returns 0 })
        }
    every { stepsRefWeek.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onDataChange(mockk { every { getValue(Int::class.java) } returns 0 })
        }

    stepCounterService.onSensorChanged(event)

    verify(exactly = 1) { stepsRefTotal.setValue(any()) }
    verify(exactly = 1) { stepsRefDaily.setValue(any()) }
    verify(exactly = 1) { stepsRefWeek.setValue(any()) }
  }

  @Test
  fun testOnSensorChangedWithFailure() {
    every { stepsRefTotal.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onCancelled(mockk { every { message } returns "testError" })
        }
    every { stepsRefDaily.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onCancelled(mockk { every { message } returns "testError" })
        }
    every { stepsRefWeek.addListenerForSingleValueEvent(any()) } answers
        {
          val listener = arg<ValueEventListener>(0)
          listener.onCancelled(mockk { every { message } returns "testError" })
        }

    mockkStatic(Log::class)

    every { Log.e(any(), any()) } returns 0

    stepCounterService.onSensorChanged(event)

    verify { Log.e(eq("StepCounterService"), eq("Database error: testError")) }
    verify { Log.e(eq("StepCounterService"), eq("Database error: testError")) }
  }

  @Test
  fun testOnBind() {
    assertNull(stepCounterService.onBind(null))
  }
}
