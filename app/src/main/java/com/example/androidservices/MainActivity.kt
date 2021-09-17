package com.example.androidservices

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.core.content.ContextCompat
import com.example.androidservices.databinding.ActivityMainBinding
import com.example.androidservices.helper.onClick
import com.example.androidservices.helper.secondsToTime
import com.example.androidservices.model.TimerState
import com.example.androidservices.services.NOTIFICATION_TEXT
import com.example.androidservices.services.SERVICE_COMMAND
import com.example.androidservices.services.TimerService

/**
 * Main Screen
 */

const val TIMER_ACTION = "TimerAction"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Foreground receiver
    private val timerReceiver: TimerReceiver by lazy { TimerReceiver() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListners()

        registerBroadcast()
    }

    private fun registerBroadcast() {
        registerReceiver(timerReceiver, IntentFilter(TIMER_ACTION))
    }

    private fun setClickListners() {
        binding.btnStartForegroundService.onClick {
            sendCommandToForegroundService(TimerState.START)
        }
    }

    private fun updateUi(elapsedTime: Int) {
        binding.tvTimer.text = elapsedTime.secondsToTime()
    }

    inner class TimerReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == TIMER_ACTION) updateUi(intent.getIntExtra(NOTIFICATION_TEXT, 0))
        }
    }

    private fun sendCommandToForegroundService(timerState: TimerState) {
        ContextCompat.startForegroundService(this, getServiceIntent(timerState))
    }

    private fun getServiceIntent(command: TimerState) =
        Intent(this, TimerService::class.java).apply {
            putExtra(SERVICE_COMMAND, command as Parcelable)
        }
}