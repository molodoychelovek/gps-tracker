package com.gpstracker.anton.Pages;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gpstracker.anton.Server.BitmapEditor;
import com.gpstracker.anton.Server.DataServer;
import com.gpstracker.anton.MyProperties;
import com.gpstracker.anton.R;
import com.gpstracker.anton.Server.UploadFiles;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;

public class Registration extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);
        init();
    }


    private void init(){
        imageView = (CircularImageView) findViewById(R.id.imageView);
        refreshcontent = true;
        reloadContent();
    }

    Runnable refresh = null;
    private boolean refreshcontent;

    private void reloadContent(){
        final Handler handler = new Handler();

        if(refreshcontent) {
            refresh = new Runnable() {
                public void run() {
                    changeColor();
                    handler.postDelayed(refresh, 15);
                }
            };
            handler.post(refresh);
        }
    }


    private int MAX_VALUE = 186;
    private int color1 = MAX_VALUE;
    private int color2 = 0;
    private int color3 = 0;

    private void changeColor(){
        int color = Color.argb(255, color1, color2, color3);
        imageView.setBorderColor(color);
        //imageView.setShadowColor(color);
        startColor();
    }

    private int count = 1;
    private void startColor(){
        if(count == 1) {
            color2 = color2 + 1;
            if(color1 == MAX_VALUE && color2 == MAX_VALUE && color3 == 0)
                count = count + 1;
        }
        if(count == 2){
            color1 = color1 - 1;
            if(color1 == 0 && color2 == MAX_VALUE && color3 == 0)
                count = count + 1;
        }
        if(count == 3){
            color3 = color3 + 1;
            if(color1 == 0 && color2 == MAX_VALUE && color3 == MAX_VALUE)
                count = count + 1;
        }
        if(count == 4){
            color2 = color2 - 1;
            if(color1 == 0 && color2 == 0 && color3 == MAX_VALUE)
                count = count + 1;
        }
        if(count == 5){
            color1 = color1 + 1;
            if(color1 == MAX_VALUE && color2 == 0 && color3 == MAX_VALUE)
                count = count + 1;
        }
        if(count == 6){
            color3 = color3 - 1;
            if(color1 == MAX_VALUE && color2 == 0 && color3 == 0)
                count = 1;
        }

    }

    private EditText emailText;
    private EditText passwordText;
    private TextView errorText;

    public void signUpClick(View view) {
        emailText = (EditText) findViewById(R.id.editText2);
        passwordText = (EditText) findViewById(R.id.editText3);
        errorText = (TextView) findViewById(R.id.textError);

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if(!new DataServer().checkExists(email) && !email.equals("") && !password.equals("")) {
            if(picturePath.equals("") || picturePath == null)
                filename = "noimage.jpg";
            else
                new UploadFiles(picturePath, filename);
            new DataServer().addUser(email, password, filename);
            new MyProperties().CreatePropertiesFile(this, email, password, "0");
            refreshcontent = false;
            Intent intent = new Intent(this, Rooms.class);
            startActivity(intent);
            errorText.setVisibility(View.INVISIBLE);
        } else {
            errorText.setVisibility(View.VISIBLE);
        }
    }


    private String picturePath = "";
    private String filename;
    private CircularImageView imageView;
    public static int PICK_IMAGE = 1;

    public void imageClick(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = new BitmapEditor().getResizedBitmap(BitmapFactory.decodeStream(imageStream), 450, 450);
                imageView.setImageBitmap(selectedImage);

                picturePath = getRealPathFromURI(getApplicationContext(), imageUri);
                filename = picturePath.substring(picturePath.lastIndexOf("/")+1);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getApplicationContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
