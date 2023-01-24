package com.example.speedtrap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Login extends AppCompatActivity {
    private String url = "sqlserver information that does not exist";
    private String uid = "standard user";
    private String passw = "sp33dTrapStandardUs3r";
    private Connection con = null;

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
        setContentView(R.layout.activity_login);

        Button loginButton = findViewById(R.id.logbt);
        Button cancelButton = findViewById(R.id.cancelbt);
        EditText uname = findViewById(R.id.unameET);
        EditText pw = findViewById(R.id.pwET);

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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loggedIn = false;
                adminUser = null;
                Intent intent = new Intent(Login.this, MainActivity.class);
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

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uname.getText() == null || pw.getText() == null){
                    if(uname.getText() == null && pw.getText() == null) Toast.makeText(getApplicationContext(),"Please enter a valid login information.",Toast.LENGTH_SHORT).show();
                    else if(pw.getText() == null) Toast.makeText(getApplicationContext(),"Please enter a password",Toast.LENGTH_SHORT).show();
                    else Toast.makeText(getApplicationContext(),"Please enter a valid username.",Toast.LENGTH_SHORT).show();
                }
                else{
                    String user = uname.getText().toString();
                    String psw = pw.getText().toString();
                    //this next line is edited to bypass connecting to a sql server since we don't
                    //have one, if url, user, pw were all true, this code should be 100% functional
                    //but putting in dummy information in for now
                    String result = user;//validateUser(user,psw);
                    //phnNum and driver name would also be set during validation section, will set statitcally here
                    //these values can only be changed via the website in the database
                    //phnNum = "2508631722";
                    //drivername = "Ryan";  //changed to add an activity to change these, should be gotten from database but for demo purposes, changing in app
                    switch(result){
                        case "failure":
                            Toast.makeText(getApplicationContext(),"Could not connect to login server, please try again later.",Toast.LENGTH_LONG).show();
                            break;
                        case "false":{
                            pw.setText("");
                            Toast.makeText(getApplicationContext(),"Password failed, please try again.",Toast.LENGTH_SHORT).show();}
                            break;
                        case "no user":{
                            uname.setText(null);
                            pw.setText(null);
                            Toast.makeText(getApplicationContext(),"Username not found, please try again.",Toast.LENGTH_LONG).show();}
                            break;
                        default: {
                            adminUser = result;
                            loggedIn = true;
                            Intent intent = new Intent(Login.this, settings.class);
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
            }
        });
    }

    public void getConnection() throws SQLException {

        try{
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        catch (Exception e){
            throw new SQLException("ClassNotFoundException: " + e);
        }
        con = DriverManager.getConnection(url,uid,passw);
    }

    public void closeConnection() {
        try{
            if(con!=null) con.close();
            con = null;
        }
        catch (SQLException e){
            Toast.makeText(getApplicationContext(),"SQLException: " + e,Toast.LENGTH_LONG).show();
        }
    }

    public String validateUser(String user, String pw){
        try{
            getConnection();
            String sql = "SELECT username, password, phonenumber, drivername FROM anonlinedatabase WHERE username = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, user);

            ResultSet rst = pstmt.executeQuery();

            if(rst.next()){
                String EnteredPW = rst.getString(2);
                if(EnteredPW.equals(pw)) {
                    phnNum = rst.getString(7);
                    drivername = rst.getString(6);
                    return user;

                }
                else return "false";
            }
            else return "no user";
        }
        catch (SQLException e){
            Toast.makeText(getApplicationContext(),"SQLException: " + e,Toast.LENGTH_LONG).show();
        }
        finally {
            closeConnection();
        }
        return "failure";
    }



}