package com.example.carrotchegg;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatActivity;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class SearchActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private List<String> subjectList;

    private ArrayList<Post> dataModelsOri = new ArrayList();
    private ArrayList<Post> dataModels = new ArrayList();
    private String searchText;

    private TestAdapter adapter;
    private RecyclerView recyclerView;
    private TextView anyText;
    private int cnt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.any_list);


        anyText = findViewById(R.id.any_text);
        anyText.setText("검색 결과");

        //back버튼
        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        searchText = getIntent().getStringExtra("text").toLowerCase();

        //유저의 관심 과목들 목록부터 가져오기
        Serializable serial = getIntent().getSerializableExtra("subjectList");
        subjectList = (List<String>) serial;


        for(int i=0; i<subjectList.size(); i++)
        {
            db.collection("subjects").document(subjectList.get(i)).collection("post")
                    .orderBy("heart")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                        dataModelsOri.add(new Post(
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
                            cnt++;
                        }
                    });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cnt == subjectList.size()) {
                    Collections.sort(dataModelsOri); //하트 내림차순 정렬
                    for (int i = 0; i < dataModelsOri.size(); i++) {
                        //검색어 제목이나 내용과 일치하는 것들만 골라주기
                        if (dataModelsOri.get(i).getTitle().toString().toLowerCase().contains(searchText)) {
                            dataModels.add(dataModelsOri.get(i));
                        }
                        else if (dataModelsOri.get(i).getText().toString().toLowerCase().contains(searchText)) {
                            dataModels.add(dataModelsOri.get(i));
                        }
                    }
                    A();
                }
            };
        }, 1000); //검색 처리 시간
    }


    void A()
    {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new TestAdapter(SearchActivity.this, dataModels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}