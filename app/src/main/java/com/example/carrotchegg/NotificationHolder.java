package com.example.carrotchegg;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class NotificationHolder extends RecyclerView.ViewHolder {

    RelativeLayout postLayout;
    TextView titleView, textView, heartTextView, answerTextView;
    ImageView postImageView;
    ImageView heart,answer;
    String msg;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;





    NotificationHolder(@NonNull View itemView) {
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
        db.collection("userData").document(uid).collection("notification")
                .whereEqualTo("post",scrap.getPost())
                .whereEqualTo("subject",scrap.getSubject())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Timestamp timestamp = (Timestamp) document.get("timestamp");
                            answerTextView.setText("· " + formatTimeString(timestamp.toDate().getTime()));
                        }
                    }
                });

        db.collection("subjects").document(scrap.getSubject())
                .collection("post").document(Long.toString(scrap.getPost()))
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        Map<String, Object> getData = task.getResult().getData();

                        titleView.setText("새로운 답변이 있습니다.");
                        textView.setText("'"+(String) getData.get("title")+"'글에 답변이 추가되었어요");
                        heartTextView.setText((String)getData.get("subject"));
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
    public String toTimeStamp(long num){
        Date toTimeStamp = new Date(num);
        SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
        return datef.format(toTimeStamp) ;
    }
    private static class TIME_MAXIMUM{
        public static final int SEC = 60;
        public static final int MIN = 60;
        public static final int HOUR = 24;
        public static final int DAY = 30;
        public static final int MONTH = 12;
    }
    public String formatTimeString(long regTime) {
        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - regTime) / 1000;

        if (diffTime < TIME_MAXIMUM.SEC) {
            msg = "방금 전";
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + "분 전";
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + "시간 전";
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + "일 전";
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + "달 전";
        } else {
            msg = (diffTime) + "년 전";
        }
        return msg;
    }
}
