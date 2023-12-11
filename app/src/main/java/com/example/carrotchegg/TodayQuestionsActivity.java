package com.example.carrotchegg;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TodayQuestionsActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private ImageButton btnBarMain, btnBarScrap, btnBarNotification, btnBarMyProfile, btnBackButton;
    private List<String> subjectList;

    private ArrayList<Post> dataModels = new ArrayList();

    TestAdapter adapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //과목-포스트일 때
        setContentView(R.layout.today_questions);

        btnBarMain = (ImageButton) findViewById(R.id.barMain);
        btnBarScrap = (ImageButton) findViewById(R.id.barScape);
        btnBarNotification = (ImageButton) findViewById(R.id.barNotification);
        btnBarMyProfile = (ImageButton) findViewById(R.id.barMyProfile);
        btnBackButton = (ImageButton) findViewById(R.id.backButton);


        //하단바
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
        btnBarMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        });
        btnBackButton.setOnClickListener(v -> {
            finish();
        });

        //유저의 관심 과목들 목록부터 가져오기
        Serializable serial = getIntent().getSerializableExtra("subjectList");
        subjectList = (List<String>) serial;

        setTitle("Using FirestoreRecyclerAdapter");


        //현재 시간
        Date currentDate = new Date();

        //하루 전 시간 계산
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date oneDayAgo = calendar.getTime();

        for(int i=0; i<subjectList.size(); i++)
        {
            //구독한 과목 중 최근 24시간 글만 걸러냄
            db.collection("subjects").document(subjectList.get(i)).collection("post")
                    .orderBy("timestamp")
                    .whereGreaterThanOrEqualTo("timestamp", oneDayAgo)
                    .whereLessThanOrEqualTo("timestamp", currentDate)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        //3이상인지 따로 확인
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(Integer.parseInt(document.getData().get("heart").toString())>=3) {
                                        dataModels.add(new Post(
                                                (String) document.getData().get("title"),
                                                (String) document.getData().get("text"),
                                                (long) document.getData().get("heart"),
                                                (long) document.getData().get("answer"),
                                                (boolean) document.getData().get("image"),
                                                (String) document.getData().get("imageSource"),
                                                (long) document.getData().get("postNum")
                                        ));
                                    }
                                }
                            } else {
                                Log.d("테스트", "Error getting documents: ", task.getException());
                            }
                            //이으러 감
                            A();
                        }
                    });
        }
    }

    void A()
    {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new TestAdapter(TodayQuestionsActivity.this, dataModels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    //엑티비티 종료 엑티비티 제거
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}