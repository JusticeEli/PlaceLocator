package com.justice.placelocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEdtTxt, passwordEdtTxt, confirmPasswordEdtTxt;
    private TextView loginTxtView;
    private Button registerBtn;

    ///////////PROGRESS lINEAR_LAYOUT/////////
    private LinearLayout load;
    private TextView loadTxtView;
    private LinearLayout linearLayout;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initwidgets();
        setUpAnimation();
        firebaseAuth = FirebaseAuth.getInstance();
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
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        }
    }


    private void setOnClickListeners() {

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fieldsAreEmpty()) {
                    Toasty.error(RegisterActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgress(true);
                String email = emailEdtTxt.getText().toString().trim();
                String password = passwordEdtTxt.getText().toString().trim();
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        } else {
                            String error = task.getException().getMessage();
                            Toasty.error(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                        showProgress(false);
                    }
                });


            }


        });


        loginTxtView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

            }
        });

    }

    private boolean fieldsAreEmpty() {
        if (emailEdtTxt.getText().toString().trim().isEmpty() || passwordEdtTxt.getText().toString().trim().isEmpty() || confirmPasswordEdtTxt.getText().toString().trim().isEmpty()) {
            return true;
        }
        return false;
    }

    /////////////////////PROGRESS_BAR////////////////////////////
    private void showProgress(boolean show) {
        if (show) {
            load.setVisibility(View.VISIBLE);
            loadTxtView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.GONE);

        } else {
            load.setVisibility(View.GONE);
            loadTxtView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);


        }

    }

    private void initwidgets() {
        emailEdtTxt = findViewById(R.id.emailEdtTxt);
        passwordEdtTxt = findViewById(R.id.passwordEdtTxt);
        confirmPasswordEdtTxt = findViewById(R.id.confirmPasswordEdtTxt);
        loginTxtView = findViewById(R.id.loginTxtView);
        registerBtn = findViewById(R.id.registerBtn);

        //////////////////PROGRESS_BAR//////////////////////
        load = findViewById(R.id.loadingLinearLayout);
        loadTxtView = findViewById(R.id.loadTxtView);
        linearLayout = findViewById(R.id.linearLayout);
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }


    }
}
