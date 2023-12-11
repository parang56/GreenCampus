package com.example.carrotchegg;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class PostActivity extends AppCompatActivity {

    private TextView anyText;

    private FirestoreRecyclerAdapter adapter;

    private ImageButton btnbookmark;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    private int bookmark, bookmarkOrigin;
    private ImageButton btnBarMain, btnBarScrap, btnBarNotification, btnBarMyProfile, btnBackButton;
    private String subject;
    private TextView btnChange;
    private boolean changed=false;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //과목-포스트일 때
        setContentView(R.layout.subject);

        subject = getIntent().getStringExtra("subjectName");

        anyText = (TextView) findViewById(R.id.subjectName);
        btnBarMain = (ImageButton) findViewById(R.id.barMain);
        btnBarScrap = (ImageButton) findViewById(R.id.barScape);
        btnBarNotification = (ImageButton) findViewById(R.id.barNotification);
        btnBarMyProfile = (ImageButton) findViewById(R.id.barMyProfile);
        btnBackButton = (ImageButton) findViewById(R.id.backButton);
        btnbookmark = (ImageButton) findViewById(R.id.bookmark);
        btnChange = (TextView) findViewById(R.id.change);


        anyText.setText(subject);

        //유저 데이터와 비교하여 북마크 표시 on off
        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    List<String> subjectList = (List<String>) task.getResult().get("subject");

                    if(subjectList.contains(subject)) {
                        btnbookmark.setImageResource(R.drawable.bookmark_on);
                        bookmarkOrigin=bookmark=1;
                    }
                    else
                    {
                        btnbookmark.setImageResource(R.drawable.bookmark_off);
                        bookmarkOrigin=bookmark=-1;
                    }
                }
            }
        });

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


        setTitle("Using FirestoreRecyclerAdapter");

        Query query = FirebaseFirestore.getInstance()
                .collection("subjects").document(subject)
                .collection("post")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();


        adapter = new FirestoreRecyclerAdapter<Post, PostHolder>(options) {

            @Override
            public void onBindViewHolder(PostHolder holder, int position, Post model) {
                holder.bind(model, PostActivity.this, subject);
            }

            @Override
            public PostHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.post_form, group, false);
                return new PostHolder(view);
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
    protected void onResume() {
        super.onResume();

        //즐겨찾기 on off
        btnbookmark.setOnClickListener(v -> {
            bookmark*=-1;
            if(bookmark==-1) btnbookmark.setImageResource(R.drawable.bookmark_off);
            else btnbookmark.setImageResource(R.drawable.bookmark_on);
        });


        //일반글 <-> 인기글
        btnChange.setOnClickListener(v-> {
            if(!changed) {
                //인기글의 조건 (startAt, endAt 안됨)
                Query query = FirebaseFirestore.getInstance()
                        .collection("subjects").document(subject)
                        .collection("post")
                        .orderBy("heart", Query.Direction.DESCENDING);

                FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();

                adapter.updateOptions(options);
                adapter.notifyDataSetChanged();
                
                btnChange.setText("인기순"); //임시
            }
            else
            {
                //다시 최신글로 돌아감
                Query query = FirebaseFirestore.getInstance()
                        .collection("subjects").document(subject)
                        .collection("post")
                        .orderBy("timestamp", Query.Direction.DESCENDING);

                FirestoreRecyclerOptions<Post> options = new FirestoreRecyclerOptions.Builder<Post>()
                        .setQuery(query, Post.class)
                        .build();

                adapter.updateOptions(options);
                adapter.notifyDataSetChanged();

                btnChange.setText("최신순");
            }
            changed = !changed;
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //해당 과목에 북마크 한 사람들 수 업데이트
        db.collection("subjects").document(subject).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if(bookmarkOrigin!=bookmark)
                        db.collection("subjects").document(subject).update("users", Integer.parseInt(task.getResult().getData().get("users").toString())+bookmark);
                }
            }
        });
        //해당 과목을 해당 유저 정보에서 추가 혹은 제거
        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> subjectList = (List<String>) task.getResult().getData().get("subject");

                    if(bookmarkOrigin!=bookmark)
                    {
                        if(bookmark==-1) subjectList.remove(subject);
                        else subjectList.add(subject);
                    }
                    db.collection("userData").document(uid).update("subject", subjectList);
                }
            }
        });
    }
}