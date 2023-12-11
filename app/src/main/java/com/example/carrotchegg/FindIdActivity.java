package com.example.carrotchegg;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FindIdActivity extends AppCompatActivity {

    View back_Arrow;
    TextView next_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_id);

        // Go back to login screen
        back_Arrow = findViewById(R.id.back_arrow);
        back_Arrow.setOnClickListener(v -> {
            finish();
        });

        // 여기는 코드를 바꿔야함 -> 서버에 이메일 비교해서 있으면 이메일 보내고 토스트 띄우고,
        // 이메일 비교했는데 없으면 없다는 토스트만 띄우기.

        //일단은 성공 토스트를 띄우는 걸로 함.
        next_Btn = findViewById(R.id.find_id_btn);
        next_Btn.setOnClickListener(v -> {
            /*
            if(server has no email)
                Toast.makeText(this, "존재하지 않는 이메일입니다.", Toast.LENGTH_LONG).show();
            else
             */
            Toast.makeText(this, "이메일을 성공적으로 보냈습니다", Toast.LENGTH_LONG).show();
        });
    }
}