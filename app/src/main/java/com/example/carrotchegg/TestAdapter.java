package com.example.carrotchegg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import java.util.ArrayList;


public class TestAdapter extends RecyclerView.Adapter{
    //리사이클러뷰에 넣을 데이터 리스트
    ArrayList<Post> dataModels;
    Context context;
    ImageView postImageView;

    //생성자를 통하여 데이터 리스트 context를 받음
    public TestAdapter(Context context, ArrayList<Post> dataModels){
        this.dataModels = dataModels;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        //데이터 리스트의 크기를 전달해주어야 함
        return dataModels.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //자신이 만든 itemview를 inflate한 다음 뷰홀더 생성
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_form,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);


        //생선된 뷰홀더를 리턴하여 onBindViewHolder에 전달한다.
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyViewHolder myViewHolder = (MyViewHolder)holder;

        myViewHolder.titleView.setText(dataModels.get(position).getTitle());
        myViewHolder.textView.setText(dataModels.get(position).getText());
        myViewHolder.heartTextView.setText(dataModels.get(position).getHeart());
        myViewHolder.answerTextView.setText(dataModels.get(position).getAnswer());

        myViewHolder.postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭 시 해당 게시글로 이동
                questionParcelableData data = new questionParcelableData("C", dataModels.get(holder.getBindingAdapterPosition()).getPostNum());
                Intent intent = new Intent(context, DetailOfQuestion.class).putExtra("questionParcelableData", data);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(intent);
            }
        });

        //과목 이미지
        if (dataModels.get(position).getImage()) {
            StorageReference submitProfile = FirebaseStorage.getInstance().getReferenceFromUrl(dataModels.get(position).getImageSource());
            submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(postImageView);
                }
            });
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.rightMargin = (int) context.getResources().getDimension(R.dimen.PostLayoutMargin);
            params.leftMargin = (int) context.getResources().getDimension(R.dimen.PostLayoutMargin);

            postImageView.setLayoutParams(params);
        }
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout postLayout;
        TextView titleView, textView, heartTextView, answerTextView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            postLayout = itemView.findViewById(R.id.post);
            titleView = itemView.findViewById(R.id.title);
            textView = itemView.findViewById(R.id.text);
            heartTextView = itemView.findViewById(R.id.heartCnt);
            answerTextView = itemView.findViewById(R.id.answerCnt);
            postImageView = itemView.findViewById(R.id.image);
        }
    }

}
