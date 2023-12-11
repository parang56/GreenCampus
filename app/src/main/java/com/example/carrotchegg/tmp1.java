package com.example.carrotchegg;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.carrotchegg.databinding.ActivityTmp1Binding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class tmp1 extends AppCompatActivity {
    ActivityTmp1Binding binding;
    private FirebaseFirestore db;
    private static final String TAG = "20201508";
    private boolean isTrue = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        initializeCloudFirestore();


        binding = ActivityTmp1Binding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        binding.mainBtn.setOnClickListener(v -> {
            if(isTrue){
                questionParcelableData data = new questionParcelableData("test1",1);
                startActivity(new Intent(tmp1.this,DetailOfQuestion.class).putExtra("questionParcelableData",data));
            }
        });

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        Log.d("20201508","결과가 잘 전송되었나");
                        if (o.getResultCode() == RESULT_OK){
//                            informationOfAns infA = (informationOfAns)bundle2.getParcelable("makequest");
//
                        }
                        //binding.textForResult.setText(o.getData().getStringExtra("result"));
                    }
                });
        binding.mainBtn2.setOnClickListener(v -> {
            launcher.launch(new Intent(tmp1.this, MakeQuestion.class));
        });

        Intent intent = getIntent();
        if(intent != null) {//푸시알림을 선택해서 실행한것이 아닌경우 예외처리
            String notificationData = intent.getStringExtra("test");
            if(notificationData != null)
                Log.d("FCM_TEST", notificationData);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed");
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        System.out.println(token);
                        Toast.makeText(tmp1.this, "Your device registration token is" + token
                                , Toast.LENGTH_SHORT).show();
                        Log.d("20201508",token);
                    }
                });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
    private void initializeCloudFirestore() {
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

}