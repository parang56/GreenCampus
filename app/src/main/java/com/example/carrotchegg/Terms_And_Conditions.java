package com.example.carrotchegg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;

public class Terms_And_Conditions extends Activity{

    private static final int TERMS_ACTIVITY_REQUEST_CODE_Terms = 1;
    private static final int TERMS_ACTIVITY_REQUEST_CODE_Conditions = 2;

    View terms_arrow, conditions_arrow, back_arrow;
    TextView next_btn, error_msg;


    CheckBox checkAll, checkTerms, checkConditions, checkAds;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TERMS_ACTIVITY_REQUEST_CODE_Terms) {
            if (resultCode == Activity.RESULT_OK) {
                // Handle the result from TermsActivity
                if (data != null && data.getBooleanExtra("termsAccepted", true)) {
                    // Terms accepted, you can update UI accordingly
                    checkTerms.setChecked(true);

                    // If all checkboxes are separately checked without checking checkAll checkbox,
                    // check the checkAll checkbox.
                    if(checkTerms.isChecked() && checkConditions.isChecked() && checkAds.isChecked())
                        checkAll.setChecked(true);
                }
            }
        }

        else if (requestCode == TERMS_ACTIVITY_REQUEST_CODE_Conditions) {
            if (resultCode == 2) { // Check for the custom result code
                // Handle the result from TermsActivity
                if (data != null && data.getBooleanExtra("termsAccepted", true)) {
                    // Terms accepted, you can update UI accordingly
                    checkConditions.setChecked(true);

                    // If all checkboxes are separately checked without checking checkAll checkbox,
                    // check the checkAll checkbox.
                    if(checkTerms.isChecked() && checkConditions.isChecked() && checkAds.isChecked())
                        checkAll.setChecked(true);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms_and_conditions);

        // Init error_msg
        error_msg = findViewById(R.id.error_msg);

        // Open terms.xml
        terms_arrow = findViewById(R.id.terms_details_arrow);
        terms_arrow.setOnClickListener(v -> {
            // Start TermsActivity and listen for the result
            Intent intent = new Intent(getApplicationContext(), TermsActivity.class);
            startActivityForResult(intent, TERMS_ACTIVITY_REQUEST_CODE_Terms);
        });

        // Open conditions.xml
        conditions_arrow = findViewById(R.id.conditions_details_arrow);
        conditions_arrow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ConditionsActivity.class);
            startActivityForResult(intent, TERMS_ACTIVITY_REQUEST_CODE_Conditions);
        });

        // Button to continue to registration
        next_btn = findViewById(R.id.next_button);
        next_btn.setOnClickListener(v-> {
            // Continue only when both terms and conditions are checked
            if(checkTerms.isChecked() && checkConditions.isChecked()) {
                Intent intent = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(intent);
            }
            else {
                error_msg.setVisibility(View.VISIBLE);
            }
        });

        // Go back to main menu
        back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginScreenActivity.class);
            startActivity(intent);
            finish();
        });

        // checkAll checkbox
        checkAll = findViewById(R.id.check_all);
        checkAds = findViewById(R.id.check_advertise);
        checkTerms = findViewById(R.id.check_terms);
        checkConditions = findViewById(R.id.check_conditions);
        checkAll.setOnClickListener(v -> {
            // If checkAll is unchecked uncheck all checkboxes
            if(!checkAll.isChecked()) {
                checkAll.setChecked(false);
                checkAds.setChecked(false);
                checkConditions.setChecked(false);
                checkTerms.setChecked(false);
            }
            // else if checkALl is checked check all checkboxes
            else{
                checkAll.setChecked(true);
                checkAds.setChecked(true);
                checkConditions.setChecked(true);
                checkTerms.setChecked(true);
            }
        });

        // 이용약관 동의 | Terms
        checkTerms.setOnClickListener(v -> {
            // If checkTerns is checked, and error msg is current visible, remove the error message.
            if(checkConditions.isChecked() && checkTerms.isChecked())
                if(error_msg.getVisibility() == View.VISIBLE)
                    error_msg.setVisibility(View.INVISIBLE);

            // If all checkboxes are separately checked without checking checkAll checkbox,
            // check the checkAll checkbox.
            if(checkTerms.isChecked() && checkConditions.isChecked() && checkAds.isChecked())
                checkAll.setChecked(true);

            // If checkCondition is unchecked, uncheck checkAll
            if(!checkTerms.isChecked())
                checkAll.setChecked(false);
        });

        // 개인정보 수집 및 이용 동의 | Conditions
        checkConditions.setOnClickListener(v -> {
            // If checkConditions is checked, and error msg is current visible, remove the error message.
            if(checkConditions.isChecked() && checkTerms.isChecked())
                if(error_msg.getVisibility() == View.VISIBLE)
                    error_msg.setVisibility(View.INVISIBLE);

            // If all checkboxes are separately checked without checking checkAll checkbox,
            // check the checkAll checkbox.
            if(checkTerms.isChecked() && checkConditions.isChecked() && checkAds.isChecked())
                checkAll.setChecked(true);

            // If checkCondition is unchecked, uncheck checkAll
            if(!checkConditions.isChecked())
                checkAll.setChecked(false);
        });

        checkAds.setOnClickListener(v->{
            // If all checkboxes are separately checked without checking checkAll checkbox,
            // check the checkAll checkbox.
            if(checkTerms.isChecked() && checkConditions.isChecked() && checkAds.isChecked())
                checkAll.setChecked(true);

            // If checkCondition is unchecked, uncheck checkAll
            if(!checkAds.isChecked())
                checkAll.setChecked(false);
        });
    }
}
