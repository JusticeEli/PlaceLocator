package com.justice.placelocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;

import es.dmoral.toasty.Toasty;

public class AppointMentActivity extends AppCompatActivity implements AppointmentAdapter.ItemClicked {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private AppointmentAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoint_ment);
        setUpRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appointment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.bookAppointmentItem:
                startActivity(new Intent(this, BookAppointMentActivity.class));
                break;
            case R.id.appointmentItem:
                startActivity(new Intent(this, AppointMentActivity.class));
                break;
            case R.id.logoutItem:
                logoutUser();
                break;
            case R.id.deleteItem:
                deleteAllPlaces();

                break;

        }
        return true;
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

    }

    private void deleteAllPlaces() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_delete)
                .setBackground(getDrawable(R.drawable.button_bg))
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAll();
                    }
                });
        builder.show();
    }

    private void deleteAll() {
        for (int i = 0; i < adapter.getSnapshots().size(); i++) {
            adapter.getSnapshots().getSnapshot(i).getReference().delete().addOnCompleteListener(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(AppointMentActivity.this, LoginActivity.class));
            finish();
        } else {
            setTitle(firebaseAuth.getCurrentUser().getEmail());
            setUpRecyclerView();
            setSwipeListenerForItems();

        }


    }


    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = firebaseFirestore.collection("appointments").orderBy("expectedFromTime", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<AppointmentData> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<AppointmentData>().setQuery(query,
                new SnapshotParser<AppointmentData>() {
                    @NonNull
                    @Override
                    public AppointmentData parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        AppointmentData appointmentData = snapshot.toObject(AppointmentData.class);
                        appointmentData.setId(snapshot.getId());
                        return appointmentData;
                    }
                }).setLifecycleOwner(AppointMentActivity.this).build();


        adapter = new AppointmentAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void setSwipeListenerForItems() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deletePlaceFromDatabase(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);
    }

    private void deletePlaceFromDatabase(int position) {
        adapter.getSnapshots().getSnapshot(position).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(AppointMentActivity.this, "deletion success", Toasty.LENGTH_SHORT).show();
                } else {
                    Toasty.error(AppointMentActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    public void itemClickedDocumentSnapshot(DocumentSnapshot document) {
        MainActivity.documentSnapshot = document;
        startActivity(new Intent(this, MapsActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void itemLongClicked(DocumentSnapshot document) {
        AppointmentData appointmentData = document.toObject(AppointmentData.class);

        StringBuilder stringBuilder = new StringBuilder();
        if (appointmentData.getCheckInTime() == null) {
            stringBuilder.append("CheckInTime : User has not checked in");
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        } else {
            stringBuilder.append("CheckedInTime :" + appointmentData.getCheckInTime());
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        }

        stringBuilder.append("ExpectedTime :" + appointmentData.getExpectedFromTime());
        stringBuilder.append("\n");
        stringBuilder.append("\n");

        if (appointmentData.getCheckOutTime() == null) {
            stringBuilder.append("CheckOutTime : User has not checked out");
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        } else {
            stringBuilder.append("CheckOutTime :" + appointmentData.getCheckOutTime());
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        }

        stringBuilder.append("expectTime :" + appointmentData.getExpectToTime());
        stringBuilder.append("\n");
        stringBuilder.append("\n");

        if (appointmentData.getCheckInLocation() == null) {
            stringBuilder.append("CheckInLocation : User has not checked in");
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        } else {
            stringBuilder.append("CheckInLocation :" + appointmentData.getCheckInLocation().getAddress());
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        }
        if (appointmentData.getCheckOutLocation() == null) {
            stringBuilder.append("CheckOutLocation :User has not checked out");
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        } else {
            stringBuilder.append("CheckOutLocation :" + appointmentData.getCheckOutLocation().getAddress());
            stringBuilder.append("\n");
            stringBuilder.append("\n");

        }

        stringBuilder.append("Expected Location to check in and out :" + appointmentData.getDestinationLocation());
        stringBuilder.append("\n");


        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle(appointmentData.getEmail())
                .setMessage(stringBuilder.toString())
                .setBackground(getDrawable(R.drawable.button_bg))
                .setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();

    }


}