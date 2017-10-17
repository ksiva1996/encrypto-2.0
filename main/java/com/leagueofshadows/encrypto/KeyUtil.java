package com.leagueofshadows.encrypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static com.leagueofshadows.encrypto.Util.keySize;


class KeyUtil { 

    private String Password;
    private final SecretKeyFactory factory;
    private final Cipher cipher;
    Context context;
    KeyUtil(String Password,Context context) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.Password = Password;
        factory = SecretKeyFactory.getInstance(Util.factory);
        cipher = Cipher.getInstance(Util.cipher);
        this.context=context;
    }


    String encryptKey(String privateKey) throws InvalidKeyException, InvalidParameterSpecException, InvalidKeySpecException {
        byte[] salt = new byte[8];
        Random rnd = new Random();
        rnd.nextBytes(salt);
        SecretKey secretkey = makeKey(Password, salt);
        cipher.init(Cipher.ENCRYPT_MODE, secretkey);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] privateKeyBytes = useKeyOnData(privateKey.getBytes());
        byte[] finalArray = new byte[salt.length+iv.length+privateKeyBytes.length];
        System.arraycopy(salt, 0,finalArray, 0, salt.length);
        System.arraycopy(iv, 0,finalArray, salt.length, iv.length);
        System.arraycopy(privateKeyBytes,0,finalArray,salt.length+iv.length,privateKeyBytes.length);
        Log.d("enc",Integer.toString(finalArray.length));
        return getstring(finalArray);
    }


    String decryptPrivateKey(String saved) throws InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] savedBytes = getbytes(saved);
        Log.d("dec",Integer.toString(savedBytes.length));
        byte[] salt = new byte[8];
        System.arraycopy(savedBytes,0,salt,0,salt.length);
        byte[] iv = new byte[16];
        System.arraycopy(savedBytes,salt.length,iv,0,iv.length);
        byte[] privateKeyBytes = new byte[savedBytes.length-24];
        System.arraycopy(savedBytes,salt.length+iv.length,privateKeyBytes,0,privateKeyBytes.length);
        PBEKeySpec spec = new PBEKeySpec(Password.toCharArray(), salt,Util.numberOfIterations,keySize);
        SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
        byte[] out = useKeyOnData(privateKeyBytes);
        return new String(out);
    }

    private SecretKey makeKey(String password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,Util.numberOfIterations, keySize);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private byte[] useKeyOnData(byte[] inBytes)  {
        try
        {
            byte[] out = cipher.update(inBytes, 0,inBytes.length);
            byte[] finalOut = cipher.doFinal();
            byte[] output = new byte[out.length+finalOut.length];
            System.arraycopy(out, 0, output, 0, out.length);
            System.arraycopy(finalOut, 0, output, out.length, finalOut.length);
            return output;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }


    boolean checkBytes()
    {
        SharedPreferences sp = context.getSharedPreferences("preferences",Context.MODE_PRIVATE);
        String saltString = sp.getString(Util.saltString,null);
        byte[] salt = getbytes(saltString);
        String ivString = sp.getString(Util.ivString,null);
        byte[] iv = getbytes(ivString);
        PBEKeySpec spec = new PBEKeySpec(Password.toCharArray(), salt,Util.numberOfIterations,keySize);
        SecretKey secretKey;
        try
        {
            secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey,new IvParameterSpec(iv));
            String encrypted = sp.getString("encrypted",null);
            byte[] inBytes = getbytes(encrypted);
            byte[] out = useKeyOnData(inBytes);
            return Arrays.equals(out,Util.check);
        }
        catch (InvalidKeySpecException | InvalidKeyException e)
        {
            e.printStackTrace();
            return false;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return false;
        }

    }


    void encryptBytes()
    {
        byte[] salt = new byte[8];
        Random rnd = new Random();
        rnd.nextBytes(salt);
        String saltString = getstring(salt);
        Log.e(Util.saltString,saltString);

        SharedPreferences sp = context.getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Util.saltString,saltString);
        PBEKeySpec spec = new PBEKeySpec(Password.toCharArray(), salt,Util.numberOfIterations,keySize);
        SecretKey secretKey;
        try
        {
            secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

            String ivString = getstring(iv);
            edit.putString(Util.ivString,ivString);
            Log.e(Util.ivString,ivString);
            byte[] out  = useKeyOnData(Util.check);
            String encrypted = getstring(out);
            edit.putString("encrypted",encrypted);
            edit.apply();
        }
        catch (InvalidKeySpecException | InvalidKeyException | InvalidParameterSpecException e)
        {
            e.printStackTrace();
        }

    }



    private String getstring(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        int x;
        for(int i=0;i<bytes.length-1;i++)
        {
            x = bytes[i];
            builder.append(Integer.toString(x)).append("+");
        }
        x = bytes[bytes.length-1];
        builder.append(Integer.toString(x));
        return builder.toString();
    }

    private byte[] getbytes(String s)
    {
        StringTokenizer st = new StringTokenizer(s,"+");
        byte[] bytes = new byte[st.countTokens()];
        int i=0;
        while(st.hasMoreTokens())
        {
            int x= Integer.parseInt(st.nextToken());
            bytes[i]= (byte) x;
            i++;
        }
        return bytes;
    }


}
