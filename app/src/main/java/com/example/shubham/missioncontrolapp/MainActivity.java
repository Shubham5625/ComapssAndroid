package com.example.shubham.missioncontrolapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private ImageView image;
    // record the compass picture angle turned
    private float currentDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    // co-ordinates
    TextView tvHeading;
    //seekbar
    private SeekBar seekBar;
    // distance
    private TextView textView;
    // latitude
    private  TextView lat;
    // longitude
    private  TextView longi;
    // new latitude
    private TextView latNew;
    // new longitude
    private  TextView longNew;
    // date and time
    String dateandtime;
    // degree
    float degree;
    // temp variables for latitude and longitude
    double latitude;
    double longitute;
    // temp for seekbar progress
    int seekbarProgress;
    // initiate location manager
    LocationManager locationManager;
    String provider;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 0;
    // initiate firebase
    FirebaseDatabase database;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // our compass image
        image = (ImageView) findViewById(R.id.compassimage);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.heading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        textView = (TextView) findViewById(R.id.textView);
        lat = (TextView) findViewById(R.id.lat);
        longi = (TextView) findViewById(R.id.longi);
        latNew = (TextView) findViewById(R.id.latNew);
        longNew = (TextView) findViewById(R.id.longNew);
        // Distance covered using seekbar
        textView.setText("Covered: " + seekBar.getProgress() + "/" + seekBar.getMax() + " m");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                seekbarProgress = seekBar.getProgress();
                textView.setText("Covered: " + seekbarProgress + "/" + seekBar.getMax() + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Covered: " + progress + "/" + seekBar.getMax() + " m");
            }
        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION))
            {
            } else
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_ACCESS_COARSE_LOCATION);
            }
            return;
        }
        // Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {

            Toast.makeText(getApplicationContext(), "Yes",
                    Toast.LENGTH_LONG).show();

            latitude = location.getLatitude();
            longitute = location.getLongitude();
            Toast.makeText(getApplicationContext(), "" + latitude + " " + longitute,
                    Toast.LENGTH_LONG).show();
            Log.i("Location", "Location Achieved");
        } else {
            Toast.makeText(getApplicationContext(), "No",
                    Toast.LENGTH_LONG).show();
            Log.i("Location", "Something is Going wrong");
        }
    }
    @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            switch (requestCode) {
                case PERMISSION_ACCESS_COARSE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        // permission was granted
                        Toast.makeText(getApplicationContext(), "yeeeeeeee",
                                Toast.LENGTH_LONG).show();

                    } else {
                        // permission denied
                    }
                    return;
                }
            }
        }
    // on click event for location
        public void send(View v) {

            double latitudeNew= latitude + (seekbarProgress* 0.001 * Math.cos(Math.toRadians(degree)));
            double longititudeNew=longitute + (seekbarProgress* 0.001 * Math.sin(Math.toRadians(degree)));

            lat.setText(""+latitude);
            longi.setText(""+longitute);
            latNew.setText(""+latitudeNew);
            longNew.setText(""+longititudeNew);

            Toast.makeText(getApplicationContext(), ""+latitudeNew+" "+longititudeNew,
                    Toast.LENGTH_LONG).show();


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd_HH:mm:ss");
            dateandtime = sdf.format(new Date());
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference(dateandtime);
            myRef.setValue(latitudeNew+","+longititudeNew);
        }
    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        degree = Math.round(event.values[0]);
        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");
        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
    @Override
    public void onLocationChanged(Location location) {

        double latitude = location.getLatitude();
        double longitute=location.getLongitude();
        Toast.makeText(getApplicationContext()," "+latitude+ " "+ longitute,
                Toast.LENGTH_LONG).show();
        Log.i("location"," "+latitude+" "+longitute);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

   @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
