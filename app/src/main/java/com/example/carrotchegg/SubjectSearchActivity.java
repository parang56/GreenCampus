package com.example.carrotchegg;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SubjectSearchActivity extends AppCompatActivity {

    private ImageButton btnBack, btnSearch;
    private InputMethodManager imm;
    private EditText etSearch;
    private TextView txtSearchResultCount;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference pathRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private LinearLayout linearLayout;
    private RelativeLayout subjectList[];
    private List<String> userSubjectList;
    private TextView addSubject;

    
    //완벽한듯

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subject_search);

        pathRef = storage.getReference().child("subject_icon");


        //back버튼
        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        //과목 요청
        addSubject = (TextView) findViewById(R.id.add_subject);
        addSubject.setOnClickListener(v -> {
            EditText input = new EditText(SubjectSearchActivity.this);
            input.setHint("필요한 과목명을 입력해주세요");

            AlertDialog.Builder dlg = new AlertDialog.Builder(SubjectSearchActivity.this);
            dlg.setTitle("과목 요청")
               .setView(input)
               .setNegativeButton("취소",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("확인",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Map<String, Object> request = new HashMap<>();
                        request.put("subject", input.getText().toString());
                        db.collection("subject request").document().set(request);
                    }})
                .show();
        });

        //키보드 관련
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        //검색창
        etSearch = (EditText) findViewById(R.id.search);

        //검색 버튼
        btnSearch = (ImageButton) findViewById(R.id.searchButton);

        //검색 결과 수
        txtSearchResultCount = (TextView) findViewById(R.id.searchResultCount);

        //검색 결과 레이아웃 받아오기
        linearLayout = (LinearLayout) findViewById(R.id.searchResult);

        //검색 처리 (키보드 검색 모양)
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //검색 결과 초기화
                    linearLayout.removeAllViews();

                    //과목 검색 + 레이아웃 생성
                    subjectSearch(etSearch.getText().toString().toLowerCase(), getApplicationContext());
                    return true;
                }
                return false;
            }
        });

        //검색 처리 (검색창 검색 모양)
        btnSearch.setOnClickListener(v -> {
            //검색 결과 초기화
            linearLayout.removeAllViews();

            //과목 검색 + 레이아웃 생성
            subjectSearch(etSearch.getText().toString().toLowerCase(), getApplicationContext());
        });

        //계정 정보 가져오기
        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    //계정 즐겨찾기 과목 가져옴
                    userSubjectList = (List<String>) task.getResult().getData().get("subject");
                }
            }
        });

    }

    //다른 곳 누르면 키보드 내리기
    public void OnClick(View v) {
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        etSearch.clearFocus();
    }

    //검색 관련 처리
    private void subjectSearch(String search, Context applicationContext) {
        db.collection("subjects")
            .whereArrayContains("name", search)
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {

                        //검색 개수만큼 레이아웃 생성
                        subjectList = new RelativeLayout[task.getResult().size()];

                        int i = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            subjectList[i] = new RelativeLayout(applicationContext);

                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) getResources().getDimension(R.dimen.SearchLayoutWight), (int) getResources().getDimension(R.dimen.SearchLayoutHeight));
                            subjectList[i].setLayoutParams(params);

                            linearLayout.addView(subjectList[i]);

                            //과목 이미지
                            ImageView subjectImage = new ImageView(applicationContext);
                            subjectImage.setId(View.generateViewId());
                            RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(
                                    (int) getResources().getDimension(R.dimen.SearchLayoutImage),
                                    (int) getResources().getDimension(R.dimen.SearchLayoutImage)
                            );
                            imgParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                            imgParams.addRule(RelativeLayout.CENTER_VERTICAL);
                            subjectImage.setLayoutParams(imgParams);
                            subjectImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            StorageReference submitProfile = pathRef.child(document.getId().toString() + ".png");
                            submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Glide.with(applicationContext).load(uri).into(subjectImage);
                                }
                            });

                            subjectList[i].addView(subjectImage);

                            //과목 이름
                            TextView titleTextView = new TextView(applicationContext);
                            titleTextView.setId(View.generateViewId());
                            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            titleParams.addRule(RelativeLayout.ALIGN_TOP, subjectImage.getId());
                            titleParams.addRule(RelativeLayout.RIGHT_OF, subjectImage.getId());
                            titleParams.leftMargin = (int) getResources().getDimension(R.dimen.SearchLayoutMargin);
                            titleTextView.setLayoutParams(titleParams);
                            titleTextView.setText(document.getId());
                            subjectList[i].addView(titleTextView);

                            //추가하기
                            Button addButton = new Button(applicationContext);
                            addButton.setId(View.generateViewId());
                            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                            btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
                            addButton.setLayoutParams(btnParams);
                            if(userSubjectList.contains(document.getId())) addButton.setText("빼기");
                            else addButton.setText("추가하기");
                            subjectList[i].addView(addButton);

                            //과목 설명 및 즐겨찾기 이용자 수
                            TextView descriptionTextView = new TextView(applicationContext);
                            descriptionTextView.setId(View.generateViewId());
                            RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            descParams.addRule(RelativeLayout.BELOW, titleTextView.getId());
                            descParams.addRule(RelativeLayout.RIGHT_OF, subjectImage.getId());
                            descParams.addRule(RelativeLayout.LEFT_OF, addButton.getId());
                            descParams.leftMargin = (int) getResources().getDimension(R.dimen.SearchLayoutMargin);
                            descParams.rightMargin = (int) getResources().getDimension(R.dimen.SearchLayoutMargin);
                            descriptionTextView.setLayoutParams(descParams);
                            descriptionTextView.setMinLines(2);
                            descriptionTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            descriptionTextView.setText(document.get("text").toString() + "\n참여자 : " + document.get("users").toString() + "명");
                            subjectList[i].addView(descriptionTextView);



                            //중복 추가 막아주기
                            addButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int check;
                                    if(userSubjectList.contains(document.getId())) check=1;
                                    else check=-1;

                                    //참여자 수 증가 or 감소
                                    db.collection("subjects").document(document.getId()).update("users", Integer.parseInt(document.get("users").toString()) - check);

                                    //회원 정보에 즐겨찾기 과목 추가 or 삭제
                                    db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if(check==1) userSubjectList.remove(document.getId());
                                                else userSubjectList.add(document.getId());
                                                db.collection("userData").document(uid).update("subject", userSubjectList);
                                            } else {
                                                Log.d("정익", "유저정보 가져오기 실패");
                                            }
                                        }
                                    });
                                    finish();
                                }
                            });
                            i++;
                        }
                        txtSearchResultCount.setText("검색 결과 : " + task.getResult().size() + "건");
                        txtSearchResultCount.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("정익", "서버에서 과목 가져오기 오류남", task.getException());
                    }
                }
            });
    }
}