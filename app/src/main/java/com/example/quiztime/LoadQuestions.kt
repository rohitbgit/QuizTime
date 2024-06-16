package com.example.quiztime

import android.content.Context
import com.google.gson.Gson

object LoadQuestions {

    var data = emptyArray<QuizModel>()

    fun loadQuestions(context: Context){
        val inputStream =context.assets.open("questions.json")
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        val json = String(buffer,Charsets.UTF_8)
        val gson = Gson()
        data = gson.fromJson( json,Array<QuizModel>::class.java)

    }
}