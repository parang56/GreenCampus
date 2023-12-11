package com.example.carrotchegg;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.carrotchegg.databinding.ActivityMakeQuestionBinding;

import android.widget.ListAdapter;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MakeQuestion extends AppCompatActivity {
    ActivityMakeQuestionBinding binding;
    private static final String TAG = "20201508";
    AlertDialog categoryDialog;
    Uri uri;
    private FirebaseFirestore db;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    String lastNum ="0";
    String imageRef;
    ImageView imageView = null;
    private EditText title, text;
    private List<String> subjectList;
    private String[] subjectArr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference ImagesRef = storageRef.child(getPath("jpg"));
        initializeCloudFirestore();
        binding = ActivityMakeQuestionBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.makeQuestionBack.setOnClickListener(v -> {
            finish();
        });
        //informationOfQuest quest = new informationOfQuest();
        ImageView image=null;


        //유저의 관심 과목들 목록 가져오기
        Serializable serial = getIntent().getSerializableExtra("subjectList");
        subjectList = (List<String>) serial;

        // List -> String[]
        subjectArr = subjectList.toArray(new String[subjectList.size()]);



        title = findViewById(R.id.make_question_title);
        text = findViewById(R.id.make_question_text);
        // Minimize keyboard when click somewhere that is not keyboard.
        setupTouchListener(title,text);

        binding.makeQuestionFinishBtn.setOnClickListener(v -> { // 저장버튼 눌렀을 때

            // If title, text, or category is empty don't proceed.
            if(binding.makeQuestionTitle.getText().toString().isEmpty()){
                Toast.makeText(this, "제목이 빈칸일 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            if(binding.makeQuestionText.getText().toString().isEmpty()){
                Toast.makeText(this, "내용이 빈칸일 수 없습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            String category = binding.makeQuestionCategoryChoice.getText().toString();
            Log.d("test", category);
            if(binding.makeQuestionCategoryChoice.getText().toString().equals("카테고리 선택")){
                Toast.makeText(this, "카테고리를 고르셔야 합니다.", Toast.LENGTH_LONG).show();
                return;
            }

            if(binding.makeQuestionInsertImage.getDrawable() == null){ // 올릴 이미지가 없다면
                Log.d("20201508","그림없음 확인");
                addData();
                finish();
            }
            else {
                Bitmap bitmap = ((BitmapDrawable) binding.makeQuestionInsertImage.getDrawable()).getBitmap();
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
                        imageRef = taskSnapshot.getMetadata().getReference().toString(); // 이미지 주소를 받아오기

                        Log.d("20201508", imageRef);
                        Log.d("20201508", "저장시도");
                        // 데이터베이스에 저장하는 코드가 필요함.
                        addData();
                        finish();
                    }
                });
            }
        });

        //사진 가져오기
        ActivityResultLauncher<Intent> launcher =registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        int code = o.getResultCode();
                        if(o.getResultCode() == RESULT_OK) { // 이미지를 앨범에서 잘 받아왔을 경우에
                            Intent intentData = o.getData();
                            uri = intentData.getData();
                            //imageView.setImageURI(uri);
                            binding.makeQuestionInsertImage.setImageURI(uri);
                        }
                    }
                }
        );
        binding.makeQuestionCategoryChoice.setOnClickListener(view -> {
            categoryDialog = new AlertDialog.Builder(this)
                    .setSingleChoiceItems(subjectArr,0,dialogListener)
                    .create();
            categoryDialog.show();
        });
        binding.makeQuestionInsertImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            launcher.launch(intent);
        });
    }

    DialogInterface.OnClickListener dialogListener =new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if(dialogInterface == categoryDialog){
                binding.makeQuestionCategoryChoice.setText(subjectArr[i]);
                categoryDialog.dismiss();
            }
        }
    };

    private String getPath(String extension) {
        String uid = getUidOfCurrentUser();

        String dir = (uid != null) ? uid : "public";

        String fileName = (uid != null) ? (uid + "_" + System.currentTimeMillis() + "." + extension)
                : ("anonymous" + "_" + System.currentTimeMillis() + "." + extension);

        return dir + "/" + fileName;
    }
    private String getUidOfCurrentUser() {
        return hasSignedIn() ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }
    private boolean hasSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? true : false;
    }

    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }


    //db에 데이터를 추가하기
    private void addData() {
       //저장할곳 : subjects(컬랙션,고정) -> test1(document,과목명(c등)) -> post -> 0,1,2,...

        String category = binding.makeQuestionCategoryChoice.getText().toString();
        CollectionReference infOfQuestions = db.collection("subjects").document(category).collection("post");
        infOfQuestions.orderBy("postNum", Query.Direction.DESCENDING).limit(1);
        infOfQuestions.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d("20201508","db접근 성공");
                        informationOfQuest tmp = document.toObject(informationOfQuest.class);
                        lastNum = String.valueOf(tmp.getPostNum()+1);
                        Log.d(TAG, document.getId()); // 문서의 ID를 가져옵니다
                    }

                } else {
                    // 쿼리 실행 중에 오류가 발생한 경우 처리할 작업 수행
                }

                informationOfQuest information;
                if(binding.makeQuestionInsertImage.getDrawable() != null)
                    information = new informationOfQuest(binding.makeQuestionTitle.getText().toString(),imageRef,uid,binding.makeQuestionText.getText().toString(),
                            0,0,binding.makeQuestionCategoryChoice.getText().toString(),0,true,Integer.parseInt(lastNum),null, true, true);
                else
                    information = new informationOfQuest(binding.makeQuestionTitle.getText().toString(),"null",uid,binding.makeQuestionText.getText().toString(),
                            0,0,binding.makeQuestionCategoryChoice.getText().toString(),0,false,Integer.parseInt(lastNum),null, true, true);

                infOfQuestions.document(lastNum).set(information);


                //유저한테 쓴 글 추가
                Map<String, Object> data = new HashMap<>();
                data.put("post", Integer.parseInt(lastNum));
                data.put("subject", category);
                data.put("timestamp", new Date());

                db.collection("userData").document(uid)
                        .collection("question").document()
                        .set(data);

                // 유저 질문 수 업데이트
                db.collection("userData").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        db.collection("userData").document(uid).update("question",Long.parseLong(task.getResult().getData().get("question").toString())+1);
                    }
                });

            }
        });
    }

    // Hide keyboard
    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener(final EditText title, final EditText text) {
        final View rootView = findViewById(R.id.root_view); // Assuming you have a root layout with this ID
        if (rootView != null) {
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Hide the soft keyboard when tapping outside the EditText fields
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    title.clearFocus();
                    text.clearFocus();
                }
            });
        }
    }

}