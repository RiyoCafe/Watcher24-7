package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.watcher.helper.DeviceLocationFinder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    //AIzaSyD5T6g8CGfcnTE01eX4gm5KCm-B5yMLhLY
    private static final String TAG = "MainActivity";
    public static final String NOTIFICATION_CHANNEL_ID = "TEST_CHANNEL";
    private EditText editText1,editText2;
    private ImageView imageView;
    private Button button1,button2;
    private CheckBox checkBox;
    private TextView textView;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private boolean isShown=false;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        mAuth=FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        progressDialog=new ProgressDialog(this);
        UsersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        Paper.init(this);
        DeviceLocationFinder.getCurrentDeviceLocation(this, new DeviceLocationFinder.OnDeviceLocationFoundListener() {
            @Override
            public void onDeviceLocationFound(LatLng latLng) {
                Log.d(TAG, "onDeviceLocationFound: lat: "+latLng.latitude+ " lon: "+latLng.longitude);
            }
        });
        findAllViewId();

        String UserEmailKey = Paper.book().read(Prevalent.UserEmailKey);
        String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);

        if (UserEmailKey!=null &&  UserPasswordKey!=null)
        {
            editText1.setText(UserEmailKey);
            editText2.setText(UserPasswordKey);
            checkBox.setChecked(true);

        }

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginApp();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isShown){
                    imageView.setImageResource(R.drawable.view);
                    isShown=false;
                    editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else if(!isShown){
                    imageView.setImageResource(R.drawable.visibility);
                    isShown=true;
                    editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

    }

    private void resetPassword() {
        Intent intent=new Intent(MainActivity.this,COnfigureUser.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();


        if (currentUser != null)
        {
            Intent intent=new Intent(MainActivity.this,FinalHomeActivity2.class);
            startActivity(intent);
        }

    }
    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Our test channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = super.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }


    private void loginApp() {
        String password=editText2.getText().toString().trim();
        String email=editText1.getText().toString().trim();
        if(password.isEmpty()){
            editText2.setError("Please enter  password");
            editText2.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editText1.setError("Please enter your email address");
            editText1.requestFocus();
            return;
        }
        progressDialog.setTitle("Login Account");
        progressDialog.setMessage("Please wait,while we are checking the credentials");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){


                    if(checkBox.isChecked())
                    {
                        Paper.book().write(Prevalent.UserEmailKey, email);
                        Paper.book().write(Prevalent.UserPasswordKey, password);

                    }
                    else{
                        Paper.book().destroy();
                    }

                    String currentUserId = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    UsersRef.child(currentUserId).child("device_token")
                            .setValue(deviceToken)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();
                                        Intent intent=new Intent(MainActivity.this, FinalHomeActivity2.class);
                                        startActivity(intent);
                                        Toast.makeText(MainActivity.this,"You have successfully logged in",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            }
        });
    }



    private void findAllViewId() {

        editText1=findViewById(R.id.email_login);
        editText2=findViewById(R.id.password_login);
        imageView=findViewById(R.id.view_pass_login);
        button1=findViewById(R.id.login_btn);
        button2=findViewById(R.id.signup_login);
        checkBox=findViewById(R.id.forget_password);
        textView=findViewById(R.id.forget_pass_textview);
        editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
}