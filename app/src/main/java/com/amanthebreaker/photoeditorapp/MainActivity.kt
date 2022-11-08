package com.amanthebreaker.photoeditorapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.amanthebreaker.photoeditorapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        actionBar?.hide()
        try {
            Handler().postDelayed({
                startActivity(Intent(this@MainActivity,EditImageActivity::class.java))
                finish()
            },3000)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }
}