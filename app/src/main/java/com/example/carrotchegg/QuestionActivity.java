package com.example.carrotchegg;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class QuestionActivity extends AppCompatActivity {

    private FirestoreRecyclerAdapter adapter;
    private String uid;
    private TextView anyText;

    private ImageView btnBack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = getIntent().getStringExtra("uid");

        setContentView(R.layout.any_list);

        anyText = (TextView) findViewById(R.id.any_text);
        anyText.setText("질문한 목록");

        btnBack = (ImageButton) findViewById(R.id.backButton);
        btnBack.setOnClickListener(v ->{
            finish();
        });


        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("userData").document(uid).collection("question")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<UserData> options = new FirestoreRecyclerOptions.Builder<UserData>()
                .setQuery(query, UserData.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<UserData, QuestionHolder>(options) {

            @Override
            public void onBindViewHolder(QuestionHolder holder, int position, UserData model) {
                holder.bind(model, QuestionActivity.this);
            }

            @Override
            public QuestionHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.post_form, group, false);
                return new QuestionHolder(view);
            }
        };


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        adapter.notifyDataSetChanged(); //리사이클러뷰에 데이터 변경을 알림 (안하면 돌아왔을때 터짐)
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    //엑티비티 종료 엑티비티 제거
    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}