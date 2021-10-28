package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class COnfigureUser extends AppCompatActivity {

    private Button button;
    private EditText editText;
    private DatabaseReference ref;
    private String key;
    private boolean isFound=false;
    private FirebaseAuth mAuth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c_onfigure_user);
        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.bar_color));
        mAuth=FirebaseAuth.getInstance();
        button=findViewById(R.id.verify_btn_configure_user);
        dialog=new ProgressDialog(this);
        editText=findViewById(R.id.email_configure_user);
        ref=FirebaseDatabase.getInstance().getReference().child("Users");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent=new Intent(COnfigureUser.this,popUpActivity.class);
//                startActivity(intent);
//                finish();
//                verifyUser();
                showCustomDialog();

            }
        });
    }



    private void verifyUser() {
        String input=editText.getText().toString();
        if(input.isEmpty()){
            editText.setError("Please enter your email address");
            editText.requestFocus();
            Toast.makeText(COnfigureUser.this,"Please enter your email address",Toast.LENGTH_LONG).show();
        }
        else{
           /* dialog.setTitle("Email Verification");
            dialog.setMessage("Please wait, while we wre verifying your password");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();*/
            /*ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot ds:snapshot.getChildren()){
                            key=ds.child("email").getValue().toString();
                            Log.d("all email",key);
                            if(key.equals(input)){
                                isFound=true;

                                //Toast.makeText(COnfigureUser.this,"Your email is verified",Toast.LENGTH_LONG).show();
                                //dialog.dismiss();
                                Intent intent=new Intent(COnfigureUser.this,ConfirmPassword.class);
                                intent.putExtra("uid",ds.getKey());
                                Log.d("emailkey",key);
                                startActivity(intent);
                                break;
                            }

                        }
                        if(isFound==false)
                            Toast.makeText(COnfigureUser.this,"This email address does not exist",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });*/

            mAuth.sendPasswordResetEmail(input).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){

                       Toast.makeText(COnfigureUser.this,"Please check your email if you want to reset your password",Toast.LENGTH_LONG).show();

                       Intent intent=new Intent(COnfigureUser.this,MainActivity.class);
                       startActivity(intent);
                   }
                   else{
                       Toast.makeText(COnfigureUser.this,task.getException().toString(),Toast.LENGTH_LONG).show();

                   }
                }
            });
        }

    }
    private void showCustomDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(COnfigureUser.this);
        Log.d("inside", "showCustomDialog: ");
        builder.setMessage("We will mail you a link.Please press Ok and Click On that link to reset your password.")
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        verifyUser();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        builder.show();
    }

}