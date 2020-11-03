package com.gpstracker.anton.Server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


public class DataServer {
    public void addUser(String email, String password, String img) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/adduser.php?" +
                "name=" + null +
                "&email=" + email +
                "&password=" + password +
                "&room=" + 0 +
                "&latitude=" + 0 +
                "&longitude=" + 0 +
                "&img=http://142.93.139.45/gpstracker/api/v1/users/images/" + img;
        try {
            new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addRoom(String name, String user, String password) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/addroom.php?" +
                "name=" + name +
                "&users=" + user +
                "&password='" + password + "'";
        try {
            new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateUserPos(String email, String latitude, String longitude) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/updatepos.php?" +
                "email='" + email +
                "'&latitude='" + latitude +
                "'&longitude='" + longitude + "'";

        try {
            new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public boolean checkUser(String email, String password) {
        try {
            JSONArray json = new JSONArray(getUserJSON(email));
            System.out.println("CHECK: " + json);

            for (int i = 0; i < json.length(); i++) {
                JSONObject e = json.getJSONObject(i);
                if (e.getString("email").equals(email) && e.getString("password").equals(password)) {
                    return true;
                }
            }
        } catch (Exception e) { }
        return false;
    }

    public boolean checkExists(String email) {
        try {
            JSONArray json = new JSONArray(getUserJSON(email));
            for (int i = 0; i < json.length(); i++) {
                JSONObject e = json.getJSONObject(i);
                if (e.getString("email").equals(email)) {
                    return true;
                }
            }
        } catch (Exception e) { }
        return false;
    }

    public boolean updateRoom(String email, String allusers, String id, boolean leave) {
        ArrayList<String> list = new ArrayList<String>(Arrays.asList(allusers.split(" ")));

        String newList = "";
        String url = "";

        if (leave) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(email)) {
                    list.remove(i);
                }
            }
        } else {
            list.add(email);
        }

        if (list.size() == 0) {
            url = "http://142.93.139.45/gpstracker/api/v1/users/deleteroom.php?" +
                    "id=" + id;
            try {
            new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

            return true;
        }

        for (String s : list)
            newList += s + " ";

        System.out.println(id);

        try {
            url = "http://142.93.139.45/gpstracker/api/v1/users/updateroom.php?" +
                    "users='" + URLEncoder.encode(newList, "UTF-8") + "'" +
                    "&id=" + id;
            new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public String getUsersJSON() {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/getJSON.php";
       try {
            return new ServerConnect().execute(url, "true").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String updateUserRoom(String email, String room) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/updateUserRoom.php?" +
                "email='" + email + "'" +
                "&room=" + room;
       try {
            return new ServerConnect().execute(url, "false").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getUserJSON(String email) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/getUser.php?email='" + email + "'";
       try {
            return new ServerConnect().execute(url, "true").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRoomsJSON() {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/getRooms.php";
       try {
            return new ServerConnect().execute(url, "true").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRoomJSON(String id) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/getRoom.php?id=" + id;
       try {
            return new ServerConnect().execute(url, "true").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNewRoomJSON(String user) {
        String url = "http://142.93.139.45/gpstracker/api/v1/users/getnewroom.php?user='" + user + "'";
        try {
            return new ServerConnect().execute(url, "true").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, String> getNewRoom(String email) {
        HashMap<String, String> hm = new HashMap<>();
        try {
            JSONArray json = new JSONArray(getNewRoomJSON(email));

            for (int i = 0; i < json.length(); i++) {
                JSONObject e = json.getJSONObject(i);
                hm.put("id", e.getString("id"));
                hm.put("name", e.getString("name"));
                hm.put("users", e.getString("users"));
                hm.put("password", e.getString("password"));
            }
        } catch (Exception e) { }
        return hm;
    }

    public boolean succefull = false;
    public ArrayList<HashMap<String, String>> getRooms() {
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        try {
            JSONArray json = new JSONArray(getRoomsJSON());
            for (int i = 0; i < json.length(); i++) {
                HashMap<String, String> hm = new HashMap<>();

                JSONObject e = json.getJSONObject(i);
                hm.put("id", e.getString("id"));
                hm.put("name", e.getString("name"));
                hm.put("users", e.getString("users"));
                hm.put("password", e.getString("password"));

                list.add(hm);
                succefull = true;
            }
        } catch (Exception e) {
            succefull = false;
            System.out.println("Rooms lenght 0");
        }
        return list;
    }

    public HashMap<String, String> getRoom(String id) {
        HashMap<String, String> hm = new HashMap<>();
        try {
            JSONArray json = new JSONArray(getRoomJSON(id));

            for (int i = 0; i < json.length(); i++) {
                JSONObject e = json.getJSONObject(i);
                hm.put("id", e.getString("id"));
                hm.put("name", e.getString("name"));
                hm.put("users", e.getString("users"));
                hm.put("password", e.getString("password"));
            }
        } catch (Exception e) { }
        return hm;
    }

    public HashMap<String, String> getUser(String email) {
        HashMap<String, String> list = new HashMap<>();
        try {
            JSONArray json = new JSONArray(getUserJSON(email));
            for (int i = 0; i < json.length(); i++) {
                HashMap<String, String> hm = new HashMap<>();

                JSONObject e = json.getJSONObject(i);
                hm.put("id", e.getString("id"));
                hm.put("name", e.getString("name"));
                hm.put("email", e.getString("email"));
                hm.put("password", e.getString("password"));
                hm.put("img", e.getString("img"));
                hm.put("latitude", e.getString("latitude"));
                hm.put("longitude", e.getString("longitude"));
                hm.put("room", e.getString("room"));

                return hm;
            }
        } catch (Exception e) { }
        return list;
    }

    private class ServerConnect extends AsyncTask<String, Integer, String>{
        @Override
        protected String doInBackground(String... strings) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(strings[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return responseString;
        }
    }

    public Bitmap loadImg(String url){
        try {
            return new getImage().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class getImage extends AsyncTask<String, Integer, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                java.net.URL url = new java.net.URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
