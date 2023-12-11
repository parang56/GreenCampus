package com.example.carrotchegg;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SplashTheme); // Apply your custom splash theme
        setContentView(R.layout.splash_screen);

        // Delayed handler to open the main activity after a specified time
        new Handler().postDelayed(() -> {
            // Check if the user is already logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // User is already logged in, navigate to the main activity
                Intent intent = new Intent(SplashScreenActivity.this, MainMainActivity.class);
                startActivity(intent);
            } else {
                // If not logged in, continue to the login screen
                Intent intent = new Intent(SplashScreenActivity.this, LoginScreenActivity.class);
                startActivity(intent);
            }

            finish();  // Optional: finish the current activity
        }, SPLASH_DELAY);
    }
}
