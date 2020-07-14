package com.justice.placelocator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.justice.placelocator.MainActivity.APPOINTMENTS;
import static com.justice.placelocator.MainActivity.PERSONAL_APPOINTMENTS;

public class AppointMentActivity extends AppCompatActivity implements AppointmentAdapter.ItemClicked, CustomDialogAdapter.ItemClicked {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private AppointmentAdapter adapter;
    private RecyclerView recyclerView;
    private static final String TAG = "AppointMentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoint_ment);
        setUpRecyclerView();
        setTitle("All appointments booked by clients");
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
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);

    }

    private void deleteAllPlaces() {
        Toasty.error(this, "functionality not yet implemented").show();
       /**
         *    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
         *                 .setIcon(R.drawable.ic_delete)
         *                 .setBackground(getDrawable(R.drawable.button_bg))
         *                 .setNegativeButton("Cancel", null)
         *                 .setPositiveButton("Delete All", new DialogInterface.OnClickListener() {
         *                     @Override
         *                     public void onClick(DialogInterface dialog, int which) {
         *                         deleteAll();
         *                     }
         *                 });
         *         builder.show();
         */

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
            setUpRecyclerView();
            setSwipeListenerForItems();

        }


    }


    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = firebaseFirestore.collection(APPOINTMENTS);
        FirestoreRecyclerOptions<Appointment> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Appointment>().setQuery(query,
                new SnapshotParser<Appointment>() {
                    @NonNull
                    @Override
                    public Appointment parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        Appointment appointment = snapshot.toObject(Appointment.class);
                        return appointment;
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
    public void itemOnDialogClicked(DocumentSnapshot document) {

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

    @Override
    public void itemOnDialogLongClicked(DocumentSnapshot document) {


        MainActivity.documentSnapshot = document;
        startActivity(new Intent(this, MapsActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void approveAppointment(DocumentSnapshot document, final boolean approved) {
        Map<String, Object> map = new HashMap<>();
        map.put("approved", approved);
        document.getReference().set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (approved) {
                        Toasty.success(AppointMentActivity.this, "Appointment Approved successfully").show();

                    } else {
                        Toasty.success(AppointMentActivity.this, "Appointment cancel").show();

                    }

                } else {
                    Toasty.error(AppointMentActivity.this, "Error; " + task.getException().getMessage()).show();

                }
            }
        });

    }

    @Override
    public void itemClickedDocumentSnapshot(DocumentSnapshot document) {
        Appointment appointment = document.toObject(Appointment.class);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setBackground(getDrawable(R.drawable.recycler_view_dialog_bg));
        builder.setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = (View) inflater.inflate(R.layout.custom_alert_dialog, null);
        builder.setView(dialogView);
        builder.setTitle("Appointments booked by " + appointment.getEmail());

        RecyclerView rv = (RecyclerView) dialogView.findViewById(R.id.rv);

        Query query = firebaseFirestore.collection(APPOINTMENTS).document(appointment.getId()).collection(PERSONAL_APPOINTMENTS).orderBy("expectedFromTime", Query.Direction.DESCENDING);
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

        final CustomDialogAdapter dialogAdapter = new CustomDialogAdapter(AppointMentActivity.this, firestoreRecyclerOptions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(dialogAdapter);

///////////////set up swipe listener ///// so that supervisor can delete the appointment by scrolling of the screen//////////

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                deleteAppointment(viewHolder.getAdapterPosition(), dialogAdapter);

            }
        }).attachToRecyclerView(rv);
////////////////////////////////
        AlertDialog dialog = builder.create();

        dialog.show();


///////////previous code///////
        /**
         *    MainActivity.documentSnapshot = document;
         *         startActivity(new Intent(this, MapsActivity.class));
         *         overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         */
    }

    private void deleteAppointment(int adapterPosition, CustomDialogAdapter dialogAdapter) {
        dialogAdapter.getSnapshots().getSnapshot(adapterPosition).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(AppointMentActivity.this, "Appointment Deleted").show();

                } else {
                    Toasty.success(AppointMentActivity.this, "Error: " + task.getException().getMessage()).show();

                }
            }
        });
    }


}