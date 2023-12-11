package com.example.carrotchegg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SocialRegistration extends AppCompatActivity {
    TextView nextBtn, nickname_error_msg_kor, nickname_error_msg_eng,
            nickname_error_msg_both, nickname_error_msg_conflict;;
    TextInputEditText rectangleNickname;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_social);

        // Initialize Firebase Auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        nickname_error_msg_eng = findViewById(R.id.error_msg_nickname_eng);
        nickname_error_msg_kor = findViewById(R.id.error_msg_nickname_kor);
        nickname_error_msg_both = findViewById(R.id.error_msg_nickname_both);
        nickname_error_msg_conflict = findViewById(R.id.error_msg_nickname_conflict);

        rectangleNickname = findViewById(R.id.rectangle_nickname);
        String nickname = Objects.requireNonNull(rectangleNickname.getText()).toString();

        nextBtn = findViewById(R.id.next_button);
        nextBtn.setOnClickListener(v->{
            if(isValidNickName(nickname) == 1){
                storeUserDataInFirestore(uid, nickname);
                // Go back to login page
                Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }
            else{
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

    private void storeUserDataInFirestore(String uid, String nickname) {
        // Get a reference to your Cloud Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the "userData" collection and the document with the user's UID
        DocumentReference newUserRef = db.collection("userData").document(uid);

        // Create a data object with the user's information
        Map<String, Object> userData = new HashMap<>();
        userData.put("nickname", nickname);
        userData.put("subject", Arrays.asList());

        // Set the data in the Cloud Firestore document
        newUserRef.set(userData);
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
        boolean isEnglish = nickname.matches("[a-zA-Z]+");
        boolean isKorean = nickname.matches("[가-힣]+");

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
}
