package com.leagueofshadows.encrypto;

import android.os.Environment;

class Util {
    static final String url ="http://araniisansthan.com/Encrypto/";
    static final String urlUploads ="http://araniisansthan.com/Encrypto/uploads/";
    static final String username = "username";
    static final String password="password";
    static final String success="success";
    static final String privateKey="privateKey";
    static final String publicKey="publicKey";
    static final String preferences = "preferences";
    static final String name="name";
    static final int keySize = 256;
    static final int numberOfIterations = 1000;
    static final String factory = "PBKDF2WithHmacSHA1";
    static final String cipher = "AES/CBC/PKCS5Padding";
    static final String cipher1 = "AES";
    static final String path = Environment.getExternalStorageDirectory().getPath()+"/Encrypto/";
    static final String sdcard = Environment.getExternalStorageDirectory().getPath();
    static final String output = Environment.getExternalStorageDirectory().getPath()+"/Encrypto/Outputs/";
    static final String received = "Received";
    static final String sync = "sync";
    static final String fileName = "filename";
    static final String fileAddress = "fileaddress";
    static final String id = "id";
    static final String response="response";
    static final String files= "files";
    static final String databaseID= "databaseID";
    static final String from = "from";
    static final  byte[] check = {21,22,23,24,25,26,27,28,29,30};
    static final String saltString="saltString";
    static final String ivString="ivString";
    static final String key="key";
    static final String toUser = "toUser";
}
