package com.example.carrotchegg;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.paging.PagingConfig;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.carrotchegg.databinding.ActivityDetailOfQuestionBinding;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;

public class DetailOfQuestion extends AppCompatActivity {
    ActivityDetailOfQuestionBinding binding;
    private FirebaseFirestore db;
    FirebaseStorage storage;
    private static final String TAG = "20201508";
    informationOfQuest information;
    questionParcelableData questionParcelableData;
    FirestorePagingAdapter<informationOfAns,informationOfAnsViewHolder> adapter;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    LinearLayout image_layout;
    private ImageView heart, scrap;
    int heartNum;
    int accuseCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        storage = FirebaseStorage.getInstance();
        initializeCloudFirestore();

        //글 번호 받아오기
        Intent intent = getIntent();
        questionParcelableData = intent.getParcelableExtra("questionParcelableData");
        binding = ActivityDetailOfQuestionBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getADocument(questionParcelableData.subjectName,questionParcelableData.documentNum);
        binding.detailOfQuestBack.setOnClickListener(v -> {
            finish();
        });
        binding.detailOfQuestMakeAnsBtn.setOnClickListener(v -> {
            questionParcelableData data = new questionParcelableData(questionParcelableData.subjectName,questionParcelableData.documentNum);
            startActivity(new Intent(DetailOfQuestion.this,WriteAns.class).putExtra("questionParcelableData",data));
        });
        binding.detailOfQuestDot.setOnClickListener(v -> {

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.detail_of_quest_main,QuestFragment.newInstance(information.getTitle(),uid,information.getSubject(),information.getPostNum()))
                    .commit();
        });


        // Erase image view when there is no image
        image_layout = findViewById(R.id.image_rectangle);

        //유저 데이터와 비교하여 하트 표시 on off
        heart = findViewById(R.id.heart);
        db.collection("userData").document(uid).collection("heart")
                .whereEqualTo("post", questionParcelableData.documentNum)
                .whereEqualTo("subject", questionParcelableData.subjectName)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty())
                            {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    //하트 칠해줌
                                    heart.setImageResource(R.drawable.heart);
                                    heartOrigin = heartBool = 1;
                                    heartDocumentId=document.getId();
                                }
                            }
                            else
                            {
                                heart.setImageResource(R.drawable.heart_empty);
                                heartOrigin=heartBool=-1;
                            }
                        }
                    }
                });


        //유저 데이터와 비교하여 스크랩 표시 on off
        scrap = findViewById(R.id.scrap);
        db.collection("userData").document(uid).collection("scrap")
                .whereEqualTo("post", questionParcelableData.documentNum)
                .whereEqualTo("subject", questionParcelableData.subjectName)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(!task.getResult().isEmpty())
                            {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    //스크랩 칠해줌
                                    scrap.setImageResource(R.drawable.scrap_fill);
                                    scrapOrigin = scrapBool = 1;
                                    scrapDocumentId = document.getId();
                                }
                            }
                            else
                            {
                                scrap.setImageResource(R.drawable.scrap);
                                scrapOrigin=scrapBool=-1;
                            }
                        }
                    }
                });

        // 리사이클러 뷰 구현부분
        setTitle("FirestorePagingAdapter");

        // The "base query" is a query with no startAt/endAt/limit clauses that the adapter can use
        // to form smaller queries for each page. It should only include where() and orderBy() clauses
        Query baseQuery = FirebaseFirestore.getInstance().collection("subjects")
                .document(questionParcelableData.subjectName).collection("post")
                .document(String.valueOf(questionParcelableData.documentNum)).collection("answers")
                .orderBy("postNum", Query.Direction.ASCENDING);

        // This configuration comes from the Paging 3 Library
        // https://developer.android.com/reference/kotlin/androidx/paging/PagingConfig
        PagingConfig config = new PagingConfig(/* page size */ 4, /* prefetchDistance */ 2,
                /* enablePlaceHolders */ false);

        // The options for the adapter combine the paging configuration with query information
        // and application-specific options for lifecycle, etc.
        FirestorePagingOptions<informationOfAns> options = new FirestorePagingOptions.Builder<informationOfAns>()
                .setLifecycleOwner(this) // an activity or a fragment
                .setQuery(baseQuery, config, informationOfAns.class)
                .build();
        adapter = new FirestorePagingAdapter<informationOfAns, informationOfAnsViewHolder>(options) {


            @NonNull
            @Override
            public informationOfAnsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create the ItemViewHolder
                // ...
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.question_item, parent, false);
                return new informationOfAnsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull informationOfAnsViewHolder holder,
                                            int position,
                                            @NonNull informationOfAns model) {
                // Bind the item to the view holder
                // ...
                holder.bind(model,DetailOfQuestion.this,information.getSubject());
            }
        };
        binding.detailOfQuestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.detailOfQuestRecyclerView.setAdapter(adapter);
    }

    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    private long heartOrigin, heartBool, scrapOrigin,  scrapBool;
    private String heartDocumentId, scrapDocumentId;

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


    private void downloadImageTo(String uri) {
        Log.d(TAG, "이미지 다운중"+uri);

        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference gsReference = storage.getReferenceFromUrl(uri); // from gs://~~~
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        Glide.with(this /* context */)
                .load(gsReference)
                .error(R.drawable.heart)
                .into(binding.detailOfQuestImage);
    }
    public String toTimeStamp(long num){
        Date toTimeStamp = new Date(num);
        SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        return datef.format(toTimeStamp) ;
    }
    private void getADocument(String subjectName, int documentNum) {
        Log.d(TAG,subjectName+" "+documentNum);
        DocumentReference docRef = db.collection("subjects").document(subjectName)
                .collection("post").document(String.valueOf(documentNum));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        information = document.toObject(informationOfQuest.class);
                        assert information != null;
                        boolean heartCheck = false;
                        List<String> heartUserIdList = information.getHeartUserId();
                        if (heartUserIdList!= null && !heartUserIdList.isEmpty()) {
                            for (String str : heartUserIdList) {
                                if (str.equals(uid)) {
                                    heartCheck = true;
                                    break;
                                }
                            }
                        }
                        if(heartCheck == true){
                            binding.heart.setVisibility(View.VISIBLE);
                        }

                        if(information.getImage())
                            downloadImageTo(information.getImageSource());
                        else
                            image_layout.setVisibility(View.GONE);
                        heartNum = information.getHeart();
                        binding.detailOfQuestSubject.setText(information.getSubject());
                        binding.detailOfQuestMaketime.setText(toTimeStamp(information.getTimestamp().toDate().getTime()));
                        binding.detailOfQuestSentence.setText(information.getText());
                        db.collection("subjects").document(questionParcelableData.subjectName)
                                .collection("post").document(String.valueOf(questionParcelableData.documentNum))
                                .update("views",information.getViews()+1); // 조회수 업데이트
                        binding.detailOfQuestNumOfViews.setText("조회 "+String.valueOf(information.getViews()));
                        binding.detailOfQuestTitle.setText(information.getTitle());
                        FindNickName(information.getName());
                        StorageReference storageRef = storage.getReference();
                        storageRef.child("user_image").child(information.getName() + ".png")
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Glide.with(DetailOfQuestion.this).load(uri).into(binding.detailOfQuestImageOfWriter);
                                    }
                                });

                        //document.get("name").toString() = 상대 uid
                        binding.otherProfile.setOnClickListener(v -> {
                            Intent intent;
                            if(uid.equals(document.get("name").toString()))
                            {
                                intent = new Intent(DetailOfQuestion.this, MyProfileActivity.class);
                            }
                            else //다른사람 프로필
                            {
                                intent = new Intent(DetailOfQuestion.this, OtherProfileActivity.class);
                                intent.putExtra("uid", document.get("name").toString());
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
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
    private void FindNickName(String id){
        DocumentReference loadName = db.collection("userData").document(id);
        loadName.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String s = task.getResult().getData().get("nickname").toString();
                    binding.detailOfQuestNameOfWriter.setText(s);
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Query baseQuery = FirebaseFirestore.getInstance().collection("subjects")
                .document(questionParcelableData.subjectName).collection("post")
                .document(String.valueOf(questionParcelableData.documentNum)).collection("answers")
                .orderBy("postNum", Query.Direction.ASCENDING);

        // This configuration comes from the Paging 3 Library
        // https://developer.android.com/reference/kotlin/androidx/paging/PagingConfig
        PagingConfig config = new PagingConfig(/* page size */ 4, /* prefetchDistance */ 2,
                /* enablePlaceHolders */ false);

        // The options for the adapter combine the paging configuration with query information
        // and application-specific options for lifecycle, etc.
        FirestorePagingOptions<informationOfAns> options = new FirestorePagingOptions.Builder<informationOfAns>()
                .setLifecycleOwner(this) // an activity or a fragment
                .setQuery(baseQuery, config, informationOfAns.class)
                .build();
        adapter.updateOptions(options);
        adapter.notifyDataSetChanged();
        getADocument(questionParcelableData.subjectName,questionParcelableData.documentNum);
        binding.detailOfQuestRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.detailOfQuestRecyclerView.setAdapter(adapter);
        Log.d(TAG, "리로딩");
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

        //하트 on off
        heart.setOnClickListener(v -> {
            heartBool = -heartBool;
            if (heartBool==1) heart.setImageResource(R.drawable.heart);
            else heart.setImageResource(R.drawable.heart_empty);
        });

        //스크랩 on off
        scrap.setOnClickListener(v -> {
            scrapBool = -scrapBool;
            if (scrapBool==1) scrap.setImageResource(R.drawable.scrap_fill);
            else scrap.setImageResource(R.drawable.scrap);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //유저 정보 하트 갱신
        if(heartOrigin!=heartBool)
        {
            if(heartBool==1)
            {
                //하트 문서 추가
                Map<String, Object> data = new HashMap<>();
                data.put("post", questionParcelableData.documentNum);
                data.put("subject", questionParcelableData.subjectName);

                db.collection("userData").document(uid)
                        .collection("heart").document()
                        .set(data);
            }
            else
            {
                //하트 문서 삭제
                db.collection("userData").document(uid)
                        .collection("heart").document(heartDocumentId)
                        .delete();
            }

            //해당 과목 하트 수 업데이트
            db.collection("subjects").document(questionParcelableData.subjectName)
                    .collection("post").document(String.valueOf(questionParcelableData.documentNum))
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        db.collection("subjects").document(String.valueOf(questionParcelableData.subjectName))
                                .collection("post").document(String.valueOf(questionParcelableData.documentNum))
                                .update("heart", Long.parseLong(task.getResult().getData().get("heart").toString())+heartBool);
                    }
                }
            });
        }


        //유저 정보 스크랩 갱신
        if(scrapOrigin!=scrapBool)
        {
            if(scrapBool==1)
            {
                //스크랩 문서 추가
                Map<String, Object> data = new HashMap<>();
                data.put("post", questionParcelableData.documentNum);
                data.put("subject", questionParcelableData.subjectName);
                data.put("timestamp", new Date());

                db.collection("userData").document(uid)
                        .collection("scrap").document()
                        .set(data);
            }
            else
            {
                //스크랩 문서 삭제
                db.collection("userData").document(uid)
                        .collection("scrap").document(scrapDocumentId)
                        .delete();
            }
        }
    }

}

