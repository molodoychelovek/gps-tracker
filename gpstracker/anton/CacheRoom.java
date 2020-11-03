package com.gpstracker.anton;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.gpstracker.anton.Server.BitmapEditor;
import com.gpstracker.anton.Server.DataServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CacheRoom {
    public void getCache(String room){

        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "/GPSTracker/");
        System.out.println(folder.getAbsolutePath());
        if (!folder.exists()) {
            System.out.println("folder not exists");
            folder.mkdirs();
        }

        HashMap<String , String> hm = new DataServer().getRoom(room);

        List<String> users = Arrays.asList(hm.get("users").split(" "));
        users.removeAll(Collections.singleton(null));

        for(int i = 0; i < users.size(); i++) {
            System.out.println("CACHE ROOM: USER " + users.get(i));
            HashMap<String, String> user = new DataServer().getUser(users.get(i));

            String imgname = user.get("img").replace("http://142.93.139.45/gpstracker/api/v1/users/images/", "");

            File f = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "GPSTracker/" + imgname);

            if (!f.exists() || f.getAbsolutePath().contains("noimage.jpg")) {
                System.out.println(user.get("ADDED NEW IMAGE 1"));
                Bitmap image = new BitmapEditor().getBitmapFromURL(user.get("img"));

                try {
                    System.out.println(user.get("ADDED NEW IMAGE 2"));
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

                    f.createNewFile();
                    FileOutputStream fo = new FileOutputStream(f);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean existsImg(String img, String room){
        String imgname = img.replace("http://142.93.139.45/gpstracker/api/v1/users/images/", "");

        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "GPSTracker/" + imgname);

        if(f.exists())
            return true;

        return false;
    }
}
