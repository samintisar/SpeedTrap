package com.example.speedtrap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import info.pavie.basicosmparser.controller.OSMParser;
import info.pavie.basicosmparser.model.Element;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0 ;

    public int currSpeed;//already got, updates continuously
    public int overZone = 5;//changes with settings (5 is a default setting if no changes were made)
    public int maxkph = 100;//same as the 5, just a default value of 100
    public int zoneLimit;//needs to be loaded locally
    public String drivername;//this one will be dificult to dynamically populate, will set as "Ryan" for demo purposes
    public String adminUser;//not needed in this activity, used in login and settings
    public boolean loggedIn = false;//not needed in this activity, used in login and settings
    public int starthour;//self explanitory, curfew times
    public int startmin;
    public int stophour;
    public int stopmin;
    private long lastMsgTime = 0;
    private long lastRead = 0;
    public String phnNum;
    public String msg;
    double lat;
    double longt;
    String getSpeedLimit = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        TextView user = findViewById(R.id.useridTV);
        TextView speedZone = findViewById(R.id.speedzoneTV);
        TextView maxSpeed = findViewById(R.id.maxspeedTV);
        TextView curfewTime = findViewById(R.id.curfewTV);
        Button logButton = findViewById(R.id.loginButton);

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
        String file = "data.txt";

        try{
            FileInputStream fis = openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buff = new BufferedReader(isr);
            String temp;
            String[] data;
            if((temp = buff.readLine()) != null){
                data = temp.split(",");
                overZone = Integer.parseInt(data[0]);
                maxkph = Integer.parseInt(data[1]);
                starthour = Integer.parseInt(data[2]);
                startmin = Integer.parseInt(data[3]);
                stophour = Integer.parseInt(data[4]);
                stopmin = Integer.parseInt(data[5]);
                drivername = data[6];
                phnNum = data[7];
            }

            user.setText("Current Driver: "+ drivername);
            speedZone.setText(zoneLimit+" km/h");
            maxSpeed.setText(maxkph+" km/h");
            String times = String.format("%d:%02d - %d:%02d", starthour,startmin,stophour,stopmin);
            curfewTime.setText(times);



        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        LocationManager lm =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Criteria criteria = new Criteria();
        Location location = lm.getLastKnownLocation(lm.getBestProvider(criteria, false));
        this.onLocationChanged(location);
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Login.class);
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

    @Override
    public void onLocationChanged(Location loc) {
        long currTime = System.currentTimeMillis()/1000l;
        try {

            if(lastRead + 5 < currTime){
                lastRead = currTime;
                lat = loc.getLatitude();;//49.883581;//
                longt = loc.getLongitude();;//-119.488446;//
                double latPlus = lat + 0.00004;
                double latMinus = lat - 0.00004;
                double longPlus = longt + 0.000075;
                double longMinus = longt - 0.000075;
                String urlString = "https://www.overpass-api.de/api/xapi?*[maxspeed=*][bbox="+longMinus+","+latMinus+","+longPlus+","+latPlus+"]";

//                final Map<String, Element> resultList = new HashMap<>();

                final String[] result = {""};
                try{
                  Thread gfgThread = new Thread(new Runnable() {
                      @Override
                      public void run() {
                        try {
                            URL url = new URL(urlString);
                            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.connect();
                            File xmlFile = new File(getFilesDir() + "xmlFile.xml");
                            FileOutputStream fileOutputStream = new FileOutputStream(xmlFile);
                            InputStream inputStream = urlConnection.getInputStream();
                            int fileSize = urlConnection.getContentLength();
                            int downSize = 0;
                            byte[] buff = new byte[1024];
                            int buffLength = 0;
                            while ((buffLength = inputStream.read(buff)) > 0) {
                                fileOutputStream.write(buff, 0, buffLength);
                                downSize += buffLength;
                            }
                            fileOutputStream.close();
                            OSMParser p = new OSMParser();
                            Map<String, Element> resultList = p.parse(xmlFile);
                            String[] tempArr = new String[]{};
                            for(String key : resultList.keySet()){
                                String temp = String.valueOf(resultList.get(key));
                                if(temp.contains("maxspeed")){
                                    tempArr = temp.split(" ");
                                }
                            }
                            for(String val : tempArr){
                                if(val.contains("maxspeed")){
                                    String[] arr = val.split("=");
                                    arr = arr[1].split(",");
                                    getSpeedLimit = arr[0];
                                }
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                      }
                  });gfgThread.start();

                  zoneLimit = Integer.parseInt(getSpeedLimit);

                  TextView speedZone = findViewById(R.id.speedzoneTV);
                  speedZone.setText(String.valueOf(zoneLimit) + " km/h");


                } catch (NetworkOnMainThreadException e){
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //curfew checking

            }
            TextView currentSpeed = findViewById(R.id.currspeedTV);
            if (loc == null) currentSpeed.setText("00 km/h");
            else {
                currSpeed = (int) ((loc.getSpeed() * 3.600));
                currentSpeed.setText(currSpeed + " km/h");
                if (currSpeed > maxkph && currSpeed > 10 && zoneLimit > 0) {
                    if(currTime > lastMsgTime + 300){//limit text messages to once every 5 min
                        msg = "The Driver " + drivername + " is exceeding your MAX SPEED limit. " + Calendar.getInstance().getTime();
                        lastMsgTime = currTime;
                        sendMessage();
                    }
                }

                if (currSpeed + overZone > zoneLimit && currSpeed > 10 && zoneLimit > 0) {
                    if(currTime > lastMsgTime + 300){//limit text messages to once every 5 min
                        msg = "The Driver " + drivername + " is exceeding your set zone limits. " + Calendar.getInstance().getTime();
                        lastMsgTime = currTime;
                        sendMessage();
                    }
                }
                if(currSpeed > 20 && zoneLimit > 0){
                    Calendar currentTime = Calendar.getInstance();
                    int hours = currentTime.get(Calendar.HOUR_OF_DAY);
                    int mins = currentTime.get(Calendar.MINUTE);
                    if((hours > stophour && mins > stopmin) || (hours < starthour && mins < startmin)){
                        if(currTime > lastMsgTime + 300){//limit text messages to once every 5 min
                            msg = "The Driver " + drivername + " is driving outside your set driving times. " + Calendar.getInstance().getTime();
                            lastMsgTime = currTime;
                            sendMessage();
                        }
                    }
                }

            }
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Exception occured: " + e,Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onProviderDisabled(String provider) {


    }

    protected void sendMessage(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)){

            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int request, String perm[], int[] granted) {
        super.onRequestPermissionsResult(request, perm, granted);
        switch (request) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (granted.length > 0 && granted[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phnNum, null, msg, null, null);

                } else return;
            }
        }
    }
}