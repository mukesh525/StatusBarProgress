package in.vmc.downloaddemo;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private NotificationManager mNotifyManager;
    private android.support.v4.app.NotificationCompat.Builder mBuilder;
    int id = 1;
    private static String file_url = "http://api.androidhive.info/progressdialog/hive.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button b1 = (Button) findViewById(R.id.notificationButton);
        b1.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(MainActivity.this);
                mBuilder.setContentTitle("Download")
                        .setOngoing(true)
                        .setContentText("Download in progress")
                        .setContentInfo("0%")
                        .setSmallIcon(android.R.drawable.stat_sys_download);


                //new Downloader().execute();
                new Downloader().execute(file_url);
            }
        });
    }

    private class Downloader extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Displays the progress bar for the first time.
            mBuilder.setProgress(100, 0, false);
            mNotifyManager.notify(id, mBuilder.build());
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            mBuilder.setContentInfo(values[0] + "%");
            mBuilder.setProgress(100, values[0], false);
            mNotifyManager.notify(id, mBuilder.build());
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(String... params) {
            int i;
//            for (i = 0; i <= 100; i += 5) {
//                // Sets the progress indicator completion percentage
//                publishProgress(Math.min(i, 100));
//                try {
//                    // Sleep for 5 seconds
//                    Thread.sleep(2 * 1000);
//                } catch (InterruptedException e) {
//                    Log.d("TAG", "sleep failure");
//                }
//            }

            int count;
            try {
                URL url = new URL(params[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream("/sdcard/downloadedfile.jpg");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress((int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File("/sdcard/downloadedfile.jpg"); // set your audio path
            intent.setDataAndType(Uri.fromFile(file), "image/*");
            PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
            mBuilder.setContentText("Download complete");
            // Removes the progress bar
            mBuilder.setOngoing(false);
            mBuilder.setContentIntent(pIntent);
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done);
            mBuilder.setProgress(0, 0, false);

            mNotifyManager.notify(id, mBuilder.build());
        }
    }
}

