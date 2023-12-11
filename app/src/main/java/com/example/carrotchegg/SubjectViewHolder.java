package com.example.carrotchegg;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SubjectViewHolder extends RecyclerView.ViewHolder {
    TextView mTextView;
    TextView mValueView;

    SubjectViewHolder(@NonNull View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(android.R.id.text1);
        mValueView = itemView.findViewById(android.R.id.text2);
    }

    void bind(@NonNull Subject subject) {
        mTextView.setText(subject.getName());
        mValueView.setText(subject.getText());
    }
}

