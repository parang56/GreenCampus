package com.example.carrotchegg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ConditionsActivity extends Activity{
    View back_Arrow;
    TextView next_Btn;

    //If press okay btn, send feedback to Terms_And_Conditions
    private void onTermsAccepted() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("termsAccepted", true);
        setResult(2, resultIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conditions);

        back_Arrow = findViewById(R.id.back_arrow);
        back_Arrow.setOnClickListener(v -> finish());

        next_Btn = findViewById(R.id.next_button);
        next_Btn.setOnClickListener(v-> {
            onTermsAccepted();
        });
    }
}