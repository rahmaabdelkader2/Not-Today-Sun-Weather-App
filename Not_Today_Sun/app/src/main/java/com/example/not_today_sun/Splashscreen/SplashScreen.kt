package com.example.not_today_sun.Splashscreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.not_today_sun.MainActivity
import com.example.not_today_sun.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.animationView.addAnimatorListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {
            }
            override fun onAnimationEnd(animation: android.animation.Animator) {
                navigateToInitialScreen()  // navigate to InitialScreen when animation ends
            }
            override fun onAnimationCancel(animation: android.animation.Animator) {
                navigateToInitialScreen()
            }
            override fun onAnimationRepeat(animation: android.animation.Animator) {
            }
        })
    }

    private fun navigateToInitialScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}