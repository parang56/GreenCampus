package com.example.carrotchegg;

import static androidx.core.app.NotificationCompat.PRIORITY_HIGH;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.carrotchegg.databinding.ActivityWriteAnsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WriteAns extends AppCompatActivity {
    ActivityWriteAnsBinding binding;
    private static final String TAG = "20201508";
    String lastNum ="0";
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    Uri uri;
    String otherUser;
    UserModel userdata;
    String name;

    EditText text;
    private FirebaseFirestore db;
    informationOfQuest information;
    String imageRef;
    questionParcelableData questionParcelableData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        binding = ActivityWriteAnsBinding.inflate(getLayoutInflater()); // 초기설정
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ImagesRef = storageRef.child(getPath("jpg"));
        initializeCloudFirestore();
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        Intent intent = getIntent(); // 인텐트 받아오기(어떤 글의 답변인지 알아야함)
        questionParcelableData = intent.getParcelableExtra("questionParcelableData");

        text = findViewById(R.id.write_ans_textOfMakingAnswer);
        setupTouchListener(text);


        assert questionParcelableData != null;
        getADocument(questionParcelableData.subjectName,questionParcelableData.documentNum);
        ImageView image = null;
        binding.writeAnsFinishBtn.setOnClickListener(v -> { // 저장버튼 눌렀을때

            // If text is empty don't proceed.
            if(binding.writeAnsTextOfMakingAnswer.getText().toString().isEmpty()){
                Toast.makeText(this, "내용이 빈칸일 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }

            if(binding.writeAnsInsertImage.getDrawable() == null){ // 올릴 이미지가 없다면
                addData();
            }
            else {
                Bitmap bitmap = ((BitmapDrawable) binding.writeAnsInsertImage.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //0-100
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = ImagesRef.putBytes(data);// 이미지 서버로 업로드 시도
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("20201508", "이미지뷰의 이미지 업로드 실패");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.d("20201508", "이미지뷰의 이미지 업로드 성공");
                        imageRef = Objects.requireNonNull(taskSnapshot.getMetadata()).getReference().toString(); // 이미지 주소를 받아오기

                        Log.d("20201508", imageRef);
                        Log.d("20201508", "저장시도");
                        // 데이터베이스에 저장하는 코드가 필요함.
                        addData();
                    }
                });
            }
        });

        ActivityResultLauncher<Intent> launcher =registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        int code = o.getResultCode();
                        if(o.getResultCode() == RESULT_OK) { // 이미지를 앨범에서 잘 받아왔을 경우에
                            Intent intentData = o.getData();
                            assert intentData != null;
                            uri = intentData.getData();
                            //imageView.setImageURI(uri);
                            binding.writeAnsInsertImage.setImageURI(uri);
                        }
                    }
                }
        );

        binding.writeAnsBack.setOnClickListener(v -> {
            finish();
        });

        binding.writeAnsInsertImage.setOnClickListener(view -> {
            Intent intent2 = new Intent();
            intent2.setType("image/*");
            intent2.setAction(Intent.ACTION_GET_CONTENT);
            launcher.launch(intent2);
        });
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

    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    private void addData(){
        //답변저장할곳 : subjects(컬랙션,고정) -> test1(document,과목명(c등)) -> post(고정) -> 0,1,2,... -> answer

        CollectionReference infOfQuestions = db.collection("subjects").document(questionParcelableData.subjectName)
                .collection("post").document(String.valueOf(questionParcelableData.documentNum)).collection("answers");
        infOfQuestions.orderBy("postNum", Query.Direction.DESCENDING).limit(1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                informationOfAns tmp = document.toObject(informationOfAns.class);
                                lastNum = String.valueOf(tmp.getAnsNum() + 1);
                                Log.d(TAG, document.getId()+"!"); // 문서의 ID를 가져옵니다
                            }

                        } else {
                            // 쿼리 실행 중에 오류가 발생한 경우 처리할 작업 수행
                        }
                        informationOfAns information2;
                        if (binding.writeAnsInsertImage.getDrawable() != null)
                            information2 = new informationOfAns(binding.writeAnsTextOfMakingAnswer.getText().toString()
                                    ,true,information.getPostNum(),0,imageRef,0,uid,questionParcelableData.subjectName,Integer.parseInt(lastNum), null);
                        else
                            information2 = new informationOfAns(binding.writeAnsTextOfMakingAnswer.getText().toString()
                                    ,false,information.getPostNum(),0,"null",0,uid,questionParcelableData.subjectName,Integer.parseInt(lastNum), null);
                        infOfQuestions.document(lastNum).set(information2);

                        db.collection("subjects").document(questionParcelableData.subjectName)
                                .collection("post").document(String.valueOf(questionParcelableData.documentNum))
                                .update("answer",Integer.parseInt(lastNum)+1); // 답변수 업데이트




                        //유저한테 답변 문서 추가
                        Map<String, Object> data = new HashMap<>();
                        data.put("post", questionParcelableData.documentNum);
                        data.put("subject", questionParcelableData.subjectName);
                        data.put("timestamp", new Date());

                        db.collection("userData").document(uid)
                                .collection("answer").document()
                                .set(data);

                        //받는 유저 알림데이터 추가

                        if(!(uid.equals(information.getName()))) {
                            Map<String, Object> data2 = new HashMap<>();
                            data2.put("post", questionParcelableData.documentNum);
                            data2.put("subject", questionParcelableData.subjectName);
                            data2.put("clicked", false);
                            data2.put("timestamp", new Date());
                            db.collection("subjects").document(questionParcelableData.subjectName)
                                    .collection("post").document(String.valueOf(questionParcelableData.documentNum))
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            String otherUID = task.getResult().getData().get("name").toString();
                                            db.collection("userData").document(otherUID)
                                                    .collection("notification").document()
                                                    .set(data2);
                                        }
                                    });
                        }

                        // 유저 답변수 업데이트
                        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                db.collection("userData").document(uid).update("answer", Long.parseLong(task.getResult().getData().get("answer").toString()) + 1);
                            }
                        });

                        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    userdata = document.toObject(UserModel.class);
                                    name = userdata.getNickname();
                                }
                            }
                        });
                        db.collection("userData").document(information.getName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        userdata = document.toObject(UserModel.class);
                                        otherUser = userdata.getFcmToken();

                                        if(task.getResult().getData().get("push_alarm").toString().equals("true")) {
                                            sendNotification("'" +name + "'님께서 '" + information.getTitle() + "'글에 답변을 남기셨습니다.");
                                            Log.d("20201508", "uid : " + name + "\n token : " + information.getName());
                                        }
                                    }
                                } else {
                                    // 쿼리 실행 중에 오류가 발생한 경우 처리할 작업 수행
                                }
                            }
                        });

                        // Change user exp
                        db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Retrieve the current exp value
                                        long currentExp = document.getLong("exp");
                                        // Change the exp and level
                                        long newExp = currentExp + 25;
                                        long newLevel = (newExp / 100) + 1;

                                        // Update the exp and level field in the Firestore document
                                        db.collection("userData").document(uid).update("exp", newExp);
                                        db.collection("userData").document(uid).update("level", newLevel);
                                    }
                                }
                            }
                        });


                        finish();
                    }
                });
    }
    void sendNotification(String message){
        //current username, message, currentuserid, otherusertoken
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                UserModel currentUser = task.getResult().toObject(UserModel.class);
                try{
                    JSONObject jsonObject = new JSONObject();
                    JSONObject notificationObj = new JSONObject();
                    notificationObj.put("title","답변 알림");
                    notificationObj.put("body",message);
                    JSONObject dataObj = new JSONObject();
                    dataObj.put("userId",uid);
                    jsonObject.put("notification",notificationObj);
                    jsonObject.put("data",dataObj);
                    jsonObject.put("to",otherUser);
                    callApi(jsonObject);
                }catch (Exception e){

                }
            }
        });
    }
    void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json");

        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","bearer AAAArFN8unM:APA91bFph-3cfMP1aNcSJ6Ou1iQGFE26-Gc4IhWh1axapzdNPS267ZgELbL67y7u716NrB1wthALGnTH21XRPQ881gvZ7d8Mm_fnWJObWIKTIDj-odXN6X8-1x3L4ikONHeyuiyCoVRW")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
    private void getADocument(String subjectName, int documentNum) {
        DocumentReference docRef = db.collection("subjects").document(subjectName)
                .collection("post").document(String.valueOf(documentNum));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        information = document.toObject(informationOfQuest.class);
                        binding.writeAnsContentOfQuestion.setText(information.getText());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    // Hide keyboard
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final EditText text) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    text.clearFocus();
                }
            });
        }
    }
}