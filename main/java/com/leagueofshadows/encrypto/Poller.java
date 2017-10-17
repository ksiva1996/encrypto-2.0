package com.leagueofshadows.encrypto;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class Poller extends BroadcastReceiver {


    @Override
    public void onReceive(final Context context, Intent intent) {

        SharedPreferences sp = context.getSharedPreferences(Util.preferences,Context.MODE_PRIVATE);
        final String username = sp.getString(Util.username,null);
        if(username==null)
        {
            return;
        }

        String url = Util.url+"check.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try
                {
                    JSONObject json = new JSONObject(response);
                    int sync = json.getInt(Util.sync);
                    if(sync==1)
                    {
                        try
                        {
                            Db db = new Db(context);
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray(Util.files);
                            for(int j=0;j<array.length();j++)
                            {
                                JSONObject x = array.getJSONObject(j);
                                FilesObject file = new FilesObject(x.getString(Util.fileName),x.getString(Util.fileAddress),0,0,"0",x.getString(Util.databaseID),x.getString(Util.from),0,x.getString(Util.key));
                                db.addFile(file);
                            }
                            db.close();
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        boolean x = Encrypto.getEncrypto().refresh();
                        if(!x) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                            builder.setContentTitle("Encrypto");
                            builder.setContentText("you have files to download click to view details");
                            builder.setSmallIcon(R.mipmap.ic_launcher);
                            Intent i = new Intent(context, Downloader.class);
                            i.putExtra(Util.response, response);
                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(Downloader.class);
                            stackBuilder.addNextIntent(i);
                            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(resultPendingIntent);
                            builder.setAutoCancel(true);
                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(1254, builder.build());
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent intent1 = new Intent(context, Poller.class);
                long scTime = 60*200;//12seconds
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scTime, pendingIntent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Intent intent1 = new Intent(context, Poller.class);
                long scTime = 60*200;//12seconds
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent1, 0);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scTime, pendingIntent);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(Util.username,username);
                return params;
            }
        };
        VolleyHelper.getInstance(context).addToRequestQueue(request);

    }
}
