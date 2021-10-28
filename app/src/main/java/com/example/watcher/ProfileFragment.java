package com.example.watcher;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {


    private View view;
    private Button button;
    FirebaseAuth mAuth;
    private Button startButton;
    private CircleImageView circleImageView;
    private TextView textView1,textView2,textView3,textView4,textView5;
    private DatabaseReference RootRef;
    private String currentUserID;
    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_profile, container, false);
        button=view.findViewById(R.id.signout);
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        circleImageView=view.findViewById(R.id.profile_image_frament);
        textView1=view.findViewById(R.id.username_profile_fragment);
        textView2=view.findViewById(R.id.email_profile_fragment);
        textView3=view.findViewById(R.id.phone_profile_fragment);
        textView4=view.findViewById(R.id.address_profile_fragment);
        textView5=view.findViewById(R.id.blood_group_profile_fragment);
        startButton=view.findViewById(R.id.startBackgroundService);

        retrieveUserInfo();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent=new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String str=startButton.getText().toString();
                if(str.equalsIgnoreCase("start"))
                {
                    startButton.setText("Stop");
                    startEmergencyService();
                }else
                {
                    startButton.setText("Start");
                    stopEmergencyService();
                }


            }
        });
        return view;
    }
    private void stopEmergencyService() {
        Intent intent = new Intent(getContext(), LockScreenService.class);
        getActivity().
        stopService(intent);
    }

    private void startEmergencyService(){
        Intent intent = new Intent(getContext(), LockScreenService.class);
        ContextCompat.startForegroundService(getContext(), intent);
    }

    private void retrieveUserInfo() {

        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild("username")){
                                textView1.setText(dataSnapshot.child("username").getValue().toString());
                                // Intent intent = new Intent();
                                // intent.putExtra("notifiername",dataSnapshot.child("username").getValue().toString());

                                // Log.d("is setting",dataSnapshot.child("username").getValue().toString());
                                //  Prevalent.UserName=dataSnapshot.child("username").getValue().toString();
                            }
                            if(dataSnapshot.hasChild("email")){
                                textView2.setText(dataSnapshot.child("email").getValue().toString());
                                //Log.d("is setting",dataSnapshot.child("email").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("phone")){
                                textView3.setText(dataSnapshot.child("phone").getValue().toString());
                                // Log.d("is setting",dataSnapshot.child("phone").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("address")){
                                textView4.setText(dataSnapshot.child("address").getValue().toString());
                                // Log.d("is setting",dataSnapshot.child("address").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("BloodGroup")){
                                textView5.setText(dataSnapshot.child("BloodGroup").getValue().toString());
                                //Log.d("is setting",dataSnapshot.child("BloodGroup").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("image")){
                                // Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(circleImageView);
                                Glide.with(getActivity().getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(circleImageView);
                                Log.d("image came?",dataSnapshot.child("image").getValue().toString());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}