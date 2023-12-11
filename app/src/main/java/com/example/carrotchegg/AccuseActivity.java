package com.example.carrotchegg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.carrotchegg.databinding.ActivityAccuseBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AccuseActivity extends AppCompatActivity {
    ActivityAccuseBinding binding;
    private FirebaseFirestore db;
    List<String> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        binding = ActivityAccuseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        accuseParcelableData data = intent.getParcelableExtra("accuse");
        Log.d("20201508",data.subject+"-!-"+data.Number);
        binding.accuseTitle.setText("글제목 : " + data.title);
        binding.accuseSubject.setText("과목명 : " + data.subject);
        db.collection("userData").document(data.writername)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        String otherNick = task.getResult().getData().get("nickname").toString();
                        binding.accuseWriterName.setText("작성자 : " + otherNick);
                    }
                });
        binding.accuseFinishBtn.setOnClickListener(v -> {
            if (binding.accuseCheck1.isChecked() == true)
                list.add("스팸홍보/도배글입니다.");
            if (binding.accuseCheck2.isChecked() == true)
                list.add("불법정보를 포함하고 있습니다.");
            if (binding.accuseCheck3.isChecked() == true)
                list.add("욕설/생명경시/혐오/차별적 표현입니다.");
            if (binding.accuseCheck4.isChecked() == true)
                list.add("개인정보 노출 게시물입니다.");
            if (binding.accuseCheck5.isChecked() == true)
                list.add("불쾌한 표현이 있습니다.");
            if (binding.accuseCheck6.isChecked() == true)
                list.add("명예훼손 또는 저작권이 침해되었습니다.");
            if(list.isEmpty()==true)
                Toast.makeText(this,"하나이상의 항목을 선택해 주세요.",Toast.LENGTH_SHORT).show();
            else {
                DocumentReference docRef = db.collection("accuse").document(data.title);
                informationOfAccuse information = new informationOfAccuse(data.writername,data.title,list,data.subject,data.Number);
                docRef.set(information);
                Toast.makeText(this, "신고가 접수되었습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        binding.accuseBack.setOnClickListener(v -> {
            finish();
        });
    }
}