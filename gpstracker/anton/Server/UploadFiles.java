package com.gpstracker.anton.Server;

import android.os.AsyncTask;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class UploadFiles {
    private String sourceFileUri = "";
    private String pathname = "";

    public UploadFiles(final String sourceFileUri, String pathname){
        this.sourceFileUri = sourceFileUri;
        this.pathname = pathname;
        new UploadFileAsync().execute();
    }

    private class UploadFileAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            upload(sourceFileUri, pathname);
            return "";
        }
    }

    public void upload(String path, String pathname) {
        System.out.println(pathname);
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            JSch jsch = new JSch();

            Session session = jsch.getSession("root", "142.93.139.45");
            session.setPassword("YEh63f");
            session.setConfig(config);

            session.connect();

            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            sftpChannel.put(path, "/var/www/html/gpstracker/api/v1/users/images/");
            System.out.println("+");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}