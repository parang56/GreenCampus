package com.example.carrotchegg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.carrotchegg.databinding.ActivityDetailOfAnswerBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class DetailOfAnswer extends AppCompatActivity {
    private static final String TAG = "20201508";
    private FirebaseFirestore db;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    FirebaseStorage storage;
    informationOfAns information;
    ActivityDetailOfAnswerBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityDetailOfAnswerBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        storage = FirebaseStorage.getInstance();
        initializeCloudFirestore();
        Intent intent = getIntent();
        ansParcelableData Data = intent.getParcelableExtra("questionParcelableData");
        getADocument(Data.subject,Data.postNumber,Data.ansNum);
        binding.detailOfAnsBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void getADocument(String subjectName, int documentNum,int ansNum) {
        Log.d(TAG,subjectName+" !"+documentNum+" "+ansNum);
        DocumentReference docRef = db.collection("subjects").document(subjectName)
                .collection("post").document(String.valueOf(documentNum))
                .collection("answers").document(String.valueOf(ansNum));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        information = document.toObject(informationOfAns.class);
                        assert information != null;

                        if(information.getImage())
                            downloadImageTo(information.getImageSource());
                        else {
                            binding.detailOfAnsImage.setVisibility(View.GONE);
                            binding.lineImage.setVisibility(View.GONE);
                        }

                        FindNickName(information.getUserName());
                        binding.detailOfAnsContent.setText(information.getContent());

                        storage = FirebaseStorage.getInstance();
                        StorageReference storageRef = storage.getReference();
                        storageRef.child("user_image").child(information.getUserName() + ".png")
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(DetailOfAnswer.this).load(uri).into(binding.detailOfAnsProfileImage);
                                    }
                                });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }
    private void downloadImageTo(String uri) {
        Log.d(TAG, "이미지 다운중"+uri);

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference gsReference = storage.getReferenceFromUrl(uri); // from gs://~~~
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        Glide.with(this /* context */)
                .load(gsReference)
                .error(R.drawable.heart)
                .into(binding.detailOfAnsImage);
    }
    private String getPath(String extension) {
        String uid = getUidOfCurrentUser();

        String dir = (uid != null) ? uid : "public";

        String fileName = (uid != null) ? (uid + "_" + System.currentTimeMillis() + "." + extension)
                : ("anonymous" + "_" + System.currentTimeMillis() + "." + extension);

        return dir + "/" + fileName;
    }

    private String getUidOfCurrentUser() {
        return hasSignedIn() ? Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() : null;
    }

    private boolean hasSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }
    private void FindNickName(String id){
        DocumentReference loadName = db.collection("userData").document(id);
        loadName.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String s = task.getResult().getData().get("nickname").toString();
                    binding.detailOfAnsName.setText(s);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
}