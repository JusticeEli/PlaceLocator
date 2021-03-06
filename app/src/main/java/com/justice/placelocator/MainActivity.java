package com.justice.placelocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements MyLocationListener.LocationListenerCallbacks, MainAdapter.ItemClicked {

    public static final String APPOINTMENTS = "appointments";
    public static final String PERSONAL_APPOINTMENTS = "personal_appointments";

    ////////0 seconds/////////
    private static final long LOCATION_REFRESH_TIME = 0;
    ///////0 metre///////////////
    private static final float LOCATION_REFRESH_DISTANCE = 0;

    private static final int LOCATION_PERMISSION = 3;
    private static final int NO_APPOINTMENTS = 8;
    private CoordinatorLayout coordinatorLayout;
    private Button checkInBtn;
    private Button checkOutBtn;
    private ExtendedFloatingActionButton fob;
    private RecyclerView recyclerView;
    private MainAdapter adapter;
    private LocationManager locationManager;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    public static DocumentSnapshot documentSnapshot;

    private LocationListener mLocationListener;

    public static boolean checkIn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        setTitle("My appointments");
        setOnClickListeners();
        networkConnectionIsPresent();
        checkIfUserHasLoggedIn_andHasBookedAnAppointment();
    }

    private void checkIfUserHasLoggedIn_andHasBookedAnAppointment() {
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else {
            ///////////// checks if user has booked an appointment so we can direct him to book appointment first before checkIn
            checkIfUserHasBookedAnAppointment();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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


    private void setUpLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        seeIfGPSisEnabled();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            return;
        }
        mLocationListener = new MyLocationListener(this, locationManager);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

    }

    private void seeIfGPSisEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setBackground(getDrawable(R.drawable.button_bg)).setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        builder.show();

    }

    private void initWidgets() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        checkInBtn = findViewById(R.id.checkInBtn);
        checkOutBtn = findViewById(R.id.checkOutBtn);
        progressDialog = new ProgressDialog(this);
        fob = findViewById(R.id.fob);
    }

    private void setOnClickListeners() {
        checkInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn = true;
                checkInBtnClicked();
            }
        });
        checkOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIn = false;
                checkOutBtnClicked();

            }
        });
        fob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BookAppointMentActivity.class));

            }
        });
    }


    private void checkInBtnClicked() {
        //change visibility of buttons
        checkInBtn.setVisibility(View.GONE);
        checkOutBtn.setVisibility(View.VISIBLE);
        showSnackBar("please wait ,fetching current location...");
        setUpLocationManager();
    }


    @Override
    public void setTextView(String text) {
        showSnackBar(text);
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

    }

    public void sendLocationDataToDatabase(Map<String, Object> map) {
        AllData.currentLocation.getReference()
                .set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (checkIn) {
                        Toasty.success(MainActivity.this, "checked in successfully: ", Toasty.LENGTH_SHORT).show();

                    } else {
                        Toasty.success(MainActivity.this, "checked out successfully: ", Toasty.LENGTH_SHORT).show();

                    }

                } else {
                    Toasty.error(MainActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();

                }
            }
        });

/////////////previous code///////////////////
/**
 *   firebaseFirestore.collection("appointments").document(firebaseAuth.getUid())
 *         .set(map, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
 *             @Override
 *             public void onComplete(@NonNull Task<Void> task) {
 *                 if (task.isSuccessful()) {
 *                     if (checkIn) {
 *                         Toasty.success(MainActivity.this, "checked in successfully: ", Toasty.LENGTH_SHORT).show();
 *
 *                     } else {
 *                         Toasty.success(MainActivity.this, "checked out successfully: ", Toasty.LENGTH_SHORT).show();
 *
 *                     }
 *
 *                 } else {
 *                     Toasty.error(MainActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();
 *
 *                 }
 *             }
 *         });
 */

    }

    private void networkConnectionIsPresent() {

        if (!isOnline()) {
            createNetErrorDialog();
        }
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    ////////dialog shows when internet connection is not available
    protected void createNetErrorDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setBackground(getDrawable(R.drawable.button_bg))
                .setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.finish();
                            }
                        }
                );
        builder.show();
    }


    private void checkOutBtnClicked() {
        //change visibility of buttons
        showSnackBar("please wait,as we check you out...");
        checkInBtn.setVisibility(View.VISIBLE);
        checkOutBtn.setVisibility(View.GONE);
        setUpLocationManager();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_PERMISSION) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);

        }
    }

    @Override
    public void onProviderDisabled() {
        checkInBtn.setVisibility(View.VISIBLE);
        checkOutBtn.setVisibility(View.GONE);

        if (MyLocationListener.currentLocation == null) {
            return;
        }
        showSnackBar("You are currently checked out ,\n Current Location is" + MyLocationListener.currentLocation.get("address").toString());

    }

    @Override
    protected void onStart() {
        super.onStart();

//////previous code
/**
 *
 *   if (firebaseAuth.getCurrentUser() == null) {
 *             startActivity(new Intent(MainActivity.this, LoginActivity.class));
 *             finish();
 *         } else {
 *             setTitle(firebaseAuth.getCurrentUser().getEmail());
 *             ///////////// checks if user has booked an appointment so we can direct him to book appointment first before checkIn
 *             checkIfUserHasBookedAnAppointment();
 *
 *         }
 */


    }

    private void checkIfUserHasBookedAnAppointment() {
        progressDialog.setTitle("fetching data...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        firebaseFirestore.collection(APPOINTMENTS).document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {

                        setUpRecyclerViewForPersonalAppointments();

                        //      checkIfUserHasCheckedIn(task.getResult());
                    } else {
                        Snackbar.make(coordinatorLayout, "Please book an appointment first before you check in", Snackbar.LENGTH_LONG)
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        super.onDismissed(transientBottomBar, event);
                                        Intent intent = new Intent(MainActivity.this, BookAppointMentActivity.class);
                                        startActivityForResult(intent, NO_APPOINTMENTS);

                                    }
                                }).show();
                    }
                } else {
                    Toasty.error(MainActivity.this, "Error: " + task.getException().getMessage()).show();
                }
                progressDialog.dismiss();

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //////if previously there was no any appointment in the database////////////
        setUpRecyclerViewForPersonalAppointments();
    }

    private void setUpRecyclerViewForPersonalAppointments() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = firebaseFirestore.collection(APPOINTMENTS).document(firebaseAuth.getUid()).collection(PERSONAL_APPOINTMENTS).orderBy("expectedFromTime", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<AppointmentData> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<AppointmentData>().setQuery(query,
                new SnapshotParser<AppointmentData>() {
                    @NonNull
                    @Override
                    public AppointmentData parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        AppointmentData appointmentData = snapshot.toObject(AppointmentData.class);
                        appointmentData.setId(snapshot.getId());
                        return appointmentData;
                    }
                }).setLifecycleOwner(MainActivity.this).build();


        adapter = new MainAdapter(this, firestoreRecyclerOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setUpSwipeListener();
    }

    public void checkIfUserHasCheckedIn(DocumentSnapshot result) {
        AppointmentData appointmentData = result.toObject(AppointmentData.class);
        if (appointmentData.isCheckIn()) {
            checkIn = true;
            checkInBtn.setVisibility(View.GONE);
            checkOutBtn.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("You are currently checked in  for an appointment starting from ");
            stringBuilder.append(appointmentData.getExpectedFromTime());
            stringBuilder.append(" and ending at ");
            stringBuilder.append(appointmentData.getExpectToTime());
            stringBuilder.append(",\n you will be required to be at ");
            stringBuilder.append(appointmentData.getDestinationLocation());
            showSnackBar(stringBuilder.toString());
        } else {
            checkIn = false;
            checkInBtn.setVisibility(View.VISIBLE);
            checkOutBtn.setVisibility(View.GONE);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("You are currently checked out  for an appointment starting from ");
            stringBuilder.append(appointmentData.getExpectedFromTime());
            stringBuilder.append(" and ending at ");
            stringBuilder.append(appointmentData.getExpectToTime());
            if (appointmentData.getCheckInLocation() != null) {
                stringBuilder.append(",\n you will be required to be at  ");
                stringBuilder.append(appointmentData.getDestinationLocation());
            }
            showSnackBar(stringBuilder.toString());
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (locationManager != null) {
            locationManager.removeUpdates(mLocationListener);
        }

    }

    @Override
    public void itemClickedDocumentSnapshot(DocumentSnapshot document) {

////////sends user to the map////////////////
        /**
         *    documentSnapshot = document;
         *         startActivity(new Intent(this, MapsActivity.class));
         *         overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
         */

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
                .setTitle("Status of " + appointmentData.getEmail())
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

    private void showSnackBar(String text) {
        Snackbar.make(coordinatorLayout, text, Snackbar.LENGTH_LONG).show();
    }

    private void setUpSwipeListener() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {

                deleteAppointmentFromDatabase(viewHolder.getAdapterPosition());

            }
        }).attachToRecyclerView(recyclerView);
    }

    private void deleteAppointmentFromDatabase(int adapterPosition) {
        adapter.getSnapshots().getSnapshot(adapterPosition).getReference().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toasty.success(MainActivity.this, "deletion success").show();
                    //to prevent user from checkin or checkout from an appoint he has deleted we set the checkin and checkout button to invisible//
                    checkInBtn.setVisibility(View.GONE);
                    checkOutBtn.setVisibility(View.GONE);

                } else {
                    Toasty.error(MainActivity.this, "Error: " + task.getException().getMessage()).show();
                }
            }
        });
    }

}