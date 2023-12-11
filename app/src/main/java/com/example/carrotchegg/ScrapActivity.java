package com.example.carrotchegg;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

public class ScrapActivity extends AppCompatActivity {
    
    private FirestoreRecyclerAdapter adapter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private ImageButton btnBarMain, btnBarScrap, btnBarNotification, btnBarMyProfile;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.scrap);

        btnBarMain = (ImageButton) findViewById(R.id.barMain);
        btnBarScrap = (ImageButton) findViewById(R.id.barScape);
        btnBarNotification = (ImageButton) findViewById(R.id.barNotification);
        btnBarMyProfile = (ImageButton) findViewById(R.id.barMyProfile);
        

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

        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("userData").document(uid).collection("scrap")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<UserData> options = new FirestoreRecyclerOptions.Builder<UserData>()
                .setQuery(query, UserData.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<UserData, ScrapHolder>(options) {

            @Override
            public void onBindViewHolder(ScrapHolder holder, int position, UserData model) {
                holder.bind(model, ScrapActivity.this);
            }

            @Override
            public ScrapHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.post_form, group, false);
                return new ScrapHolder(view);
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