package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    private CircleImageView circleImageView;
    private EditText editText1,editText2,editText3,editText4,editText5;
    private Button button;
    private ProgressDialog loadingBar;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private Uri imageUri;
    private String myUrl="";
    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.bar_color));
        loadingBar=new ProgressDialog(this);
        findAllViewId();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        RetrieveUserInfo();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
    }

    private void RetrieveUserInfo() {

        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists()){

                            if(dataSnapshot.hasChild("username")){
                                editText1.setText(dataSnapshot.child("username").getValue().toString());
                                Log.d("is setting",dataSnapshot.child("username").getValue().toString());
                                // Prevalent.UserName=dataSnapshot.child("username").getValue().toString();
                            }
                            if(dataSnapshot.hasChild("email")){
                                editText2.setText(dataSnapshot.child("email").getValue().toString());
                                Log.d("is setting",dataSnapshot.child("email").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("phone")){
                                editText3.setText(dataSnapshot.child("phone").getValue().toString());
                                Log.d("is setting",dataSnapshot.child("phone").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("address")){
                                editText4.setText(dataSnapshot.child("address").getValue().toString());
                                Log.d("is setting",dataSnapshot.child("address").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("BloodGroup")){
                                editText5.setText(dataSnapshot.child("BloodGroup").getValue().toString());
                                Log.d("is setting",dataSnapshot.child("BloodGroup").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("image")){
                                // Picasso.get().load(dataSnapshot.child("image").getValue().toString()).into(circleImageView);
                                Glide.with(SettingActivity.this).load(dataSnapshot.child("image").getValue().toString()).into(circleImageView);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GalleryPick  &&  resultCode==RESULT_OK  &&  data!=null)
        {

            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(data.getData()==null){
                Log.d("is success","no");
            }
            else{
                imageUri = data.getData();
                Log.d("is success","yes");
            }


            circleImageView.setImageURI(imageUri);
            uploadImage();

        }
    }

    private void uploadImage() {

        loadingBar.setTitle("Update Profile");
        loadingBar.setMessage("Please wait while we are updating profile picture");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        if (imageUri!=null)
        {
            final StorageReference fileRef=UserProfileImagesRef.child(currentUserID + ".jpg");;
            uploadTask = fileRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Uri downloadUrl=task.getResult();
                        myUrl=downloadUrl.toString();
                        RootRef.child("Users").child(currentUserID).child("image")
                                .setValue(myUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(SettingActivity.this, "Image updated, Successfully...", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                        {
                                            String message = task.getException().toString();
                                            Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });

                    }
                    else {
                        loadingBar.dismiss();
                        Toast.makeText(SettingActivity.this, "Error Uploading Photo", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
        else {
            Toast.makeText(this, "Image not selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void UpdateSettings() {

        String s1=editText1.getText().toString();
        String s2=editText2.getText().toString();
        String s3=editText3.getText().toString();
        String s4=editText4.getText().toString();
        String s5=editText5.getText().toString();

        if (TextUtils.isEmpty(s1))
        {
            editText1.setError("Please enter your username");
            editText1.requestFocus();
        }
        else if (TextUtils.isEmpty(s2))
        {
            editText2.setError("Please enter your email address");
            editText2.requestFocus();
        }
        else if (TextUtils.isEmpty(s3))
        {
            editText3.setError("Please enter your phone number");
            editText3.requestFocus();
        }
        else if (TextUtils.isEmpty(s4))
        {
            editText4.setError("Please enter your address");
            editText4.requestFocus();
        }
        else if (TextUtils.isEmpty(s5))
        {
            editText5.setError("Please enter your blood group");
            editText5.requestFocus();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("username", s1);
            profileMap.put("email", s2);
            profileMap.put("phone",s3);
            profileMap.put("address", s4);
            profileMap.put("BloodGroup",s5);
            SharedPreferences pref =getSharedPreferences("nameofuser", Context.MODE_PRIVATE); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("careename",s1);
            editor.commit();
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {

                                Toast.makeText(SettingActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void findAllViewId() {
        circleImageView=findViewById(R.id.profile_image_setting);
        editText1=findViewById(R.id.username_profile_setting);
        editText2=findViewById(R.id.email_profile_setting);
        editText3=findViewById(R.id.phone_profile_setting);
        editText4=findViewById(R.id.address_profile_setting);
        editText5=findViewById(R.id.blood_group_profile_setting);
        button=findViewById(R.id.save_profile_editing_setting);
    }
}