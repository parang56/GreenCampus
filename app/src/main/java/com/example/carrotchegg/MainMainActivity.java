package com.example.carrotchegg;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;

import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainMainActivity extends AppCompatActivity {

    private ImageButton[] btnSubjectAdd = new ImageButton[6];
    private TextView[] txtSubjectAdd = new TextView[6];
    private int subjectCount, subjectMax;

    private ImageButton btnBarScrap, btnBarNotification, btnBarMyProfile, writeBtn;
    private Button btnTodayQuestions;
    private InputMethodManager imm;
    private EditText etMainsearch;
    private TextView txtNow, txtQuotes;
    private long initTime;
    private long expMax[] = {0, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
    private TextView txtExpPercent;
    private long level, exp;
    private ImageView imgExpBar;

    private boolean checked=false;

    private ArrayList<Post> dataModels = new ArrayList();

    private EditText mainSearch;
    private ImageButton mainSearchbtn;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private List<String> subjectList;


    private RelativeLayout todayQuestionsLayout;


    private TextView title, text, heartCnt, answerCnt;
    private ImageView image, heart, answer;


    //내가쓴건지 알기 위해 wN wS 이용


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_main);

        txtNow = (TextView) findViewById(R.id.now);

        txtNow.setText("Today : " + new SimpleDateFormat("yyyy / MM / dd").format(System.currentTimeMillis()));

        subjectMax=6;

        btnSubjectAdd[0] = (ImageButton) findViewById(R.id.mainQuickButton1);
        btnSubjectAdd[1] = (ImageButton) findViewById(R.id.mainQuickButton2);
        btnSubjectAdd[2] = (ImageButton) findViewById(R.id.mainQuickButton3);
        btnSubjectAdd[3] = (ImageButton) findViewById(R.id.mainQuickButton4);
        btnSubjectAdd[4] = (ImageButton) findViewById(R.id.mainQuickButton5);
        btnSubjectAdd[5] = (ImageButton) findViewById(R.id.mainQuickButton6);

        txtSubjectAdd[0] = (TextView) findViewById(R.id.mainQuickButton1Text);
        txtSubjectAdd[1] = (TextView) findViewById(R.id.mainQuickButton2Text);
        txtSubjectAdd[2] = (TextView) findViewById(R.id.mainQuickButton3Text);
        txtSubjectAdd[3] = (TextView) findViewById(R.id.mainQuickButton4Text);
        txtSubjectAdd[4] = (TextView) findViewById(R.id.mainQuickButton5Text);
        txtSubjectAdd[5] = (TextView) findViewById(R.id.mainQuickButton6Text);


        btnBarScrap = (ImageButton) findViewById(R.id.barScape);
        btnBarNotification = (ImageButton) findViewById(R.id.barNotification);
        btnBarMyProfile = (ImageButton) findViewById(R.id.barMyProfile);


        btnBarScrap.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ScrapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        btnBarNotification.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        btnBarMyProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MyProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        etMainsearch = (EditText)findViewById(R.id.mainSearch);

        writeBtn = findViewById(R.id.mainWriteButton);
        mainSearch = findViewById(R.id.mainSearch);
        mainSearchbtn = findViewById(R.id.mainSearchBtn);

        //오늘의 명언
        txtQuotes = (TextView) findViewById(R.id.quotes);
        db.collection("quotes").document("quotes").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> quotes = (List<String>) task.getResult().getData().get("quotes");
                    txtQuotes.setText("" + quotes.get((int)(Math.random()*quotes.size())));
                }
                else
                {
                    Log.d("명언", "가져오기 실패");
                }
            }
        });

        //오늘의 인기 질문
        todayQuestionsLayout = findViewById(R.id.todayQuestions);
        title = findViewById(R.id.title);
        text = findViewById(R.id.text);
        heartCnt= findViewById(R.id.heartCnt);
        answerCnt = findViewById(R.id.answerCnt);
        image = findViewById(R.id.image);
        heart = findViewById(R.id.heart_empty);
        answer = findViewById(R.id.answer);
        btnTodayQuestions = (Button) findViewById(R.id.todayQuestionsMoreButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //현재 권한이 없다면 권한 요청
            if (!(ContextCompat.checkSelfPermission(MainMainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                db.collection("userData").document(uid).update("push_alarm", false);
            } else db.collection("userData").document(uid).update("push_alarm", true);
        }
        else db.collection("userData").document(uid).update("push_alarm", true);
    }

    //종료 묻기 토스트
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(System.currentTimeMillis() - initTime > 2000) {
                Toast.makeText(this, "종료하시려면 버튼을 한 번 더 눌러주세요", Toast.LENGTH_LONG).show();
                initTime = System.currentTimeMillis();
            } else { finish(); }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //시작시 포커스 제거
    @Override
    protected void onStart() {
        super.onStart();

        if(getCurrentFocus()!=null) getCurrentFocus().clearFocus();
    }

    //다른 곳 클릭 시 키보드 내리기
    public void OnClick(View v) {
        imm.hideSoftInputFromWindow(etMainsearch.getWindowToken(), 0);
        etMainsearch.clearFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //딜레이 주고 메인화면으로
        //딜레이 안주니까 아이콘 추가가 잘 적용 안됨
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //계정 정보 가져오기
                db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            //경험치바
                            txtExpPercent = (TextView) findViewById(R.id.expPercent);
                            imgExpBar = (ImageView) findViewById(R.id.expBar);

                            level = (long) task.getResult().getData().get("level");
                            exp = (long) task.getResult().getData().get("exp");

                            txtExpPercent.setText((Double.toString((double) exp / expMax[(int) level] * 100)) + "%");


                            RelativeLayout.LayoutParams imgParams;
                            imgParams = new RelativeLayout.LayoutParams(
                                    (int) ((double) exp / expMax[(int) level] * (int) getResources().getDimension(R.dimen.ExpW)),
                                    (int) getResources().getDimension(R.dimen.ExpH)
                            );
                            imgParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.expBarMax);
                            imgParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.expBarMax);
                            imgExpBar.setLayoutParams(imgParams);

                            //바로 가져오면 적요이 안됨
                            //유저의 현재 즐겨찾기 목록 가져옴
                            subjectList = (List<String>) task.getResult().getData().get("subject");
                            subjectCount = subjectList.size();

                            //오늘의 질문
                            btnTodayQuestions.setOnClickListener(v -> {
                                Intent intent = new Intent(getApplicationContext(), TodayQuestionsActivity.class);
                                intent.putExtra("subjectList", (Serializable) subjectList);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                            });

                            //글작성
                            writeBtn.setOnClickListener(v -> {
                                if(subjectCount!=0) {
                                    Intent intent = new Intent(getApplicationContext(), MakeQuestion.class);
                                    intent.putExtra("subjectList", (Serializable) subjectList);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(MainMainActivity.this, "먼저 과목을 추가해 주세요!", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //즐겨찾기 과목들 아이콘 띄워주기
                            int i;
                            for (i = 0; i < subjectCount; i++) {
                                StorageReference profile = storage.getReference().child("subject_icon").child(subjectList.get(i) + ".png");
                                int finalI = i;
                                profile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //받아와서 순서대로 각 버튼에 이미지, 텍스트 넣어줌
                                        Glide.with(MainMainActivity.this).load(uri).into(btnSubjectAdd[finalI]);
                                        btnSubjectAdd[finalI].setVisibility(View.VISIBLE);
                                        txtSubjectAdd[finalI].setText(subjectList.get(finalI));

                                        btnSubjectAdd[finalI].setOnClickListener(v -> {
                                            //과목 클릭시 이동
                                            Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                                            intent.putExtra("subjectName", subjectList.get(finalI));
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                            startActivity(intent);
                                        });
                                    }
                                });
                            }

                            //남는 칸은 빈칸
                            for (; i < subjectMax; i++) {
                                btnSubjectAdd[i].setVisibility(View.INVISIBLE);
                                txtSubjectAdd[i].setText("");
                            }

                            //과목 추가 버튼
                            //subjectMax값 보다 적을 때만 가능.    6개면 추가 버튼 없음
                            if(subjectCount<subjectMax) {
                                btnSubjectAdd[subjectCount].setImageResource(R.drawable.plus);
                                btnSubjectAdd[subjectCount].setVisibility(View.VISIBLE);
                                txtSubjectAdd[subjectCount].setText("과목 추가");
                                btnSubjectAdd[subjectCount].setOnClickListener(v -> {
                                    Intent intent = new Intent(getApplicationContext(), SubjectSearchActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                });
                            }


                            //현재 시간
                            Date currentDate = new Date();

                            //하루 전 시간 계산
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(currentDate);
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                            Date oneDayAgo = calendar.getTime();


                            //랜덤순으로 과목 뽑기
                            int random[] = new int[subjectCount];
                            int randomCheck[] = new int[subjectCount];

                            for(i=0; i<subjectCount; ) {
                                random[i] = (int) (Math.random() * subjectCount);
                                if (randomCheck[random[i]] != 1) {
                                    randomCheck[random[i]] = 1;
                                    i++;
                                }

                            }

                            //자신이 즐겨찾기한 과목의 24시간 동안 하트 3이상 데이터 중 1개
                            checked=false;
                            for(i=0; i<subjectCount; i++) {
                                db.collection("subjects").document(subjectList.get(random[i])).collection("post").orderBy("timestamp")
                                        .whereGreaterThanOrEqualTo("timestamp", oneDayAgo).whereLessThanOrEqualTo("timestamp", currentDate)
                                        .limit(100).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful() && !checked) {
                                                    int taskSize = task.getResult().getDocuments().size();
                                                    if(taskSize>0)
                                                    {
                                                        //랜덤순으로 질문 뽑기
                                                        int randomPost[] = new int[taskSize];
                                                        int randomPostCheck[] = new int[taskSize];

                                                        for(int j=0; j<taskSize; ) {
                                                            randomPost[j] = (int) (Math.random() * taskSize);
                                                            if (randomPostCheck[randomPost[j]] != 1) {
                                                                j++;
                                                            }
                                                        }

                                                        for (int j=0; j<taskSize; j++) {
                                                            //하트 3이상
                                                            DocumentSnapshot document = task.getResult().getDocuments().get(randomPost[j]);
                                                            if(Integer.parseInt(document.getData().get("heart").toString())>=3) {

                                                                title.setText(document.getData().get("title").toString());
                                                                text.setText(document.getData().get("text").toString());
                                                                heartCnt.setText(document.getData().get("heart").toString());
                                                                answerCnt.setText(document.getData().get("answer").toString());
                                                                heart.setVisibility(View.VISIBLE);
                                                                answer.setVisibility(View.VISIBLE);

                                                                //이미지
                                                                if ((boolean) document.getData().get("image")) {

                                                                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                                                            (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostImage),
                                                                            (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostImage)
                                                                    );
                                                                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                                                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                                                                    params.rightMargin = (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostLayoutMargin);
                                                                    params.leftMargin = (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostLayoutMargin);
                                                                    image.setLayoutParams(params);

                                                                    StorageReference submitProfile = FirebaseStorage.getInstance().getReferenceFromUrl(document.getData().get("imageSource").toString());
                                                                    submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                        @Override
                                                                        public void onSuccess(Uri uri) {
                                                                            Log.d("1223", document.getData().get("image").toString());
                                                                            Glide.with(MainMainActivity.this).load(uri).into(image);
                                                                        }
                                                                    });
                                                                } else {
                                                                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
                                                                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                                                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                                                                    params.rightMargin = (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostLayoutMargin);
                                                                    params.leftMargin = (int) MainMainActivity.this.getResources().getDimension(R.dimen.PostLayoutMargin);

                                                                    image.setLayoutParams(params);
                                                                }

                                                                checked=true;

                                                                todayQuestionsLayout.setOnClickListener(v -> {
                                                                    //클릭 시 이동
                                                                    questionParcelableData data = new questionParcelableData(document.getData().get("subject").toString(), Integer.parseInt(document.getData().get("postNum").toString()));
                                                                    Intent intent = new Intent(MainMainActivity.this, DetailOfQuestion.class).putExtra("questionParcelableData", data);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                    MainMainActivity.this.startActivity(intent);
                                                                });
                                                            }
                                                            if(checked) break;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }


                            //검색 처리 (키보드 검색 모양)
                            mainSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                                        Intent intent = new Intent(MainMainActivity.this, SearchActivity.class);
                                        intent.putExtra("text", mainSearch.getText().toString().toLowerCase());
                                        intent.putExtra("subjectList", (Serializable) subjectList);
                                        startActivity(intent);

                                        return true;
                                    }
                                    return false;
                                }
                            });

                            //검색 처리 (검색창 검색 모양)
                            mainSearchbtn.setOnClickListener(v -> {
                                Intent intent = new Intent(MainMainActivity.this, SearchActivity.class);
                                intent.putExtra("subjectList", (Serializable) subjectList);
                                intent.putExtra("text", mainSearch.getText().toString().toLowerCase());
                                startActivity(intent);
                            });
                        }
                    }
                });
            }
        }, 300);
    }
}



