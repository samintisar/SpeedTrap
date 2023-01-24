package com.example.speedtrap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class settings extends AppCompatActivity {

    public int overZone;
    public int maxkph;
    public String adminUser;
    public boolean loggedIn = false;
    public int starthour;
    public int startmin;
    public int stophour;
    public int stopmin;
    public String phnNum;
    public String drivername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText overzone = findViewById(R.id.zoneOver);
        EditText max = findViewById(R.id.maxSp);
        TimePicker start = findViewById(R.id.startTime);
        TimePicker stop = findViewById(R.id.endTime);
        Button nosave = findViewById(R.id.nosaveBt);
        Button save = findViewById(R.id.saveBt);
        Button nameNum = findViewById(R.id.nameNumBT);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            overZone = extras.getInt("overZone");
            maxkph = extras.getInt("maxkph");
            loggedIn = extras.getBoolean("loggedIn");
            adminUser = extras.getString("adminUser");
            starthour = extras.getInt("starthour");
            startmin = extras.getInt("startmin");
            stophour = extras.getInt("stophour");
            stopmin = extras.getInt("stopmin");
            phnNum = extras.getString("phnNum");
            drivername = extras.getString("drivername");
        }
        try {
//            String temp2 = overZone+", "+maxkph+", "+starthour+":"+startmin+", "+stophour+":"+stopmin;
//            Toast.makeText(getApplicationContext(),temp2,Toast.LENGTH_SHORT).show();
            overzone.setText(Integer.toString(overZone));
            max.setText(Integer.toString(maxkph));
            int h1 = starthour, m1 = startmin, h2 = stophour, m2 = stopmin;
            start.setHour(h1);
            start.setMinute(m1);
            stop.setHour(h2);
            stop.setMinute(m2);
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Exception: " + e,Toast.LENGTH_LONG).show();
        }


        if(loggedIn){//shouldn't be necessary but why not
            nameNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    overZone = Integer.parseInt(overzone.getText().toString());
                    maxkph = Integer.parseInt(max.getText().toString());
                    starthour = start.getHour();
                    startmin = start.getMinute();
                    stophour = stop.getHour();
                    stopmin = stop.getMinute();

                    Intent intent = new Intent(settings.this, addNameNum.class);
                    intent.putExtra("overZone", overZone);
                    intent.putExtra("maxkph", maxkph);
                    intent.putExtra("loggedIn", loggedIn);
                    intent.putExtra("adminUser", adminUser);
                    intent.putExtra("starthour", starthour);
                    intent.putExtra("startmin", startmin);
                    intent.putExtra("stophour", stophour);
                    intent.putExtra("stopmin", stopmin);
                    intent.putExtra("phnNum", phnNum);
                    intent.putExtra("drivername", drivername);
                    startActivity(intent);
                }
            });
            nosave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loggedIn = false;
                    adminUser = null;
                    Intent intent = new Intent(settings.this, MainActivity.class);
                    intent.putExtra("overZone", overZone);
                    intent.putExtra("maxkph", maxkph);
                    intent.putExtra("loggedIn", loggedIn);
                    intent.putExtra("adminUser", adminUser);
                    intent.putExtra("curfewStart", starthour);
                    intent.putExtra("curfewStop", startmin);
                    intent.putExtra("curfewStart", stophour);
                    intent.putExtra("curfewStop", stopmin);
                    intent.putExtra("phnNum", phnNum);
                    intent.putExtra("drivername", drivername);
                    startActivity(intent);
                }
            });

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //change values
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    overZone = Integer.parseInt(overzone.getText().toString());
                    maxkph = Integer.parseInt(max.getText().toString());
                    starthour = start.getHour();
                    startmin = start.getMinute();
                    stophour = stop.getHour();
                    stopmin = stop.getMinute();

                    String filename = "data.txt";
                    String fileContents = overZone + "," + maxkph + "," + starthour + "," + startmin + "," + stophour + "," + stopmin + "," + drivername + "," + phnNum;
                    FileOutputStream outputStream;
                    try{
                        outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                        outputStream.write(fileContents.getBytes());
                        outputStream.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                            loggedIn = false;
                    adminUser = null;
                    Intent intent = new Intent(settings.this, MainActivity.class);
                    intent.putExtra("overZone", overZone);
                    intent.putExtra("maxkph", maxkph);
                    intent.putExtra("loggedIn", loggedIn);
                    intent.putExtra("adminUser", adminUser);
                    intent.putExtra("starthour", starthour);
                    intent.putExtra("startmin", startmin);
                    intent.putExtra("stophour", stophour);
                    intent.putExtra("stopmin", stopmin);
                    intent.putExtra("phnNum", phnNum);
                    intent.putExtra("drivername", drivername);
                    startActivity(intent);
                }
            });

        }
        else{
            Intent intent = new Intent(settings.this, MainActivity.class);
            adminUser = null;
            loggedIn = false;
            intent.putExtra("overZone", overZone);
            intent.putExtra("maxkph", maxkph);
            intent.putExtra("loggedIn", loggedIn);
            intent.putExtra("adminUser", adminUser);
            intent.putExtra("starthour", starthour);
            intent.putExtra("startmin", startmin);
            intent.putExtra("stophour", stophour);
            intent.putExtra("stopmin", stopmin);
            intent.putExtra("phnNum", phnNum);
            intent.putExtra("drivername", drivername);
            startActivity(intent);
        }
    }
}