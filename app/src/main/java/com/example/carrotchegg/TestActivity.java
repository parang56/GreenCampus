package com.example.carrotchegg;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    TestAdapter adapter;
    RecyclerView recyclerView;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;

    private ArrayList<Post> dataModels = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        db.collection("subjects").document("C").collection("post")
                .orderBy("heart")
                .startAt(2)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    //하트 2이상 데이터 만들어줌
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
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
                        } else {
                            Log.d("테스트", "Error getting documents: ", task.getException());
                        }
                        //이제 이으러 감
                        A();
                    }
                });
    }

    void A()
    {
        recyclerView = findViewById(R.id.recyclerview);
        adapter = new TestAdapter(TestActivity.this, dataModels);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }
}
