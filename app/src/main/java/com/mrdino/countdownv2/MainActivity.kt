package com.mrdino.countdownv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {
    private lateinit var timer: CountDownTimer
    private var isWorkoutRunning: Boolean = false
    private var isRestRunning: Boolean = false
    private var currentInterval: Int = 1
    private var numberOfIntervals: Int = 0
    private var workoutDuration: Long = 0
    private var restDuration: Long = 0

    private lateinit var numberOfIntervalsEditText: EditText
    private lateinit var workoutDurationEditText: EditText
    private lateinit var restDurationEditText: EditText
    private lateinit var workoutProgressBar: ProgressBar
    private lateinit var restProgressBar: ProgressBar
    private lateinit var timerTextView: TextView
    private lateinit var reminderTextView: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        numberOfIntervalsEditText = findViewById(R.id.number_of_intervals_edit_text)
        workoutDurationEditText = findViewById(R.id.workout_duration_edit_text)
        restDurationEditText = findViewById(R.id.rest_duration_edit_text)
        workoutProgressBar = findViewById(R.id.workout_progress_bar)
        restProgressBar = findViewById(R.id.rest_progress_bar)
        timerTextView = findViewById(R.id.timer_text_view)
        reminderTextView = findViewById(R.id.textView5)

        startButton = findViewById<Button>(R.id.start_button)
        resetButton = findViewById<Button>(R.id.reset_button)

        startButton.setOnClickListener {
            if (isWorkoutRunning) {
                pauseTimer()
            } else if (validateInputs()){
                     startTimer()
                }
            }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        // Get input values
        numberOfIntervals = numberOfIntervalsEditText.text.toString().toInt()
        workoutDuration = workoutDurationEditText.text.toString().toLong() * 1000
        restDuration = restDurationEditText.text.toString().toLong() * 1000

        // Set up notification channel
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "workout_channel"
        val channelName = "Workout Channel"
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)

        // Start workout timer
        isWorkoutRunning = true
        timer = object : CountDownTimer(workoutDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = millisUntilFinished / 1000
                workoutProgressBar.progress = progress.toInt()
                timerTextView.text = "$progress seconds remaining"
                hideKeyboard()
            }

            override fun onFinish() {
                isWorkoutRunning = false
                startButton.text = "Start"
                startButton.visibility = View.INVISIBLE
                resetButton.visibility = View.VISIBLE
                restProgressBar.max = (restDuration / 1000).toInt()
                restProgressBar.progress = restProgressBar.max
                reminderTextView.text = "Rest time!"
                startRestTimer()
            }
        }.start()

        isWorkoutRunning = true
        startButton.text = "Pause"
        resetButton.visibility = View.INVISIBLE
    }

    private fun startRestTimer() {
        // Start rest timer
        isRestRunning = true
        timer = object : CountDownTimer(restDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = millisUntilFinished / 1000
                restProgressBar.progress = progress.toInt()
                timerTextView.text = "$progress seconds remaining"
            }

            override fun onFinish() {
                isRestRunning = false
                startButton.text = "Start"
                startButton.visibility = View.VISIBLE
                resetButton.visibility = View.VISIBLE
                currentInterval++
                if (currentInterval > numberOfIntervals) {
                    timerTextView.text = "Workout complete!"
                    notifyUser()
                    reminderTextView.text =""
                } else {
                    workoutProgressBar.max = (workoutDuration / 1000).toInt()
                    workoutProgressBar.progress = workoutProgressBar.max
                    reminderTextView.text = "Interval $currentInterval - Let's go!"
                    startTimer()
                }
            }
        }.start()
        isRestRunning = true
        startButton.text = "Pause"
        startButton.visibility = View.VISIBLE
        resetButton.visibility = View.INVISIBLE
    }

    private fun notifyUser() {
        // Send notification when workout is complete
        val notificationId = 1
        val notification = NotificationCompat.Builder(this, "workout_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Workout complete!")
            .setContentText("Congratulations on completing your workout!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    //hide keyboard upon clicking start button
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken,0)
        }
    }

    private fun pauseTimer(){
        timer.cancel()
        isWorkoutRunning = false
        startButton.text = "Start"
        resetButton.visibility = View.VISIBLE
    }

    private fun resetTimer() {
        workoutDurationEditText.text = null
        restDurationEditText.text = null
        numberOfIntervalsEditText.text = null
        isWorkoutRunning = false
        isRestRunning = false
        startButton.visibility = View.VISIBLE
        resetButton.visibility = View.INVISIBLE
        workoutProgressBar.progress = 0
        restProgressBar.progress = 0
    }

    //validate if inputs are empty
    private fun validateInputs(): Boolean {
        if (numberOfIntervalsEditText.text.isNullOrEmpty()) {
            numberOfIntervalsEditText.error = "Please enter the number of intervals"
            return false
        }
        if (workoutDurationEditText.text.isNullOrEmpty()) {
            workoutDurationEditText.error = "Please enter the workout duration"
            return false
        }
        if (restDurationEditText.text.isNullOrEmpty()) {
            restDurationEditText.error = "Please enter the rest duration"
            return false
        }
        return true
    }
}