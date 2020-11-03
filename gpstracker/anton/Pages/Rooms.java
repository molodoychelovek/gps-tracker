package com.gpstracker.anton.Pages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.gpstracker.anton.CacheRoom;
import com.gpstracker.anton.OnClearFromRecentService;
import com.gpstracker.anton.Server.DataServer;
import com.gpstracker.anton.MyProperties;
import com.gpstracker.anton.R;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Rooms extends AppCompatActivity {
    private LinearLayout list;
    private EditText nameNewRoom;
    private EditText passText;
    private HashMap<String, String> prop;
    private static Context context;
    private boolean refreshContent;

    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roomtable);
        this.context = getApplicationContext();

        list = (LinearLayout) findViewById(R.id.listItem);
        list.setOrientation(LinearLayout.VERTICAL);
        nameNewRoom = (EditText) findViewById(R.id.editText);
        passText = (EditText) findViewById(R.id.editText5);
        prop = new MyProperties().getProp(this);

        refreshContent = true;
        reloadContent();
    }

    Runnable refresh = null;
    private void reloadContent(){
        final DataServer dataServer = new DataServer();
        final Handler handler = new Handler();

        refresh = new Runnable() {
            public void run() {
                if(refreshContent) {
                    ArrayList listRooms = dataServer.getRooms();

                    if (dataServer.succefull) {
                        System.out.println("+");
                        createButton(listRooms);
                    }

                    handler.postDelayed(refresh, 1000);
                }
            }
        };
        handler.post(refresh);
    }

    public void createButton(View view) {

        String name = nameNewRoom.getText().toString();
        String pass = passText.getText().toString();

        new DataServer().addRoom(name, prop.get("email"), pass);
        HashMap<String, String> data = new DataServer().getNewRoom(prop.get("email"));
        goToMaps(data, true);
    }

    public void createButton(ArrayList listRooms){
        list.removeAllViews();

        for(int i = 0; i < listRooms.size(); i++) {
            final HashMap<String, String> data = (HashMap<String, String>) listRooms.get(i);
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText inputText = new EditText(this);

            Button button = new Button(this);
            TextView textView = new TextView(this);
            TextView textId = new TextView(this);
            Space space = new Space(this);

            final LinearLayout column = new LinearLayout(this);
            LinearLayout row = new LinearLayout(this);
            LinearLayout spaceLayout = new LinearLayout(this);
            LinearLayout spaceLayout2 = new LinearLayout(this);

            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setGravity(Gravity.FILL);
            // row.setPadding(50, 15, 50,0);

            space.setLayoutParams(new LinearLayout.LayoutParams(420, 50));
            spaceLayout.setPadding(0, 2, 0, 0);
            spaceLayout2.setPadding(0, 2, 0, 0);

            button.setText("JOIN");
            button.setTextSize(13);
            button.setTextColor(Color.WHITE);
            button.setBackground(getResources().getDrawable(R.drawable.back_text_blue));
            button.setLayoutParams(new LinearLayout.LayoutParams(200, 110));
            button.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    clickRoom(data, alert, inputText);
                }
            });

            textView.setTextColor(Color.BLACK);
            textView.setText(data.get("name"));
            textView.setLayoutParams(new LinearLayout.LayoutParams(250, 150));
            textView.setTextSize(18);

            textId.setTextColor(Color.DKGRAY);
            textId.setText(data.get("id"));
            textId.setLayoutParams(new LinearLayout.LayoutParams(100, 150));
            textId.setTextSize(16);

            column.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clickRoom(data, alert, inputText);
                }
            });

            column.setOrientation(LinearLayout.HORIZONTAL);
            column.setMinimumHeight(100);
            column.setPadding(50, 35, 50, 0);

            column.addView(textId);
            column.addView(textView);
            column.addView(space);
            column.addView(button);


            row.setBackground(getResources().getDrawable(R.drawable.border_row));
            row.setBackgroundColor(Color.parseColor("#f2f2f2"));
            row.addView(column);

            list.addView(spaceLayout);
            list.addView(row);
            list.addView(spaceLayout2);
        }
    }

    private void clickRoom(final HashMap<String, String> data, AlertDialog.Builder alert, final EditText inputText){
        if (!data.get("password").equals("")) {
            alert.setMessage("Enter password");
            alert.setTitle("This room has password");
            alert.setView(inputText);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String pass = inputText.getText().toString();

                    if (pass.equals(data.get("password"))) {
                        goToMaps(data, false);
                    }
                }
            });
            alert.show();
        } else {
            goToMaps(data, false);
        }
    }

    private void goToMaps(final HashMap<String, String> data, final boolean newRoom){
        final ProgressDialog pd = new ProgressDialog(Rooms.this);
        pd.setMessage("Loading");
        pd.show();

        new Thread(new Runnable() {
            public void run() {
                refreshContent = false;

                HashMap<String, String> user = new DataServer().getUser(prop.get("email"));

                if(!newRoom) {
                    new DataServer().updateRoom(prop.get("email"), data.get("users"), data.get("id"), false);
                }

                new DataServer().updateUserRoom(prop.get("email"), data.get("id"));
                new CacheRoom().getCache(data.get("id"));

                Intent intent = new Intent(context, MapsActivity.class);
                startActivity(intent);
                pd.cancel();
            }
        }).start();
    }
}
