package com.justice.placelocator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Map;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements MyLocationListener.LocationListenerCallbacks, MainAdapter.ItemClicked {
    ////////0 seconds/////////
    private static final long LOCATION_REFRESH_TIME = 5000;
    ///////0 metre///////////////
    private static final float LOCATION_REFRESH_DISTANCE = 0;

    private static final int LOCATION_PERMISSION = 3;

    private TextView mLocationTxtView;
    private Button mCheckInBtn;
    private Button mCheckOutBtn;
    private LocationManager mLocationManager;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private MainAdapter adapter;
    private RecyclerView recyclerView;

    public static DocumentSnapshot documentSnapshot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        setOnClickListeners();
        networkConnectionIsPresent();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteItem) {
            deleteAllPlaces();
        } else if (item.getItemId() == R.id.logoutItem) {
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllPlaces() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setNegativeButton("cancel", null)
                .setPositiveButton("delete all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAll();
                    }
                }).setBackground(getDrawable(R.drawable.button_bg));
        builder.show();
    }

    private void deleteAll() {
        for (int i = 0; i < adapter.getSnapshots().size(); i++) {
            adapter.getSnapshots().getSnapshot(i).getReference().delete().addOnCompleteListener(null);
        }
    }

    private void setUpLocationManager() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        seeIfGPSisEnabled();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);

    }

    private void seeIfGPSisEnabled() {
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        mLocationTxtView = findViewById(R.id.locationTxtView);
        mCheckInBtn = findViewById(R.id.checkInBtn);
        mCheckOutBtn = findViewById(R.id.checkOutBtn);

    }

    private void setOnClickListeners() {
        mCheckInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInBtnClicked();
            }
        });
        mCheckOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkOutBtnClicked();

            }
        });
    }


    private void checkInBtnClicked() {
        //change visibility of buttons
        mCheckInBtn.setVisibility(View.GONE);
        mCheckOutBtn.setVisibility(View.VISIBLE);
        mLocationTxtView.setText("checked in ,fetching data...");
        setUpLocationManager();
    }

    private final LocationListener mLocationListener = new MyLocationListener(this);


    @Override
    public void setTextView(String text) {
        mLocationTxtView.setText(text);
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

    }

    public void sendLocationDataToDatabase(Map<String, Object> map) {

        firebaseFirestore.collection("all_locations").document(firebaseAuth.getUid()).collection("locations").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                } else {
                    Toasty.success(MainActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();

                }
            }
        });

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
        mCheckInBtn.setVisibility(View.VISIBLE);
        mCheckOutBtn.setVisibility(View.GONE);
        mLocationManager.removeUpdates(mLocationListener);
        mLocationTxtView.setText("checked out");
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION_PERMISSION) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    LOCATION_REFRESH_DISTANCE, mLocationListener);

        }
    }

    @Override
    public void onProviderDisabled() {
        mCheckInBtn.setVisibility(View.VISIBLE);
        mCheckOutBtn.setVisibility(View.GONE);
        mLocationTxtView.setText("checked out");

    }

    @Override
    protected void onStart() {
        super.onStart();


        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }else {
            setUpRecyclerView();
            setSwipeListenerForItems();

        }


    }


    private void setUpRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        Query query = firebaseFirestore.collection("all_locations").document(firebaseAuth.getUid()).collection("locations").orderBy("timeStamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<LocationData> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<LocationData>().setQuery(query,
                new SnapshotParser<LocationData>() {
                    @NonNull
                    @Override
                    public LocationData parseSnapshot(@NonNull DocumentSnapshot snapshot) {

                        LocationData locationData = snapshot.toObject(LocationData.class);
                        locationData.setId(snapshot.getId());
                        return locationData;
                    }
                }).setLifecycleOwner(MainActivity.this).build();


        adapter = new MainAdapter(this, firestoreRecyclerOptions);
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
                    Toasty.success(MainActivity.this, "deletion success", Toasty.LENGTH_SHORT).show();
                } else {
                    Toasty.error(MainActivity.this, "Error: " + task.getException().getMessage(), Toasty.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);

        }

    }

    @Override
    public void itemClickedDocumentSnapshot(DocumentSnapshot document) {
        documentSnapshot = document;
        startActivity(new Intent(this, MapsActivity.class));
    }
}