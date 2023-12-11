package com.example.carrotchegg;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PostHolder extends RecyclerView.ViewHolder {

    RelativeLayout postLayout;
    TextView titleView, textView, heartTextView, answerTextView;
    ImageView postImageView;

    PostHolder(@NonNull View itemView) {
        super(itemView);

        postLayout = itemView.findViewById(R.id.post);
        titleView = itemView.findViewById(R.id.title);
        textView = itemView.findViewById(R.id.text);
        heartTextView = itemView.findViewById(R.id.heartCnt);
        answerTextView = itemView.findViewById(R.id.answerCnt);
        postImageView = itemView.findViewById(R.id.image);
    }

    void bind(@NonNull Post post, Context applicationContext, String subject) {

        titleView.setText(post.getTitle());
        textView.setText(post.getText());
        heartTextView.setText((post.getHeart()));
        answerTextView.setText(post.getAnswer());

        //나아중

        //과목 이미지
        if (post.getImage()) {
            StorageReference submitProfile = FirebaseStorage.getInstance().getReferenceFromUrl(post.getImageSource());
            submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(applicationContext).load(uri).into(postImageView);
                }
            });
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.rightMargin = (int) applicationContext.getResources().getDimension(R.dimen.PostLayoutMargin);
            params.leftMargin = (int) applicationContext.getResources().getDimension(R.dimen.PostLayoutMargin);

            postImageView.setLayoutParams(params);
        }

        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭 시 해당 게시글로 이동
                questionParcelableData data = new questionParcelableData(subject, post.getPostNum());
                Intent intent = new Intent(applicationContext, DetailOfQuestion.class).putExtra("questionParcelableData", data);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                applicationContext.startActivity(intent);
            }
        });
    }
}
