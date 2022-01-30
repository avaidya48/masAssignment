package com.example.mas_new;

import static android.os.SystemClock.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btnLogOut;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogOut = findViewById(R.id.btnLogout);
        mAuth = FirebaseAuth.getInstance();

        btnLogOut.setOnClickListener(view ->{
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, 1);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    public void sendGPS(View view) {
        EditText text = (EditText) findViewById(R.id.editTextPhone);
        String phone = PhoneNumberUtils.formatNumber(text.getText().toString());

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(validateNumber(phone)){

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_DENIED) {
                    String[] permissions = {Manifest.permission.SEND_SMS};
                    requestPermissions(permissions, 1);
                }
            }

            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

            } else {
                List<String> providers = lm.getProviders(true);
                Location locationGPS = null;
                for (String provider : providers) {
                    Location l = lm.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (locationGPS == null || l.getAccuracy() < locationGPS.getAccuracy()) {
                        // Found best last known location: %s", l);
                        locationGPS = l;
                    }
                }
                if (locationGPS != null) {
                    double lat = locationGPS.getLatitude();
                    double longi = locationGPS.getLongitude();
                    String latitude = String.valueOf(lat);
                    String longitude = String.valueOf(longi);
                    try {
                        SmsManager smsManager = SmsManager.getDefault();
                        StringBuffer smsBody = new StringBuffer();
                        smsBody.append(latitude);
                        smsBody.append(",");
                        smsBody.append(longitude);
                        Toast.makeText(getApplicationContext(), "Sending GPS Coordinates: "+smsBody.toString(), Toast.LENGTH_LONG).show();
                        PendingIntent sentPI;
                        String SENT = "SMS_SENT";
                        sentPI = PendingIntent.getBroadcast(this, 0,new Intent(SENT), 0);
                        smsManager.sendTextMessage(phone, null, smsBody.toString(), sentPI, null);
                        //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
                }
            }
        }else{
            Toast.makeText(this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
        }




    }

    private static boolean validateNumber(String mobNumber)
    {

    //validates phone numbers having 10 digits (9998887776)
            if (mobNumber.matches("\\d{10}"))
                return true;
    //validates phone numbers having digits, -, . or spaces
            else if (mobNumber.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}"))
                return true;
            else if (mobNumber.matches("\\d{4}[-\\.\\s]\\d{3}[-\\.\\s]\\d{3}"))
                return true;
    //validates phone numbers having digits and extension (length 3 to 5)
            else if (mobNumber.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}"))
                return true;
    //validates phone numbers having digits and area code in braces
            else if (mobNumber.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}"))
                return true;
            else if (mobNumber.matches("\\(\\d{5}\\)-\\d{3}-\\d{3}"))
                return true;
            else if (mobNumber.matches("\\(\\d{4}\\)-\\d{3}-\\d{3}"))
                return true;
    //return false if any of the input matches is not found
            else
                return false;
    }

}
