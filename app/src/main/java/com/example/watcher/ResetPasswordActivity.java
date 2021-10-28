package com.example.watcher;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ResetPasswordActivity extends AppCompatActivity {
    private EditText phonenumber;
    private Button codesent;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        phonenumber=findViewById(R.id.editTextPhone);
        codesent=findViewById(R.id.button);



        codesent.setOnClickListener(v ->
        {
            sendOtpToUser("+88" + phonenumber.getText().toString());

        });
    }

    private void sendOtpToUser(String phoneNumberWithCountryCode)
    {

        Log.d("Verification" , phoneNumberWithCountryCode);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberWithCountryCode,
                60,
                TimeUnit.SECONDS,
                ResetPasswordActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("Verify Phone Activity", e.getMessage());
                        Toast.makeText(ResetPasswordActivity.this, "Verification code sending failed, Please try again", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                        loader.setVisibility(View.GONE);
//                        sendOTP.setVisibility(View.VISIBLE);

                        Intent intent=new Intent(ResetPasswordActivity.this,Verification.class);
                        intent.putExtra("verifiedcode",verificationId);
                        startActivity(intent);
                    }
                }
        );
    }

}