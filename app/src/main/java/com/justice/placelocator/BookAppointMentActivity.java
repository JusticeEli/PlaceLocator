package com.justice.placelocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class BookAppointMentActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private EditText fromEdtTxt;
    private EditText toEdtTxt;
    private EditText destinationEdtTxt;
    private Button submitBtn;
    private Calendar fromCalender = Calendar.getInstance();
    private Calendar toCalender = Calendar.getInstance();
    public static boolean from;

    private ProgressDialog progressDialog;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appoint_ment);
        initWidgets();
        setOnClickListeners();
    }

    private void setOnClickListeners() {

        fromEdtTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = true;
                showDateTimeDialogFrom();

            }
        });
        toEdtTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = false;
                showDateTimeDialogFrom();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBtnClicked();
            }
        });

    }

    private void showDateTimeDialogFrom() {
        DateDialog dateDialog = new DateDialog();
        dateDialog.show(getSupportFragmentManager(), "date");

        TimeDialog timeDialog = new TimeDialog();
        timeDialog.show(getSupportFragmentManager(), "time");

    }

    private void submitBtnClicked() {
        if (fieldsAreEmpty()) {
            return;
        }
        sendDataToDatabase();
    }


    private void sendDataToDatabase() {

        final AppointmentData appointmentData = new AppointmentData();
        appointmentData.setExpectedFromTime(fromCalender.getTime());
        appointmentData.setExpectToTime(toCalender.getTime());
        appointmentData.setDestinationLocation(destinationEdtTxt.getText().toString());
        appointmentData.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        progressDialog.setTitle("booking appointment...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Appointment appointment=new Appointment();
        appointment.setEmail(currentUser.getEmail());
        appointment.setId(currentUser.getUid());

        firebaseFirestore.collection(MainActivity.APPOINTMENTS).document(currentUser.getUid()).set(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    setTheAppointment(appointmentData);
                } else {
                    Toasty.error(BookAppointMentActivity.this, "Error: " + task.getException().getMessage()).show();
                }
            }
        });


    }

    private void setTheAppointment(AppointmentData appointmentData) {
        firebaseFirestore.collection(MainActivity.APPOINTMENTS).document(currentUser.getUid()).collection(MainActivity.PERSONAL_APPOINTMENTS).add(appointmentData).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toasty.success(BookAppointMentActivity.this, "Appointment booked successfully").show();
                    onBackPressed();
                } else {
                    Toasty.error(BookAppointMentActivity.this, "Error: " + task.getException().getMessage()).show();
                }
                progressDialog.dismiss();
            }
        });

    }

    private boolean fieldsAreEmpty() {
        if (TextUtils.isEmpty(fromEdtTxt.getText().toString()) || TextUtils.isEmpty(toEdtTxt.getText().toString()) || TextUtils.isEmpty(destinationEdtTxt.getText().toString())) {
            Toasty.error(this, "Please fill all Fields!!").show();
            return true;
        }
        return false;
    }

    private void initWidgets() {
        fromEdtTxt = findViewById(R.id.fromEdtTxt);
        toEdtTxt = findViewById(R.id.toEdtTxt);
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
        destinationEdtTxt = findViewById(R.id.destinationEdtTxt);
        submitBtn = findViewById(R.id.submitBtn);
        progressDialog = new ProgressDialog(this);


    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (from) {
            fromCalender.set(Calendar.YEAR, year);
            fromCalender.set(Calendar.MONTH, month);
            fromCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String time = new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(fromCalender.getTime());
            fromEdtTxt.setText(time);

        } else {
            toCalender.set(Calendar.YEAR, year);
            toCalender.set(Calendar.MONTH, month);
            toCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String time = new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(toCalender.getTime());
            toEdtTxt.setText(time);

        }

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (from) {
            fromCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            fromCalender.set(Calendar.MINUTE, minute);


        } else {
            toCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
            toCalender.set(Calendar.MINUTE, minute);

        }

    }
}