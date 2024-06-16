package com.example.quiztime

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quiztime.databinding.ActivityMainBinding
import com.example.quiztime.databinding.ActivityQuizBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startQuizBtn.setOnClickListener{
            val intent = Intent(this,QuizActivity::class.java)
            startActivity(intent)
        }
    }
}