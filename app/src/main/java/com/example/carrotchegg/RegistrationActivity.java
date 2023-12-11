package com.example.carrotchegg;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RegistrationActivity extends Activity {

    private FirebaseAuth mAuth;
    String token;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    View back_Arrow;
    TextView next_Btn, id_error_msg, id_error_msg_conflict, pw_error_msg_length, pw_error_msg_character,
            pw_confirm_error_msg, email_error_msg, email_error_msg_conflict, nickname_error_msg_kor,
            nickname_error_msg_eng, nickname_error_msg_both, nickname_error_msg_conflict;
    TextInputEditText rectangleId, rectanglePw, rectanglePwCheck, rectangleEmail, rectangleNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Init TextViews
        id_error_msg = findViewById(R.id.error_msg_id);
        id_error_msg_conflict = findViewById(R.id.error_msg_id_conflict);
        pw_error_msg_length = findViewById(R.id.error_msg_pw_length);
        pw_error_msg_character = findViewById(R.id.error_msg_pw_character);
        pw_confirm_error_msg = findViewById(R.id.error_msg_pw_confirm);
        email_error_msg = findViewById(R.id.error_msg_email);
        email_error_msg_conflict = findViewById(R.id.error_msg_email_conflict);
        nickname_error_msg_eng = findViewById(R.id.error_msg_nickname_eng);
        nickname_error_msg_kor = findViewById(R.id.error_msg_nickname_kor);
        nickname_error_msg_both = findViewById(R.id.error_msg_nickname_both);
        nickname_error_msg_conflict = findViewById(R.id.error_msg_nickname_conflict);


        // Init each TextInputEditText
        rectangleId = findViewById(R.id.rectangle_id);
        rectanglePw = findViewById(R.id.rectangle_pw);
        rectanglePwCheck = findViewById(R.id.rectangle_pw_check);
        rectangleEmail = findViewById(R.id.rectangle_email);
        rectangleNickName = findViewById(R.id.rectangle_nickname);

        // Minimize keyboard when click somewhere that is not keyboard.
        setupTouchListener(rectangleId, rectanglePw, rectanglePwCheck, rectangleEmail, rectangleNickName);

        // Go back to Conditions_And_Terms Activity
        back_Arrow = findViewById(R.id.back_arrow);
        back_Arrow.setOnClickListener(v -> finish());

        // If no problems, show toast for successful id registration.
        // Else show error msg for corresponding error
        next_Btn = findViewById(R.id.next_button);
        next_Btn.setOnClickListener(v -> {
            // Get the input from each TextInputEditText
            String id = Objects.requireNonNull(rectangleId.getText()).toString();
            String pw = Objects.requireNonNull(rectanglePw.getText()).toString();
            String pw_confirm = Objects.requireNonNull(rectanglePwCheck.getText()).toString();
            String email = Objects.requireNonNull(rectangleEmail.getText()).toString();
            String nickname = Objects.requireNonNull(rectangleNickName.getText()).toString();

            // Validate the input fields
            if (isValidId(id) && isValidPassword(pw) == 1 && isValidPwConfirm(pw, pw_confirm)
                    && isValidEmail(email) && isValidNickName(nickname) == 1) {
                // All input fields are valid, create user
                mAuth.createUserWithEmailAndPassword(email, pw).addOnCompleteListener(createUserTask -> {
                    if (createUserTask.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        storeUserDataInFirestore(uid, nickname, id, email);

                        // Send email verification
                        user.sendEmailVerification()
                                .addOnCompleteListener(emailVerificationTask -> {
                                    if (emailVerificationTask.isSuccessful()) {
                                        // Email verification sent successfully
                                        String successMessage = "Verification email sent to " + user.getEmail();
                                        redirectToLoginScreen(LoginScreenActivity.class, successMessage);
                                    } else {
                                        // Failed to send verification email
                                        String errorMessage = "Failed to send verification email";
                                        redirectToLoginScreen(LoginScreenActivity.class, errorMessage);
                                    }
                                });
                    }
                });

            }

            // Handle invalid input fields and show appropriate error messages
            else {
                // If conflict msg is showing, first make it invisible.
                if(id_error_msg_conflict.getVisibility() == View.VISIBLE)
                    id_error_msg_conflict.setVisibility(View.INVISIBLE);

                // Check Id
                id_error_msg.setVisibility(isValidId(id) ? View.INVISIBLE : View.VISIBLE);

                // Check Id conflict
                if(id_error_msg.getVisibility() == View.INVISIBLE) {
                    db.collection("userData").whereEqualTo("id", id).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot document = task.getResult();
                                        // If a document with the specified ID exists
                                        if (document != null && !document.isEmpty()) {
                                            id_error_msg_conflict.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            });
                }

                // Check Pw
                int passwordValidationStatus = isValidPassword(pw);
                pw_error_msg_length.setVisibility(passwordValidationStatus == -1 ? View.VISIBLE : View.INVISIBLE);
                pw_error_msg_character.setVisibility(passwordValidationStatus == -2 ? View.VISIBLE : View.INVISIBLE);

                // Check Pw Confirm
                pw_confirm_error_msg.setVisibility(isValidPwConfirm(pw, pw_confirm) ? View.INVISIBLE : View.VISIBLE);

                // If conflict msg is showing, first make it invisible.
                if(email_error_msg_conflict.getVisibility() == View.VISIBLE)
                    email_error_msg_conflict.setVisibility(View.INVISIBLE);

                // Check Email
                email_error_msg.setVisibility(isValidEmail(email) ? View.INVISIBLE : View.VISIBLE);

                // Check Email conflict
                if(email_error_msg.getVisibility() == View.INVISIBLE) {
                    db.collection("userData").whereEqualTo("email", email).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot document = task.getResult();
                                        // If a document with the specified email exists
                                        if (document != null && !document.isEmpty()) {
                                            email_error_msg_conflict.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            });
                }

                // If conflict msg is showing, first make it invisible.
                if(nickname_error_msg_conflict.getVisibility() == View.VISIBLE)
                    nickname_error_msg_conflict.setVisibility(View.INVISIBLE);

                // Check NickName
                int nicknameValidationStatus = isValidNickName(nickname);
                nickname_error_msg_eng.setVisibility(nicknameValidationStatus == -1 ? View.VISIBLE : View.INVISIBLE);
                nickname_error_msg_kor.setVisibility(nicknameValidationStatus == -2 ? View.VISIBLE : View.INVISIBLE);
                nickname_error_msg_both.setVisibility(nicknameValidationStatus == -3 ? View.VISIBLE : View.INVISIBLE);

                // Check Nickname conflict
                if(nickname_error_msg_eng.getVisibility() == View.INVISIBLE && nickname_error_msg_kor.getVisibility() == View.INVISIBLE
                && nickname_error_msg_both.getVisibility() == View.INVISIBLE) {
                    db.collection("userData").whereEqualTo("nickname", nickname).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        QuerySnapshot document = task.getResult();
                                        // If a document with the specified nickname exists
                                        if (document != null && !document.isEmpty()) {
                                            nickname_error_msg_conflict.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private void redirectToLoginScreen(Class<?> targetActivity, String message) {
        Intent intent = new Intent(getApplicationContext(), targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra("toastMessage", message); // Pass the message to LoginScreenActivity
        startActivity(intent);
        finish();
    }

    private void storeUserDataInFirestore(String uid, String nickname, String id, String email) {
        // Get a reference to your Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the "userData" collection and the document with the user's UID
        DocumentReference newUserRef = db.collection("userData").document(uid);

        // Create a data object with the user's information
        Map<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("id", id);
        userData.put("email", email);
        userData.put("subject", Arrays.asList());
        userData.put("answer", 0);
        userData.put("question", 0);
        userData.put("exp", 0);
        userData.put("image", false);
        userData.put("introduction", "안녕하세요!");
        userData.put("level", 1);
        userData.put("alarm", true);
        userData.put("push_alarm", true);

        // Set the data in the Cloud Firestore document
        //유저 토큰 받아오기
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        token = task.getResult();
                        // Log and toast
                        Log.d("20201508","토큰은"+token);
                        userData.put("fcmToken",token);
                        newUserRef.set(userData);
                    }
                });
        // Set the data in the Cloud Firestore document
    }

    // Method for id validation
    private boolean isValidId(String id) {
        // Define the allowed characters for the ID (lowercase and uppercase alphabet + numbers)
        String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        // Define the length requirements
        int minLength = 2;
        int maxLength = 12;

        // Check the length of the ID
        if (id.length() < minLength || id.length() > maxLength) {
            return false;
        }

        // Check if the ID contains only alphanumeric characters
        for (char c : id.toCharArray()) {
            if (allowedCharacters.indexOf(c) == -1) {
                return false;
            }
        }

        // If all checks pass, the ID is valid
        return true;
    }

    // Method for password validation length
    private int isValidPassword(String password) {
        // Define the password length requirements
        int minLength = 6;
        int maxLength = 12;

        // Define the allowed special characters based on your company norms
        String allowedSpecialCharacters = "!@*-_?";

        // Check the length of the password
        if (password.length() < minLength || password.length() > maxLength) {
            return -1; // Special number indicating invalid length
        }

        // Check if the password contains only alphanumeric characters and allowed special characters
        String allowedCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" + allowedSpecialCharacters;
        for (char c : password.toCharArray()) {
            if (allowedCharacters.indexOf(c) == -1) {
                return -2; // Special number indicating invalid characters
            }
        }

        // Return 1 if password is valid
        return 1;
    }

    // Method for checking if pw and pw_confirm are equal
    private boolean isValidPwConfirm(String pw, String pwConfirm) {
        // Implement your password confirmation logic here
        return pw.equals(pwConfirm);
    }

    // Method for email validation
    private boolean isValidEmail(String email) {
        // For simplicity, just checking if it's not empty
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Method for nickname validation
    private int isValidNickName(String nickname) {
        // Length requirements for English and Korean
        int minLengthEnglish = 2;
        int maxLengthEnglish = 12;
        int minLengthKorean = 1;
        int maxLengthKorean = 6;

        // Check length based on language
        int length = nickname.length();
        boolean isEnglish = nickname.matches("[a-zA-Z0-9]+");
        boolean isKorean = nickname.matches("[가-힣0-9]+");

        if (isEnglish) {
            if (length < minLengthEnglish || length > maxLengthEnglish) {
                return -1; // English length out of range
            }
        } else if (isKorean) {
            if (length < minLengthKorean || length > maxLengthKorean) {
                return -2; // Korean length out of range
            }
        } else {
            return -3; // All other exceptions
        }

        // If all checks pass, the nickname is valid
        return 1; // Valid nickname
    }

    // Hide keyboard
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final TextInputEditText rectangleId, final TextInputEditText rectanglePw,
                                    final TextInputEditText rectanglePwCheck, final TextInputEditText rectangleEmail,
                                    final TextInputEditText rectangleNickName) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    rectangleId.clearFocus();
                    rectanglePw.clearFocus();
                    rectangleEmail.clearFocus();
                    rectangleNickName.clearFocus();
                    rectanglePwCheck.clearFocus();
                }
            });
        }
    }
}
