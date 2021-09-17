package com.example.androidservices.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.example.androidservices.R
import com.example.androidservices.TIMER_ACTION
import com.example.androidservices.helper.NotificationHelper
import com.example.androidservices.helper.secondsToTime
import com.example.androidservices.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

const val SERVICE_COMMAND = "Command"
const val NOTIFICATION_TEXT = "NotificationText"

class TimerService : Service(), CoroutineScope {

    var serviceState: TimerState = TimerState.INITIALIZED
    private val helper by lazy { NotificationHelper(this) }
    private var currentTime: Int = 0
    private var startedAtTimestamp: Int = 0
        set(value) {
            currentTime = value
            field = value
        }

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = object : Runnable {
        override fun run() {
            currentTime++
            broadcastUpdate()
            // Repeat every 1 second
            handler.postDelayed(this, 1000)
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private fun startTimer(elapsedTime: Int? = null) {
        serviceState = TimerState.START

        startedAtTimestamp = elapsedTime ?: 0

        // publish notification
        startForeground(NotificationHelper.NOTIFICATION_ID, helper.getNotification())

        broadcastUpdate()

        startCoroutineTimer()
    }

    private fun broadcastUpdate() {
        // update notification
        if (serviceState == TimerState.START) {
            // count elapsed time
            val elapsedTime = (currentTime - startedAtTimestamp)

            // send time to update UI
            sendBroadcast(
                Intent(TIMER_ACTION)
                    .putExtra(NOTIFICATION_TEXT, elapsedTime)
            )
            helper.updateNotification(
                getString(R.string.time_is_running, elapsedTime.secondsToTime())
            )

        } else if (serviceState == TimerState.PAUSE) {
            helper.updateNotification(getString(R.string.get_back))
        }
    }

    private fun pauseTimerService() {
        serviceState = TimerState.PAUSE
        handler.removeCallbacks(runnable)
        broadcastUpdate()
    }

    private fun endTimerService() {
        serviceState = TimerState.STOP
        handler.removeCallbacks(runnable)
        job.cancel()
        broadcastUpdate()
        stopService()
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    private fun startCoroutineTimer() {
        launch(coroutineContext) {
            handler.post(runnable)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.extras?.run {
            when (getSerializable(SERVICE_COMMAND) as TimerState) {
                TimerState.START -> startTimer()
                TimerState.PAUSE -> pauseTimerService()
                TimerState.STOP -> endTimerService()
                else -> return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        job.cancel()
    }
}