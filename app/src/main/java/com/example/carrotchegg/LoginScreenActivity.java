package com.example.carrotchegg;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.content.IntentSender;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginScreenActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String TAG = "UILab";
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> oneTapUILauncher;
    View LoginBtn;
    TextView Registration, login_error_msg, login_error_msg_id, login_error_msg_pw, findId, resetPw;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        SharedPreferences sharedPreferences = getSharedPreferences("First", MODE_PRIVATE);

        //최초 실행이라면
        if(sharedPreferences.getBoolean("first", true))
        {
            sharedPreferences.edit().putBoolean("first", false).apply();

            //푸쉬 알림 묻기
            AlertDialog.Builder alter = new AlertDialog.Builder(LoginScreenActivity.this);
            alter.setTitle("푸쉬 알림")
                    .setMessage("푸쉬 알림을 받으시겠습니까?\n받으시려면 권한을 허용해 주셔야 합니다.")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            //33이하는 자동으로 권한이 주어진대
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                //현재 권한이 없다면 권한 요청
                                if (!(ContextCompat.checkSelfPermission(LoginScreenActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                                    ActivityCompat.requestPermissions(LoginScreenActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
                                }
                                else Toast.makeText(LoginScreenActivity.this, "푸시 알림 설정 완료!", Toast.LENGTH_LONG).show();
                            }
                        }})
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //해제는 아마 상진이가 추가로 짜줘야 할듯
                            Toast.makeText(LoginScreenActivity.this, "내 프로필 -> 알림 설정에서 다시 설정 가능합니다.", Toast.LENGTH_LONG).show();
                        }})
                    .show();
        }




        // Retrieve message from RegistrationActivity
        String toastMessage = getIntent().getStringExtra("toastMessage");

        // Check if there's a message and show the Toast
        if (toastMessage != null) {
            Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        configOneTapSignUpOrSignInClient();
        initFirebaseAuth();

        // Google Login
        /*SignInButton signInButton = findViewById(R.id.google_login_btn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });*/

        EditText rectangleLogin = findViewById(R.id.rectangle_id);
        EditText rectanglePW = findViewById(R.id.rectangle_pw);
        login_error_msg = findViewById(R.id.login_error_msg);
        login_error_msg_id = findViewById(R.id.login_error_msg_id);
        login_error_msg_pw = findViewById(R.id.login_error_msg_pw);

        // Minimize keyboard when click somewhere that is not keyboard.
        setupTouchListener(rectangleLogin, rectanglePW);

        LoginBtn = findViewById(R.id.login_btn);
        LoginBtn.setOnClickListener(v -> {
            // Set login error messages invisible in case of previous failed attempt
            login_error_msg.setVisibility(View.INVISIBLE);
            login_error_msg_id.setVisibility(View.INVISIBLE);
            login_error_msg_pw.setVisibility(View.INVISIBLE);

            // Get input from login and password to string
            String emailOrUserId = rectangleLogin.getText().toString();
            String password = rectanglePW.getText().toString();

            // Check if email or password is null or empty
            if (emailOrUserId.isEmpty() || password.isEmpty()) {
                login_error_msg.setVisibility(View.VISIBLE);
                return;
            }

            // Check if the input looks like an email
            if (emailOrUserId.contains("@")) {
                // Firebase code for login with email
                mAuth.signInWithEmailAndPassword(emailOrUserId, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                handleLoginResult(task);
                            }
                        });
            }
            else{
                // Login code for login with id
                db.collection("userData").whereEqualTo("id", emailOrUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task.getResult();
                                    // If id exists
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        // Iterate over the documents
                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                                String email = Objects.requireNonNull(document.get("email")).toString();
                                                    mAuth.signInWithEmailAndPassword(email, password)
                                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                                // User matches data
                                                                if (task.isSuccessful()) {
                                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                                    Log.d("MJ", "signIn:success");
                                                                    assert user != null;
                                                                    // User is verified
                                                                    if(user.isEmailVerified()){
                                                                        Intent intent = new Intent(getApplicationContext(), MainMainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                    // User isn't verified, show toast.
                                                                    else{
                                                                        Toast.makeText(LoginScreenActivity.this, "이메일 인증을 하셔야 합니다.", Toast.LENGTH_LONG).show();
                                                                    }
                                                                }else {
                                                                    // Password doesn't match
                                                                    Log.d("MJ", "signIn:failure_pw", task.getException());
                                                                    login_error_msg_pw.setVisibility(View.VISIBLE);
                                                                }
                                                            }
                                                        });
                                                return;  // Exit the loop after successful login
                                            }
                                        }
                                    } else {
                                    // Id doesn't exist
                                    Log.d("MJ", "signIn:failure_id", task.getException());
                                    login_error_msg_id.setVisibility(View.VISIBLE);
                                }
                            }
                        });

            }
        });

        // 회원가입 페이지
        Registration = findViewById(R.id.registration);
        Registration.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Terms_And_Conditions.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        /*// 아이디 찾기 페이지로 이동
        findId = findViewById(R.id.find_id);
        findId.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FindIdActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });*/

        // 비번 찾기 페이지로 이동
        resetPw = findViewById(R.id.change_pw);
        resetPw.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FindPwActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
    }

    private void handleLoginResult(Task<?> task) {
        if (task.isSuccessful()) {
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signIn:success");
            Intent intent = new Intent(getApplicationContext(), MainMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "signIn:failure", task.getException());
            login_error_msg.setVisibility(View.VISIBLE);
        }
    }

    //푸쉬 알람 권한 허용, 허용안함 선택 여부
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1234){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){ //알림 설정 완료
                Toast.makeText(LoginScreenActivity.this, "푸쉬 알림 설정 완료!", Toast.LENGTH_LONG).show();
            }
            else{ //알림 설정 실패
                Toast.makeText(LoginScreenActivity.this, "푸쉬 알림을 받으시려면 권한을 허용해 주세요!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final EditText loginEditText, final EditText passwordEditText) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    loginEditText.clearFocus();
                    passwordEditText.clearFocus();
                }
            });

            // Prevent touch events on the EditText fields from propagating to the root view
            loginEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });

            passwordEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
    }

    private void configOneTapSignUpOrSignInClient() {
        oneTapClient = Identity.getSignInClient(this);

        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        //.setFilterByAuthorizedAccounts(true) // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false) // Show all accounts on the device.
                        .build())
                .build();

        oneTapUILauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google. Use it to authenticat with Firebase.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        mAuth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(LoginScreenActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            String uid = user.getUid();
                                            // Reference to the document with the specified UID
                                            DocumentReference userDocumentRef = db.collection("userData").document(uid);
                                            userDocumentRef.get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot document = task.getResult();
                                                                // If the document with the specified UID exists
                                                                if (document != null && document.exists()) {
                                                                    // Document with the specified UID already exists
                                                                    // Just login.
                                                                    Intent intent = new Intent(getApplicationContext(), MainMainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    // Document with the specified UID does not exist
                                                                    // Go to social registration.
                                                                    Intent intent = new Intent(getApplicationContext(), SocialRegistration.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                    startActivity(intent);
                                                                }
                                                            } else {
                                                                // An error occurred while checking for the document
                                                                Exception exception = task.getException();
                                                                if (exception != null) {
                                                                    // Handle the exception (log, show error message, etc.)
                                                                    exception.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                    });

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                                            login_error_msg.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
    }

    // https://developers.google.com/identity/one-tap/android/get-saved-credentials
    private void signIn() {
        // check whether the user has any saved credentials for your app. -> onSuccess or onFailure
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        IntentSender intentSender = beginSignInResult.getPendingIntent().getIntentSender();
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                        oneTapUILauncher.launch(intentSenderRequest);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }
}
