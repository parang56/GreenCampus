package com.example.carrotchegg;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class SearchHolder extends RecyclerView.ViewHolder {

    RelativeLayout postLayout;
    TextView titleView, textView, heartTextView, answerTextView;
    ImageView postImageView;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;





    SearchHolder(@NonNull View itemView) {
        super(itemView);

        postLayout = itemView.findViewById(R.id.post);
        titleView = itemView.findViewById(R.id.title);
        textView = itemView.findViewById(R.id.text);
        heartTextView = itemView.findViewById(R.id.heartCnt);
        answerTextView = itemView.findViewById(R.id.answerCnt);
        postImageView = itemView.findViewById(R.id.image);
    }

    //제목이랑 내용도 같이 저장하고
    //하트랑 답변수 없애면 서버 부담이 줄어들듯
    void bind(@NonNull UserData scrap, Context applicationContext) {

        db.collection("subjects").document(scrap.getSubject())
                .collection("post").document(Long.toString(scrap.getPost()))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        Map<String, Object> getData = task.getResult().getData();

                        titleView.setText((String) getData.get("title"));
                        textView.setText((String) getData.get("text"));
                        heartTextView.setText(Long.toString((Long) getData.get("heart")));
                        answerTextView.setText(Long.toString((Long) getData.get("answer")));

                        //이미지
                        if ((boolean) getData.get("image")) {
                            StorageReference submitProfile = FirebaseStorage.getInstance().getReferenceFromUrl((String) getData.get("imageSource"));
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
                    }
                });

        postLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭 시 해당 게시글로 이동
                questionParcelableData data = new questionParcelableData(scrap.getSubject(), (int) scrap.getPost());
                Intent intent = new Intent(applicationContext, DetailOfQuestion.class).putExtra("questionParcelableData", data);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                applicationContext.startActivity(intent);
            }
        });
    }
}
