package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private TextView textView1,textView2;
    private Button button1,button2;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef,typeRef;
    private FirebaseAuth mAuth;
    private String receiverUserID,senderUserID,Current_State;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findAllViewId();
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        typeRef= FirebaseDatabase.getInstance().getReference().child("User Type");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        // NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        Log.d("whose end",receiverUserID);
        senderUserID = mAuth.getCurrentUser().getUid();
        Current_State = "new";
        RetrieveUserInfo();

    }

    @Override
    protected void onStart() {
        super.onStart();
        RetrieveUserInfo();
    }

    private void RetrieveUserInfo()
    {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists())  &&  (dataSnapshot.hasChild("image")))
                {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("username").getValue().toString();
                    //String userstatus = dataSnapshot.child("status").getValue().toString();

                    //Picasso.get().load(userImage).placeholder(R.drawable.profile_user).into(circleImageView);
                    Glide.with(ProfileActivity.this).load(userImage).placeholder(R.drawable.profile_user).into(circleImageView);
                    textView1.setText(userName);
                    //userProfileStatus.setText(userstatus);


                    ManageChatRequests();
                }
                else
                {
                    if(dataSnapshot.child("username").getValue().toString()!=null) {
                        String userName = dataSnapshot.child("username").getValue().toString();
                        //String userstatus = dataSnapshot.child("status").getValue().toString();

                        textView1.setText(userName);
                        //userProfileStatus.setText(userstatus);}
                    }


                    ManageChatRequests();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void ManageChatRequests()
    {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                Current_State = "request_sent";
                                button1.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                Current_State = "request_received";
                                button1.setText("Accept Chat Request");

                                button2.setVisibility(View.VISIBLE);
                                button2.setEnabled(true);

                                button2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                Current_State = "friends";
                                                button1.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



        if (!senderUserID.equals(receiverUserID))
        {
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    button1.setEnabled(false);

                    if (Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    if (Current_State.equals("request_sent"))
                    {
                        CancelChatRequest();
                    }
                    if (Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends"))
                    {
                        RemoveSpecificContact();
                    }
                }
            });
        }
        else
        {
            button1.setVisibility(View.INVISIBLE);
        }
    }



    private void RemoveSpecificContact()
    {

        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                button1.setEnabled(true);
                                                Current_State = "new";
                                                button1.setText("Send friend request ");

                                                button2.setVisibility(View.INVISIBLE);
                                                button2.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


        typeRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            typeRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Log.d("is removed","success");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }



    private void AcceptChatRequest()
    {
        typeRef.child(senderUserID).child(receiverUserID).child("Type").setValue("Caree");
        typeRef.child(receiverUserID).child(senderUserID).child("Type").setValue("Saver");

        FirebaseMessaging.getInstance().subscribeToTopic(senderUserID+"_"+receiverUserID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg ="task is successful";
                        if (!task.isSuccessful()) {
                            msg = "task is not successfuol";
                        }
                        Log.d("weather topic", msg);
                        // Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().subscribeToTopic(receiverUserID)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "u are in danger";
                        if (!task.isSuccessful()) {
                            msg = "u are failed";
                        }
                        Log.d("accept chat request", msg);
                        // Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    button1.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    button1.setText("Remove this Contact");

                                                                                    button2.setVisibility(View.INVISIBLE);
                                                                                    button2.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }




    private void CancelChatRequest()
    {

        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                button1.setEnabled(true);
                                                Current_State = "new";
                                                button1.setText("Send Friend Request");

                                                button2.setVisibility(View.INVISIBLE);
                                                button2.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }




    private void SendChatRequest()
    {

        HashMap<String, Object> map = new HashMap<>();
        map.put("request_type", "sent");
        map.put("type", "caree");
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("request_type", "received");
                            map.put("type", "carer");
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .updateChildren(map)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            /*if (task.isSuccessful())
                                            {
                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    SendMessageRequestButton.setEnabled(true);
                                                                    Current_State = "request_sent";
                                                                    SendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });
                                            }*/
                                            if (task.isSuccessful())
                                            {
                                                Log.d("send request subscried","yes or no");
                                                FirebaseMessaging.getInstance().subscribeToTopic(senderUserID+"_"+receiverUserID)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                String msg ="task is successful";
                                                                if (!task.isSuccessful()) {
                                                                    msg = "task is not successfuol";
                                                                }
                                                                Log.d("weather topic", msg);
                                                                // Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                button1.setEnabled(true);
                                                Current_State = "request_sent";
                                                button1.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void findAllViewId() {
        circleImageView=findViewById(R.id.image_profile);
        textView1=findViewById(R.id.username_profile);
        textView2=findViewById(R.id.status_profile);
        button1=findViewById(R.id.send_msg_req_btn);
        button2=findViewById(R.id.decline_msg_req_btn);
    }
}