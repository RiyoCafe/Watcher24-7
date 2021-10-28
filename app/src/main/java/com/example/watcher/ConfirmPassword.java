package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ConfirmPassword extends AppCompatActivity {

    private EditText editText1,editText2;
    private Button button,button2;
    private  DatabaseReference ref;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmpassword);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.bar_color));
        findAllViewId();
       // uid=getIntent().getExtras().getString("uid");
        //ref=FirebaseDatabase.getInstance().getReference().child("Users");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewPassword();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ConfirmPassword.this,MainActivity.class);
                //intent.putExtra("num",)
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });
    }

    private void saveNewPassword() {

        String pass=editText1.getText().toString();
        String confirm_pass=editText2.getText().toString();

        if(pass.isEmpty()){
            editText1.setError("Please enter your new password");
            editText1.requestFocus();
        }
        else if(confirm_pass.isEmpty()){
            editText2.setError("Please confirm  your new password");
            editText2.requestFocus();
        }
        else{
            if(pass.equals(confirm_pass)){


                ref.child(uid).child("password").setValue(pass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(ConfirmPassword.this,"Your password changed successfully",Toast.LENGTH_LONG).show();

                        }
                        else{
                            Toast.makeText(ConfirmPassword.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }else{
                Toast.makeText(ConfirmPassword.this,"Your password did not match",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void findAllViewId() {
        editText1=findViewById(R.id.pass_confirm_pass_activity);
        editText2=findViewById(R.id.confirm_pass_confirm_pass_activity);
        button=findViewById(R.id.submit_confirm_passs_activity);
        button2=findViewById(R.id.loginback_confirm_pass);
    }
}