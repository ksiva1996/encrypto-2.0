package com.leagueofshadows.encrypto;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity implements Refresh {

    ListView listView;
    CustomAdapter adapter;
    ArrayList<FilesObject> files = new ArrayList<>();
    Db db;
    static int PERMISSION_EXTERNAL_STORAGE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck1== PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_EXTERNAL_STORAGE);
        }
        else
        {
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto");
            if(!file.exists())
                file.mkdir();
            file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/encrypted");
            if(!file.exists())
                file.mkdir();
            file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/decrypted");
            if(!file.exists())
                file.mkdir();
            file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/intermediate");
            if(!file.exists())
                file.mkdir();
        }
        Intent intent1 = new Intent(MainActivity.this, Poller.class);
        long scTime = 60*200;//12seconds
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent1, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + scTime, pendingIntent);
        db = new Db(this);
        listView = (ListView)findViewById(R.id.history);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                FilesObject file = files.get(position);
                if(file.getDownload()==0)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Enter password to download and decrypt the file");
                    View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.password,null);
                    builder.setView(v);
                    final TextView password = (TextView)v.findViewById(R.id.password);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(check(password.getText().toString())) {
                                FilesObject file = files.get(position);
                                DownloadFile df = new DownloadFile(file,password.getText().toString());
                                df.execute();
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this,"Wrong Password",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setCancelable(true);
                    builder.create().show();
                }
                else {
                    openFile(file);
                }
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,SendFile.class);
                startActivity(i);
            }
        });
        load();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Encrypto.getEncrypto().setContext(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Encrypto.getEncrypto().setContext(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    void openFile(FilesObject file)
    {
        String url = Environment.getExternalStorageDirectory().getPath()+"/Encrypto/decrypted/"+file.getName();
        File f = new File(url);
        Uri uri = Uri.fromFile(f);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (url.contains(".doc") || url.contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");
        } else if (url.contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.contains(".zip") || url.contains(".rar")) {
            intent.setDataAndType(uri, "application/x-wav");
        } else if (url.contains(".rtf")) {
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.contains(".wav") || url.contains(".mp3")) {
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.contains(".gif")) {
            intent.setDataAndType(uri, "image/gif");
        } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (url.contains(".3gp") || url.contains(".mpg") || url.contains(".mpeg") || url.contains(".mpe") || url.contains(".mp4") || url.contains(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logout)
        {
            SharedPreferences.Editor edit = getSharedPreferences(Util.preferences,Context.MODE_PRIVATE).edit();
            edit.clear();
            edit.apply();
            db.drop();
            Intent i = new Intent(this,Check.class);
            startActivity(i);
            finish();
            return true;
        }
        else if(item.getItemId()==R.id.refresh)
        {
            files.clear();
            load();
            return true;
        }
        else
        return super.onOptionsItemSelected(item);
    }

    private boolean check(String s) {
        KeyUtil keyUtil;
        try {
            keyUtil = new KeyUtil(s,MainActivity.this);
            return keyUtil.checkBytes();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void load() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading");
        pd.show();
        files.clear();
        Db db = new Db(this);
        files.addAll(db.getFiles());
        adapter.notifyDataSetChanged();
        pd.dismiss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_EXTERNAL_STORAGE)
        {
            if(grantResults.length>0)
            {
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permission granted successfully", Toast.LENGTH_SHORT).show();
                    File file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto");
                    if(!file.exists())
                        file.mkdir();
                    file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/encrypted");
                    if(!file.exists())
                        file.mkdir();
                    file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/decrypted");
                    if(!file.exists())
                        file.mkdir();
                    file = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/intermediate");
                    if(!file.exists())
                        file.mkdir();
                }
                else
                    finish();
            }
        }
    }

    @Override
    public void refresh() {
        files.clear();
        load();
        Toast.makeText(this,"you have new files",Toast.LENGTH_SHORT).show();
    }

    private class CustomAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return files.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if(v==null)
            {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.filesitem, parent, false);
            }
            TextView name;
            TextView sent;
            name = (TextView) v.findViewById(R.id.name);
            sent = (TextView)v.findViewById(R.id.sent);
            name.setText(files.get(position).getName());
            sent.setText(files.get(position).getFrom());
            return v;
        }
    }

    private class DownloadFile extends AsyncTask<String,String,Boolean>
    {
        FilesObject file;
        ProgressDialog pd;
        String password;

        DownloadFile(FilesObject file,String password) {
            this.file = file;
            this.password = password;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            pd.dismiss();
            if(bool)
            {
                db.updateDownload(file.getLocalId());
                file.setDownload();
                try {
                    DecryptFile decryptFile = new DecryptFile(file,password);
                    decryptFile.execute();
                } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
                //TODO delete file from database
            }
            else
            {
                Toast.makeText(MainActivity.this,"download failed please try again." , Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Downloading...");
            pd.setCancelable(false);
            pd.setIndeterminate(false);
            pd.setMax(100);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try
            {
                URL url = new URL(file.getAddress());
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.connect();
                int lenghtOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/encrypted/"+file.getName());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(output);
                byte data[] = new byte[8192];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(Integer.toString((total*100)/lenghtOfFile));
                    bufferedOutputStream.write(data, 0, count);
                }
                bufferedOutputStream.flush();
                output.flush();
                output.close();
                input.close();
                return true;
            }
            catch (Exception e)
            {
                Log.e("Error: ", e.getMessage());
                return false;
            }
        }

        protected void onProgressUpdate(String... progress) {
            int i = Integer.parseInt(progress[0]);
            pd.setProgress(i);
        }
    }

    private class DecryptFile extends AsyncTask<String,String,Boolean>
    {
        FilesObject file;
        ProgressDialog pd;
        String password;
        SecretKeySpec key;
        private final Cipher cipher;


        DecryptFile(FilesObject file,String password) throws NoSuchAlgorithmException, NoSuchPaddingException {
            this.file = file;
            this.password = password;
            cipher = Cipher.getInstance(Util.cipher1);
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            pd.dismiss();
            if(bool)
            {
                db.updateDecrpt(file.getLocalId());
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Would you like to open the file?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openFile(file);
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(true);
                builder.create().show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"download failed please try again." , Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Decrypting...");
            pd.setCancelable(false);
            pd.setIndeterminate(false);
            pd.setMax(100);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            SharedPreferences sp = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
            String stored = sp.getString(Util.privateKey,null);
            Keys keys = new Keys(password,MainActivity.this);
            try
            {
                PrivateKey privateKey = keys.loadPrivateKey(stored);
                String receivedKeyString = file.getKey();
                byte[] receivedKeyBytes = Base64.decode(receivedKeyString,Base64.DEFAULT);
                byte[] finalReceivedKeyBytes = RSAUtil.RSADecrypt(receivedKeyBytes,privateKey);
                key = new SecretKeySpec(finalReceivedKeyBytes, 0, finalReceivedKeyBytes.length, "AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                FileInputStream inputFile = new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/encrypted/"+file.getName());
                FileOutputStream outputFile = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/decrypted/"+file.getName());
                useKeyOnData(inputFile,outputFile);
                return true;
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                return false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }

        protected void onProgressUpdate(String... progress) {
            int i = Integer.parseInt(progress[0]);
            pd.setProgress(i);
        }

        private void useKeyOnData(FileInputStream inFile, FileOutputStream outFile)  {
            try {
                byte[] input = new byte[4096];
                int bytesRead;
                while ((bytesRead = inFile.read(input)) != -1) {
                    byte[] output = cipher.update(input, 0, bytesRead);
                    if (output != null) {
                        outFile.write(output);
                    }
                }
                byte[] output = cipher.doFinal();
                if (output != null) {
                    outFile.write(output);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}