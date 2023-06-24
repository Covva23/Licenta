package com.example.licenta;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Locale;

public class Activitatea extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final String PERMISSION_GRANTED_KEY = "permission_granted";
    private static final long TIMER_DURATION = 6 * 60 * 60 * 1000;
    private static final long MOVEMENT_THRESHOLD = 15000;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1 ;

    private MapView mapView;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SharedPreferences sharedPreferences;
    private CountDownTimer timer;
    private TextView timerTextView;
    private TextView locationTextView;
    private TextView activeTimeTextView;
    private TextView backToMainTextView;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastMovementTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activitatea);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        timerTextView = findViewById(R.id.timerTextView);
        locationTextView = findViewById(R.id.locationTextView);
        activeTimeTextView = findViewById(R.id.activeTimeTextView);
        backToMainTextView = findViewById(R.id.backToMainTextView);

        timer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / (60 * 60 * 1000);
                long minutes = (millisUntilFinished % (60 * 60 * 1000)) / (60 * 1000);
                long seconds = (millisUntilFinished % (60 * 1000)) / 1000;

                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                timerTextView.setText(time);
            }

            @Override
            public void onFinish() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Query query = db.collection("Utilizatori").whereEqualTo("uid", uid);

                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                String phoneNumber = querySnapshot.getDocuments().get(0).getString("telefonContact");
                                String message = "Persoana pentru care sunteti atribuit ca si contact trebuie verificata!";

                                if (ContextCompat.checkSelfPermission(Activitatea.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                                    // Permission granted, send SMS
                                    sendSMS(phoneNumber, message);
                                } else {
                                    // Permission not granted, request it
                                    ActivityCompat.requestPermissions(Activitatea.this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
                                }
                            }
                        }
                    });
                }
            }
        };

        backToMainTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Activitatea.this, PaginaPrincipala.class);
                startActivity(intent);
                finish();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lastMovementTime = System.currentTimeMillis();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        boolean permissionGranted = sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);
        if (!permissionGranted) {
            checkLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        stopTimerService();
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        boolean permissionGranted = sharedPreferences.getBoolean(PERMISSION_GRANTED_KEY, false);
        if (permissionGranted || isLocationPermissionGranted()) {
            showLastKnownLocation();
        } else {
            requestLocationPermission();
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void savePermissionStatus(boolean granted) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PERMISSION_GRANTED_KEY, granted);
        editor.apply();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            savePermissionStatus(true);
            showLastKnownLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savePermissionStatus(true);
                showLastKnownLocation();
            } else {
                savePermissionStatus(false);
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void showLastKnownLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && googleMap != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            String coordinates = "Latitudine: " + latitude + "\nLongitudine: " + longitude;
                            locationTextView.setText(coordinates);

                            LatLng latLng = new LatLng(latitude, longitude);
                            googleMap.addMarker(new MarkerOptions().position(latLng));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
                        }
                    }
                });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
            if (acceleration > 10.0) {
                resetTimer();
            } else if (System.currentTimeMillis() - lastMovementTime > MOVEMENT_THRESHOLD) {
                showLastKnownLocation();
                long currentTime = System.currentTimeMillis();
                long activeTime = currentTime - lastMovementTime;
                long seconds = (activeTime / 1000) % 60;
                long minutes = (activeTime / (1000 * 60)) % 60;
                long hours = (activeTime / (1000 * 60 * 60)) % 24;
                String activeTimeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                activeTimeTextView.setText(activeTimeString);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void resetTimer() {
        timer.cancel();
        lastMovementTime = System.currentTimeMillis();
        timer.start();
    }

    private void startTimerService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopTimerService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        stopService(serviceIntent);
    }


    public void PermissonSMS(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String uid = currentUser.getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Query query = db.collection("Utilizatori").whereEqualTo("uid", uid);

                    query.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                String phoneNumber = querySnapshot.getDocuments().get(0).getString("telefonContact");
                                String message = "Persoana pentru care sunteti atribuit ca si contact trebuie verificata!";
                                sendSMS(phoneNumber, message);
                            }
                        }
                    });
                }
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS a fost trimis.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la trimiterea SMS.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
