package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private EditText editText1,editText2,editText3,editText4,editText5;

    private Button button;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ProgressDialog loadingBar;
    private ImageView imageView1,imageView2;
    private boolean isShown=false,isShownCom=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FirebaseApp.initializeApp(this);
        findAllViewId();
        loadingBar=new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShown){
                    imageView1.setImageResource(R.drawable.view);
                    isShown=false;
                    editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else if(!isShown){
                    imageView1.setImageResource(R.drawable.visibility);
                    isShown=true;
                    editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShownCom){
                    imageView2.setImageResource(R.drawable.view);
                    isShownCom=false;
                    editText3.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else if(!isShownCom){
                    imageView2.setImageResource(R.drawable.visibility);
                    isShownCom=true;
                    editText3.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    private void createNewAccount() {
        String email = editText4.getText().toString();
        String password = editText2.getText().toString();
        String confirmPass = editText3.getText().toString();
        String username = editText1.getText().toString();
        String phone = editText5.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            editText1.setError("Please enter your username");
            editText1.requestFocus();
        }
        else if (TextUtils.isEmpty(password))
        {
            editText2.setError("Please enter your password");
            editText2.requestFocus();
        }

        else if (TextUtils.isEmpty(confirmPass))
        {
            editText3.setError("Please confirm your password");
            editText3.requestFocus();
        }
        else if (TextUtils.isEmpty(email))
        {

            editText4.setError("Please enter your email address");
            editText4.requestFocus();
        }

        else if (TextUtils.isEmpty(phone))
        {
            editText5.setError("Please enter your phone");
            editText5.requestFocus();
        }
        else if(!password.equals(confirmPass)){
            Toast.makeText(this,"password did not match",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we wre creating new account for you...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserID = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> map = new HashMap<>();
                                map.put("uid", currentUserID);
                                map.put("username", username);
                                map.put("password", password);
                                map.put("email", email);
                                map.put("phone", phone);
                                map.put("device_token", deviceToken);

                                SharedPreferences pref =getSharedPreferences("nameofemail", Context.MODE_PRIVATE); // 0 - for private mode
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString(email,username);

                                Log.d("goes right?",email+","+username);

                                //editor.putString(e);
                                editor.commit();
                                RootRef.child("Users").child(currentUserID).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            loadingBar.dismiss();
                                            Toast.makeText(SignUp.this, "Account Created Successfully...", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            String message = task.getException().toString();
                                            Toast.makeText(SignUp.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });


                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SignUp.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void findAllViewId() {
        editText1=findViewById(R.id.user_name_signup);
        editText2=findViewById(R.id.password_signup);
        editText3=findViewById(R.id.confirm_pass_signup);
        editText4=findViewById(R.id.email_signup);
        editText5=findViewById(R.id.phone_signup);
        button=findViewById(R.id.register_signup);
        imageView1=findViewById(R.id.pass_view);
        imageView2=findViewById(R.id.com_pass_view);
        editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        editText3.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
}