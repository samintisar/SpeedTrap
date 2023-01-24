package com.example.speedtrap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class addNameNum extends AppCompatActivity {

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
        setContentView(R.layout.activity_add_name_num);

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

        Button back = findViewById(R.id.backBt);
        EditText name = findViewById(R.id.driveNameET);
        EditText phone = findViewById(R.id.phnNumET);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phnNum = phone.getText().toString();
                drivername = name.getText().toString();

                Intent intent = new Intent(addNameNum.this, settings.class);
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
}