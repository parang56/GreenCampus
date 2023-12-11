package com.example.carrotchegg;

import static android.app.Activity.RESULT_OK;

import static androidx.core.os.BundleKt.bundleOf;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carrotchegg.databinding.FragmentQuestBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QuestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QuestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    FragmentQuestBinding binding;

    // TODO: Rename and change types of parameters
    private FirebaseFirestore db;
    private String mParam1;
    int accuseCode;
    private String mParam2;
    private String mParam3;
    private int mParam4;

    public QuestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QuestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QuestFragment newInstance(String param1, String param2,String param3,int param4) {
        QuestFragment fragment = new QuestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString("param3",param3);
        args.putInt("param4",param4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString("param3");
            mParam4 = getArguments().getInt("param4");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quest, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        TextView accuse =view.findViewById(R.id.quest_toolbar);
        View view1 = view.findViewById(R.id.FragmentOne);
        ActivityResultLauncher<Intent> launcher =registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult()
                , new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                Bundle bundle = new Bundle();
                if(o.getResultCode() == RESULT_OK) {
                    accuseCode = o.getData().getIntExtra("accuseResult", -1);
                }
                fragmentManager.beginTransaction().remove(QuestFragment.this).commit();
            }
        });
        accuse.setOnClickListener(v -> {
            Log.d("20201508",mParam1+"-" + mParam2);
            accuseParcelableData data = new accuseParcelableData(mParam2,mParam1,mParam3,mParam4);
            launcher.launch(new Intent(getActivity(), AccuseActivity.class)
                    .putExtra("accuse",data));
        });
        view1.setOnClickListener(v -> {
            //Toast.makeText(getActivity(),"화면누름",Toast.LENGTH_SHORT).show();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().remove(QuestFragment.this).commit();
        });
        getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                // We use a String here, but any type that can be put in a Bundle is supported
                accuseParcelableData resultString = result.getParcelable("bundleKey");

            }
        });
        super.onViewCreated(view, savedInstanceState);
    }
}