package com.example.watcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Verification  extends AppCompatActivity {
    String verificationCodeBySystem;
    private Pinview pinview;
    private EditText pinInput;
    private Button submit;
    private ProgressBar load;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verificationcode);
        pinInput = findViewById(R.id.pinInput);
        submit=findViewById(R.id.button2);
        load=findViewById(R.id.progressBar);
        load.setVisibility(View.GONE);
        mAuth=FirebaseAuth.getInstance();
        String codedata=getIntent().getStringExtra("verifiedcode");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                load.setVisibility(View.VISIBLE);

                check(codedata,pinInput.getText().toString());
            }
        });
    }

    private void check(String codedata, String code) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(codedata, code);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    load.setVisibility(View.GONE);
                    Intent intent=new Intent(Verification.this,ConfirmPassword.class);
                    //intent.putExtra("num",)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else
                {
                    load.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
