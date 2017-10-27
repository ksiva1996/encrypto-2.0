package com.leagueofshadows.encrypto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SendFile extends AppCompatActivity {

    EditText username;
    Button selectFile;
    Button sendFile;
    TextView filename;
    File file = null;
    String user;
    String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);
        username = (EditText) findViewById(R.id.username);
        selectFile = (Button)findViewById(R.id.selectfile);
        sendFile = (Button)findViewById(R.id.send);
        filename = (TextView)findViewById(R.id.filename);

        Intent i = getIntent();
        String action = i.getAction();
        String type = i.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri uri = i.getParcelableExtra(Intent.EXTRA_STREAM);

            if (uri != null) {
                file = new File(uri.getPath());
                filename.setText(file.getName());
            }
        }

        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SendFile.this,FilePicker.class);
                startActivityForResult(i,1);
            }
        });
        SharedPreferences sp = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
        user = sp.getString(Util.username,null);
        sendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String u = username.getText().toString();
                if(check(u))
                {
                    if(file!=null&&file.exists())
                    {
                        upload(u);
                    }
                    else
                        Toast.makeText(SendFile.this,"please select a valid file",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    username.setError("username required");
                    username.requestFocus();
                }
            }
        });
    }

    private boolean check(String u) {
        return !u.equals("");
    }

    void upload(final String username)
    {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.show();
        String url = Util.url + "public.php";
        StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try
                {
                    pd.dismiss();
                    Log.i("ENCRYPTO",response);
                    JSONObject json = new JSONObject(response);
                    int success = json.getInt(Util.success);
                    if (success == 1)
                    {
                        publicKey = json.getString(Util.publicKey);
                        KeyGenerator keygen = KeyGenerator.getInstance("AES");
                        keygen.init(256);
                        SecretKey secretKey = keygen.generateKey();
                        EncryptFile encryptFile = new EncryptFile(file,secretKey,username);
                        encryptFile.execute();
                    }
                    else
                    {
                        Toast.makeText(SendFile.this,"user doesn't exist",Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    pd.dismiss();
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(SendFile.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Util.username,username);
                return params;
            }
        };
        VolleyHelper.getInstance(this).addToRequestQueue(req);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
                if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                        file = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        filename.setText(file.getName());
                    }
            }
        }
        else
            Toast.makeText(this,"file not selected",Toast.LENGTH_SHORT).show();
    }

    private class EncryptFile extends AsyncTask<String,Boolean,Boolean> {

        private File inFile;
        private final Cipher cipher;
        SecretKey key;
        String toUser;
        ProgressDialog pd;
        EncryptFile(File file,SecretKey key,String toUser) throws NoSuchAlgorithmException, NoSuchPaddingException {

            this.key = key;
            this.inFile = file;
            this.toUser = toUser;
            cipher = Cipher.getInstance(Util.cipher1);
        }

        @Override
        protected void onPreExecute()
        {
            pd = new ProgressDialog(SendFile.this);
            pd.setMessage("Encrypting...");
            pd.setCancelable(false);
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if(pd!=null)
            pd.dismiss();
            if(s)
            {
                Keys keys = new Keys(null,SendFile.this);
                PublicKey publickey;
                try {
                    publickey = keys.loadPublicKey(publicKey);
                    String secretKeyString = Base64.encodeToString(RSAUtil.RSAEncrypt(key.getEncoded(),publickey),Base64.DEFAULT);
                    UploadFile uploadFile = new UploadFile(new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/intermediate/"+file.getName()),secretKeyString,toUser);
                    uploadFile.execute();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(SendFile.this,"try upload again",Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }

        @Override
        protected Boolean doInBackground(String... p) {

            try
            {
                FileInputStream inputFile = new FileInputStream(inFile);
                FileOutputStream outputFile = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/intermediate/"+file.getName());
                cipher.init(Cipher.ENCRYPT_MODE,key);
                useKeyOnData(inputFile, outputFile);
                inputFile.close();
                outputFile.flush();
                outputFile.close();
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
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

    private class UploadFile extends AsyncTask<String,Boolean,Boolean> {

        private File file;
        String secretKeyString;
        String toUser;
        ProgressDialog pd;
        UploadFile(File file, String key,String toUser) {

            this.file = file;
            this.toUser = toUser;
            this.secretKeyString = key;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(SendFile.this);
            pd.setMessage("Uploading...");
            pd.setIndeterminate(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setMax(100);
            pd.setCancelable(false);
            pd.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean s) {
            if (s)
            {
                String url = Util.url+"det.php";
                StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        try
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            int x = jsonObject.getInt("success");
                            if(x==1)
                            {
                                File delFile = new File(Environment.getExternalStorageDirectory().getPath()+"/Encrypto/intermediate/"+file.getName());
                                if(delFile.exists())
                                    delFile.delete();
                                Toast.makeText(SendFile.this,"sent successfully",Toast.LENGTH_SHORT).show();
                            }
                            else
                                Toast.makeText(SendFile.this,"please try again",Toast.LENGTH_SHORT).show();
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SendFile.this,"please try again",Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put(Util.fileName,file.getName());
                        params.put(Util.from,user);
                        params.put(Util.key,secretKeyString);
                        params.put(Util.toUser,toUser);
                        return params;
                    }
                };
                VolleyHelper.getInstance(SendFile.this).addToRequestQueue(request);
                Log.i("Key",secretKeyString);
                Toast.makeText(SendFile.this,"sent successfully",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(SendFile.this,"try this again",Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);
        }

        void onProgressUpdate(String... progress) {
            int i = Integer.parseInt(progress[0]);
            pd.setProgress(i);
        }

        @Override
        protected Boolean doInBackground(String... p) {
            HttpURLConnection connection;
            int serverResponseCode;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead,bytesAvailable,bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                URL url = new URL(Util.url+"upload.php");
                Log.i("URL",Util.url+"upload.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",file.getAbsolutePath());

                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + file.getAbsolutePath() + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                int count = 0;
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                buffer = new byte[bufferSize];
                bytesRead = fileInputStream.read(buffer,0,bufferSize);
                while (bytesRead > 0){
                    count = count+bytesRead;
                    onProgressUpdate(Integer.toString((bytesRead*100)/bytesAvailable));
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                serverResponseCode = connection.getResponseCode();
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
                String serverResponseMessage = connection.getResponseMessage();
                Log.i("TAG-ENCRYPTO", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                return serverResponseCode == 200;
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SendFile.this,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
