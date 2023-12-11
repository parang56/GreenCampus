package com.example.carrotchegg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class FindPwActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    View back_Arrow;
    TextView next_Btn, error_msg;
    TextInputEditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_pw);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.rectangle_email);
        // Minimize keyboard when click somewhere that is not keyboard.
        setupTouchListener(email);

        // Go back to login screen
        back_Arrow = findViewById(R.id.back_arrow);
        back_Arrow.setOnClickListener(v -> finish());

        error_msg = findViewById(R.id.find_email_error_msg);

        //일단은 성공 토스트를 띄우는 걸로 함.
        next_Btn = findViewById(R.id.find_pw_btn);
        next_Btn.setOnClickListener(v -> {
            // If conflict msg is showing, first make it invisible.
            if(error_msg.getVisibility() == View.VISIBLE)
                error_msg.setVisibility(View.INVISIBLE);

            String email_Str = Objects.requireNonNull(email.getText()).toString();
            // Check Id conflict
            if (error_msg.getVisibility() == View.INVISIBLE) {
                db.collection("userData").whereEqualTo("email", email_Str).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot document = task.getResult();
                                    // If a document with the specified email exists, send a password reset email
                                    if (document != null && !document.isEmpty()) {
                                        FirebaseAuth.getInstance().sendPasswordResetEmail(email_Str)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(FindPwActivity.this, "이메일을 성공적으로 보냈습니다", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                    }else {
                                        error_msg.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
            }
        });
    }

    // Hide keyboard
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final TextInputEditText rectangleEmail) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    rectangleEmail.clearFocus();
                }
            });
        }
    }
}
