package com.example.carrotchegg;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MyProfileActivity extends AppCompatActivity {

    private ImageButton btnBarMain, btnBarScrap, btnBarNotification;

    private TextView txtNickname, txtLevel, txtNeedExp, txtExpPercent, question_number, answer_number;
    private TextView txtAlarmSetting, txtAlarmSetting2, txtDeleteAccount, txtQuestionList, txtAnswerList, logoutBtn;
    private EditText txtIntroduction;
    private ImageView btnChangeImage;
    private de.hdodenhof.circleimageview.CircleImageView userImage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    RelativeLayout rootView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private long level, expPer, exp;

    private ImageView imgExpBar;
    private long expMax[] = {0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);

        logoutBtn = findViewById(R.id.logout);

        logoutBtn.setOnClickListener(v->{
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });


        btnBarMain = (ImageButton) findViewById(R.id.barMain);
        btnBarScrap = (ImageButton) findViewById(R.id.barScape);
        btnBarNotification = (ImageButton) findViewById(R.id.barNotification);

        btnBarMain.setOnClickListener(v -> {
            finish();
        });

        btnBarScrap.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ScrapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });

        btnBarNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });

        txtNickname = (TextView) findViewById(R.id.nickname);
        txtIntroduction = (EditText) findViewById(R.id.introduction);
        txtLevel = (TextView) findViewById(R.id.level);
        txtNeedExp = (TextView) findViewById(R.id.needExp);
        txtExpPercent = (TextView) findViewById(R.id.expPercent);

        imgExpBar = (ImageView) findViewById(R.id.expBar);

        RelativeLayout rootView = findViewById(R.id.root_view);


        //계정 정보 가져와서 뷰 완성하기
        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    txtNickname.setText(task.getResult().getData().get("nickname").toString());
                    txtIntroduction.setText(task.getResult().getData().get("introduction").toString());

                    level = (long) task.getResult().getData().get("level");
                    exp = (long) task.getResult().getData().get("exp");

                    txtLevel.setText("현재 레벨 : " + Long.toString(level));
                    txtNeedExp.setText("다음 레벨까지 남은 경험치 : " + (Long.toString(expMax[(int) level] - exp))); //임시
                    txtExpPercent.setText((Double.toString((double) exp / expMax[(int) level] * 100)) + "%");


                    //경험치바
                    FrameLayout.LayoutParams imgParams;
                    imgParams = new FrameLayout.LayoutParams(
                            (int) ((double) exp / expMax[(int) level] * (int) getResources().getDimension(R.dimen.ExpW)),
                            (int) getResources().getDimension(R.dimen.ExpH)
                    );
                    imgExpBar.setLayoutParams(imgParams);

                    //유저 이미지
                    userImage = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.myPhoto);
                    FirebaseStorage.getInstance().getReference("user_image").child(uid + ".png")
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(MyProfileActivity.this).load(uri).into(userImage);
                                }
                            });
                    //질문수
                    txtQuestionList.setText(task.getResult().getData().get("question")+"\n질문 수");
                    //답변수
                    txtAnswerList.setText(task.getResult().getData().get("answer")+"\n답변 수");
                }
            }
        });

        //프로필 사진 변경
        btnChangeImage = findViewById(R.id.myPhoto);
        btnChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 갤러리에서 이미지 선택
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);
                activityResultLauncher.launch(intent);
            }
        });

        txtAlarmSetting = (TextView) findViewById(R.id.alarmSetting);
        txtAlarmSetting2 = (TextView) findViewById(R.id.alarmSetting2);
        txtDeleteAccount = (TextView) findViewById(R.id.deleteAccount);
        txtQuestionList = (TextView) findViewById(R.id.questionList);
        txtAnswerList = (TextView) findViewById(R.id.answerList);


        txtIntroduction.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that characters within `start` and `before` are about to be replaced with new text with a length of `after`.
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that somewhere within `start` and `before`, the text has been replaced with new text that has a length of `after`.
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called when the text within `EditText` has been changed.
                String newIntroduction = editable.toString();

                // Update the introduction in the database
                db.collection("userData").document(uid).update("introduction", newIntroduction);
            }
        });


        //계정삭제
        txtDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alter = new AlertDialog.Builder(MyProfileActivity.this);

                alter.setTitle("정보");
                alter.setMessage("정말 탈퇴하시겠습니까?");

                alter.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 계정 삭제
                        user.delete();
                        Toast.makeText(getApplicationContext(), "이용해주셔서 감사합니다", Toast.LENGTH_SHORT).show();
                        
                        finishAffinity();
                        System.runFinalization();
                        System.exit(0);
                    }
                });
                alter.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alter.show();
            }
        });

        //푸쉬 알림 설정
        txtAlarmSetting.setOnClickListener(v -> {
            AlertDialog.Builder alter = new AlertDialog.Builder(MyProfileActivity.this);

            alter.setTitle("푸쉬 알림 설정")
                .setMessage("푸쉬 알림을 받으시겠습니까?\n푸쉬 알림을 받으시려면 권한을 허용해 주셔야 합니다.")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    //33이하는 자동으로 권한이 주어진대
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        //현재 권한이 없다면 권한 요청
                        if (!(ContextCompat.checkSelfPermission(MyProfileActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                            ActivityCompat.requestPermissions(MyProfileActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1234);
                        }
                        else Toast.makeText(MyProfileActivity.this, "푸쉬 알림 설정 완료!", Toast.LENGTH_LONG).show();
                    }
                    db.collection("userData").document(uid).update("push_alarm", true);
                }})
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //해제는 아마 상진이가 추가로 짜줘야 할듯
                    Toast.makeText(MyProfileActivity.this, "푸쉬 알림 해제 완료!", Toast.LENGTH_LONG).show();
                    db.collection("userData").document(uid).update("push_alarm", false);
                }})
                .show();
        });

        //앱 내 알림 설정
        txtAlarmSetting2.setOnClickListener(v -> {
            AlertDialog.Builder alter = new AlertDialog.Builder(MyProfileActivity.this);

            alter.setTitle("앱 내 알림 설정")
                    .setMessage("앱 내 알림을 받으시겠습니까?")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MyProfileActivity.this, "앱 내 알림 설정 완료!", Toast.LENGTH_LONG).show();
                            db.collection("userData").document(uid).update("alarm", true);
                        }})
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MyProfileActivity.this, "앱 내 알림 해제 완료!", Toast.LENGTH_LONG).show();
                            db.collection("userData").document(uid).update("alarm", false);
                        }})
                    .show();
        });

        //질문목록 수정 완
        txtQuestionList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        //답변목록 수정 완
        txtAnswerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AnswerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear focus from the EditText when the root view is clicked
                txtIntroduction.clearFocus();

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        Uri uri = intent.getData();
                        userImage.setImageURI(uri);
                        //파일 확장자에 대해 생각해볼 필요?

                        //파이어베이스 등록
                        storageRef.child("user_image").child(uid + ".png").putFile(uri);
                    }
                }
            }
    );


    //알람 권한 허용, 허용안함 선택 여부
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1234){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){ //알림 설정 완료
                Toast.makeText(MyProfileActivity.this, "푸쉬 알림 설정 완료!", Toast.LENGTH_LONG).show();
            }
            else{ //알림 설정 실패
                Toast.makeText(MyProfileActivity.this, "푸쉬 알림을 받으시려면 알림 권한을 허용해 주세요!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }



    //엑티비티 종료 엑티비티 제거
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    // Hide keyboard
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final EditText txtIntroduction) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    txtIntroduction.clearFocus();
                }
            });
        }
    }

}