package com.gpstracker.anton.Pages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gpstracker.anton.Server.DataServer;
import com.gpstracker.anton.MyProperties;
import com.gpstracker.anton.R;
import com.gpstracker.anton.Server.UploadFiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class Login extends AppCompatActivity {
    EditText emailText = null;
    EditText passwordText = null;
    TextView textError = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        emailText = (EditText) findViewById(R.id.editText2);
        passwordText = (EditText) findViewById(R.id.editText3);
        textError = (TextView) findViewById(R.id.textError);

        HashMap<String, String> prop = new MyProperties().getProp(this);
        emailText.setText(prop.get("email"));
        passwordText.setText(prop.get("pass"));

        checkPermissions();
    }

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }


    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    public void signInClick(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(new DataServer().checkUser(email, password)) {
            textError.setVisibility(View.INVISIBLE);
            new MyProperties().CreatePropertiesFile(this, email, password, "0");
            Intent intent = new Intent(this, Rooms.class);
            startActivity(intent);
        } else {
            textError.setVisibility(View.VISIBLE);
        }
    }


    public void signUpClick(View view) {
        Intent intent = new Intent(this, Registration.class);
        startActivity(intent);
    }

}
