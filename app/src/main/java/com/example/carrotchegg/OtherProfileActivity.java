package com.example.carrotchegg;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class OtherProfileActivity extends AppCompatActivity {


    private TextView txtNickname, txtIntroduction, txtLevel, txtNeedExp, txtExpPercent;
    private TextView txtAlarmSetting, txtDeleteAccount, txtQuestionList, txtAnswerList;
    private ImageView btnChangeImage;
    private de.hdodenhof.circleimageview.CircleImageView userImage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    ;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid;
    private String otherUid;
    private long level, expPer, exp;

    private ImageView imgExpBar, btnBack;
    private long expMax[] = {0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_profile);

        uid = getIntent().getStringExtra("uid");

        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> {
            finish();
        });


        txtNickname = (TextView) findViewById(R.id.nickname);
        txtIntroduction = (TextView) findViewById(R.id.introduction);
        txtLevel = (TextView) findViewById(R.id.level);
        txtNeedExp = (TextView) findViewById(R.id.needExp);
        txtExpPercent = (TextView) findViewById(R.id.expPercent);

        imgExpBar = (ImageView) findViewById(R.id.expBar);


        //상대 계정 정보 가져와서 뷰 완성하기
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
                    storageRef.child("user_image").child(uid + ".png")
                            .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(OtherProfileActivity.this).load(uri).into(userImage);
                                }
                            });

                    //질문수
                    txtQuestionList.setText(task.getResult().getData().get("question")+"\n질문 수");
                    //답변수
                    txtAnswerList.setText(task.getResult().getData().get("answer")+"\n답변 수");
                }
            }
        });

        txtQuestionList = (TextView) findViewById(R.id.questionList);
        txtAnswerList = (TextView) findViewById(R.id.answerList);

        //상대 질문목록
        txtQuestionList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //uid를 직접 갖지 말고 전부 보내고 받는 방식으로 바꾸면 파일들 단순화 가능
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

        //상대 답변목록
        txtAnswerList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AnswerActivity.class);
                intent.putExtra("uid", uid);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }


    //엑티비티 종료 엑티비티 제거
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

}