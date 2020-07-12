package com.justice.placelocator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;


public class LoginActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt;
    private TextView resetPasswordTxtView, registerTxtView;
    private Button loginBtn;


    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initwidgets();
        setUpAnimation();
        setOnClickListeners();
    }

    private void setUpAnimation() {
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        ScrollView scrollView = findViewById(R.id.scrollView);


        AnimationDrawable animationDrawable1 = (AnimationDrawable) relativeLayout.getBackground();
        AnimationDrawable animationDrawable2 = (AnimationDrawable) scrollView.getBackground();

        animationDrawable1.setEnterFadeDuration(2000);
        animationDrawable2.setEnterFadeDuration(1500);
        animationDrawable1.setExitFadeDuration(1500);
        animationDrawable2.setExitFadeDuration(2000);
        animationDrawable1.start();
        animationDrawable2.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }
    }


    private void setOnClickListeners() {

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toasty.error(LoginActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                String email = emailEdtTxt.getText().toString().trim();
                String password = passwordEdtTxt.getText().toString().trim();
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Login user in...");
                progressDialog.show();
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);


                        } else {
                            String error = task.getException().getMessage();
                            Toasty.error(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                        progressDialog.dismiss();
                    }
                });


            }


        });


        resetPasswordTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEdtTxt.getText().toString().trim().isEmpty()) {
                    Toasty.error(LoginActivity.this, "Please Fill Email !!", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.sendPasswordResetEmail(emailEdtTxt.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(LoginActivity.this, "Password Reset Send", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = task.getException().getMessage();
                            Toasty.error(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();

                        }
                    }
                });
            }
        });
        registerTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            }
        });
    }

    private boolean fieldsAreEmpty() {
        if (emailEdtTxt.getText().toString().trim().isEmpty() || passwordEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }


    private void initwidgets() {
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        passwordEdtTxt = findViewById(R.id.passwordEdtTxt);
        resetPasswordTxtView = findViewById(R.id.resetPasswordEdtTxt);
        loginBtn = findViewById(R.id.loginBtn);
        progressDialog = new ProgressDialog(this);
        registerTxtView = findViewById(R.id.registerTxtView);



    }
}
