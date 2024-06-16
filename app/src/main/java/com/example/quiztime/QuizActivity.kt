package com.example.quiztime

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.quiztime.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizBinding
    private var remainingTime: Long = 600000 // 10 minutes in milliseconds
    private var currentQuestionIndex = 0
    private var selectedOptionIndex = -1
    private var questions: List<QuizModel> = listOf()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("QuizApp", MODE_PRIVATE)

        val isFreshStart = intent.getBooleanExtra("isFreshStart", true)

        LoadQuestions.loadQuestions(this)
        questions = LoadQuestions.data.toList()

        if (isFreshStart) {
            startFresh()
        } else {
            restoreState()
            startTimer()
        }

        binding.btnNext.setOnClickListener {
            saveAnswer()
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                binding.progressBar.progress = currentQuestionIndex + 1
                showQuestions()
            } else {
                completeQuiz()
            }
        }

        binding.tvOptionOne.setOnClickListener { onOptionSelected(0) }
        binding.tvOptionTwo.setOnClickListener { onOptionSelected(1) }
        binding.tvOptionThree.setOnClickListener { onOptionSelected(2) }
        binding.tvOptionFour.setOnClickListener { onOptionSelected(3) }
    }

    private fun startFresh() {
        currentQuestionIndex = 0
        remainingTime = 600000 // 10 minutes in milliseconds
        selectedOptionIndex = -1
        sharedPreferences.edit().clear().apply()
        binding.progressBar.progress = currentQuestionIndex + 1
        startTimer()
        showQuestions()
    }

    private fun restoreState() {
        currentQuestionIndex = sharedPreferences.getInt("currentQuestionIndex", 0)
        remainingTime = sharedPreferences.getLong("remainingTime", 600000)
        binding.progressBar.progress = currentQuestionIndex + 1
        showQuestions()
    }

    private fun defaultOptionsView() {
        val options = arrayListOf(binding.tvOptionOne, binding.tvOptionTwo, binding.tvOptionThree, binding.tvOptionFour)
        for (option in options) {
            option.setTextColor(Color.parseColor("#636569"))
            option.typeface = Typeface.DEFAULT
            option.background = ContextCompat.getDrawable(this, R.drawable.option_bg)
        }
    }

    private fun onOptionSelected(index: Int) {
        selectedOptionIndex = index
        val correctAnswerIndex = questions[currentQuestionIndex].answer
        defaultOptionsView()

        if (selectedOptionIndex == correctAnswerIndex) {
            when (index) {
                0 -> binding.tvOptionOne.setBackgroundResource(R.drawable.correct_option_bg)
                1 -> binding.tvOptionTwo.setBackgroundResource(R.drawable.correct_option_bg)
                2 -> binding.tvOptionThree.setBackgroundResource(R.drawable.correct_option_bg)
                3 -> binding.tvOptionFour.setBackgroundResource(R.drawable.correct_option_bg)
            }
        } else {
            when (index) {
                0 -> binding.tvOptionOne.setBackgroundResource(R.drawable.wrong_option_bg)
                1 -> binding.tvOptionTwo.setBackgroundResource(R.drawable.wrong_option_bg)
                2 -> binding.tvOptionThree.setBackgroundResource(R.drawable.wrong_option_bg)
                3 -> binding.tvOptionFour.setBackgroundResource(R.drawable.wrong_option_bg)
            }
            showCorrectAnswer()
        }
    }

    private fun showQuestions() {
        if (questions.isNotEmpty()) {
            val currentQuestion = questions[currentQuestionIndex]
            binding.tvProgress.text = "Question ${currentQuestionIndex + 1}/${questions.size}"
            binding.tvQuestion.text = currentQuestion.question

            binding.tvOptionOne.text = currentQuestion.options[0]
            binding.tvOptionTwo.text = currentQuestion.options[1]
            binding.tvOptionThree.text = currentQuestion.options[2]
            binding.tvOptionFour.text = currentQuestion.options[3]

            selectedOptionIndex = sharedPreferences.getInt("answer_$currentQuestionIndex", -1)
            if (selectedOptionIndex != -1) {
                onOptionSelected(selectedOptionIndex)
            } else {
                defaultOptionsView()
            }
        } else {
            Log.e("QuizActivity", "Questions list is empty!") // Error log
        }
    }

    private fun saveAnswer() {
        if (selectedOptionIndex != -1) {
            sharedPreferences.edit().putInt("answer_$currentQuestionIndex", selectedOptionIndex).apply()
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                sharedPreferences.edit().putLong("remainingTime", remainingTime).apply()
            }

            override fun onFinish() {
                completeQuiz()
            }
        }
        countDownTimer.start()
    }

    private fun completeQuiz() {
        showCorrectAnswer()
        countDownTimer.cancel()
        sharedPreferences.edit().clear().apply()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Quiz Completed")
        builder.setMessage("You have completed the quiz.")
        builder.setPositiveButton("OK") { _, _ ->
            finishAffinity() // Close the app and remove it from recent tasks
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun showCorrectAnswer() {
        val correctAnswerIndex = questions[currentQuestionIndex].answer
        when (correctAnswerIndex) {
            0 -> binding.tvOptionOne.setBackgroundResource(R.drawable.correct_option_bg)
            1 -> binding.tvOptionTwo.setBackgroundResource(R.drawable.correct_option_bg)
            2 -> binding.tvOptionThree.setBackgroundResource(R.drawable.correct_option_bg)
            3 -> binding.tvOptionFour.setBackgroundResource(R.drawable.correct_option_bg)
        }
    }

    override fun onPause() {
        super.onPause()
        saveAnswer()
        sharedPreferences.edit().putInt("currentQuestionIndex", currentQuestionIndex).apply()
        sharedPreferences.edit().putLong("remainingTime", remainingTime).apply()
        countDownTimer.cancel()
    }

    override fun onResume() {
        super.onResume()
        restoreState()
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            sharedPreferences.edit().clear().apply() // Clear SharedPreferences if the app is destroyed
        }
    }
}
