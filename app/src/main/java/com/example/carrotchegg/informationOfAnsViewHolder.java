package com.example.carrotchegg;

import static android.provider.CalendarContract.CalendarCache.URI;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class informationOfAnsViewHolder extends RecyclerView.ViewHolder {
    RelativeLayout relativeLayout;
    FirebaseStorage storage;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : null;
    TextView name;
    TextView mtextView;
    ImageView mImageView;
    ImageView icon;
    ImageView heart;
    ImageView heart_empty;
    TextView heartNum;

    public informationOfAnsViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.question_item_title);
        mtextView = itemView.findViewById(R.id.question_item_content);
        mImageView = itemView.findViewById(R.id.question_item_image);
        relativeLayout = itemView.findViewById(R.id.question_item_layout);
        heart = itemView.findViewById(R.id.question_item_heart);
        heart_empty = itemView.findViewById(R.id.question_item_heart_empty);
        icon = itemView.findViewById(R.id.question_item_imageOfWriter);
        heartNum = itemView.findViewById(R.id.question_item_heartNum);
    }

    void bind(@NonNull informationOfAns information, Context applicationContext, String subject) {
        FindNickName(information.getUserName());
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        storageRef.child("user_image").child(information.getUserName() + ".png")
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(applicationContext).load(uri).into(icon);
                    }
                });
        mtextView.setText(information.getContent());
        heartNum.setText(String.valueOf(information.getHeart()));
        if (information.getImage())
            downloadImageTo(information.getImageSource());

        boolean heartCheck = false; //하트 눌렀나 체크하기
        List<String> heartUserIdList = information.getHeartUserId();
        if (heartUserIdList!= null && !heartUserIdList.isEmpty()) {
            for (String str : heartUserIdList) {
                if (str.equals(uid)) {
                    heartCheck = true;
                    break;
                }
            }
        }
        if(heartCheck){
            heart.setVisibility(View.VISIBLE);
            heart_empty.setVisibility(View.GONE);
        }


        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭 시 해당 게시글로 이동
                ansParcelableData data = new ansParcelableData(information.getUserName(),subject, information.getPostNum(),information.getAnsNum());
                Intent intent = new Intent(applicationContext, DetailOfAnswer.class).putExtra("questionParcelableData", data);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                applicationContext.startActivity(intent);
            }
        });
        // Change heart color and count it up/down
        heart_empty.setOnClickListener(v -> {
            heartNum.setText(String.valueOf(Integer.parseInt(heartNum.getText().toString())+1));
            db.collection("subjects").document(information.getSubject())
                    .collection("post").document(String.valueOf(information.getPostNum()))
                    .collection("answers").document(String.valueOf(information.getAnsNum()))
                    .update("heart",Integer.parseInt(heartNum.getText().toString())); // 하트 숫자 업데이트

            db.collection("subjects").document(information.getSubject())
                    .collection("post").document(String.valueOf(information.getPostNum()))
                    .collection("answers").document(String.valueOf(information.getAnsNum()))
                    .update("heartUserId", FieldValue.arrayUnion(uid));
            //binding.h.setText("조회수 "+String.valueOf(information.getHeart()));//여기 하트 숫자 갱신해주기
            heart.setVisibility(View.VISIBLE);
            heart_empty.setVisibility(View.GONE);
        });
        heart.setOnClickListener(v->{
            heartNum.setText(String.valueOf(Integer.parseInt(heartNum.getText().toString())-1));
            db.collection("subjects").document(information.getSubject())
                    .collection("post").document(String.valueOf(information.getPostNum()))
                    .collection("answers").document(String.valueOf(information.getAnsNum()))
                    .update("heart",Integer.parseInt(heartNum.getText().toString())); // 하트 숫자 업데이트

            db.collection("subjects").document(information.getSubject())
                    .collection("post").document(String.valueOf(information.getPostNum()))
                    .collection("answers").document(String.valueOf(information.getAnsNum()))
                    .update("heartUserId", FieldValue.arrayRemove(uid));
            //binding.h.setText("조회수 "+String.valueOf(information.getHeart()));//여기 하트 숫자 갱신해주기
            heart.setVisibility(View.GONE);
            heart_empty.setVisibility(View.VISIBLE);
        });
    }
    private void downloadImageTo(String uri) {
        storage = FirebaseStorage.getInstance();
        // Create a reference to a file from a Google Cloud Storage URI
        StorageReference gsReference = storage.getReferenceFromUrl(uri); // from gs://~~~
        // Download directly from StorageReference using Glide
        // (See MyAppGlideModule for Loader registration)
        Glide.with(itemView /* context */)
                .load(gsReference)
                .error(R.drawable.heart)
                .into(mImageView);
    }
    private void FindNickName(String id){
        DocumentReference loadName = db.collection("userData").document(id);
        loadName.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String s = task.getResult().getData().get("nickname").toString();
                    name.setText(s);
                }
            }
        });
    }


//    public void remove(int position) {
//        try {
//            itemData.remove(position);
//            notifyDataSetChanged();
//        } catch (IndexOutOfBoundsException e) {
//            e.printStackTrace();
//        }
//    }
}
