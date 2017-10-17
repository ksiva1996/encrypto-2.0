package com.leagueofshadows.encrypto;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.NoSuchPaddingException;


class Keys  {

    private String Password;
    private Context context;
    private KeyPair keyPair;
    Keys(String password, Context context)
    {
        this.context = context;
        this.Password = password;
    }
    String[] createKeys()
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keypair = keyPairGenerator.generateKeyPair();
            this.keyPair = keypair;
            PrivateKey privateKey = keypair.getPrivate();
            String privateKeyString = savePrivateKey(privateKey);
            String finalPrivateKeyString = encryptPasswordBased(privateKeyString,Password);
            PublicKey publicKey = keypair.getPublic();
            String publicKeyString = savePublicKey(publicKey);
            SharedPreferences sp = context.getSharedPreferences(Util.preferences,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(Util.publicKey,publicKeyString);
            edit.putString(Util.privateKey,finalPrivateKeyString);
            edit.apply();
            return  new String[]{publicKeyString,finalPrivateKeyString};
        }
        catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    KeyPair getKeyPair() {
        return keyPair;
    }

     private String encryptPasswordBased(String privateKeyString, String password) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException {
        KeyUtil keyutil = new KeyUtil(password,context);
        return keyutil.encryptKey(privateKeyString);
    }

     PrivateKey loadPrivateKey(String stored) throws GeneralSecurityException
    {
        KeyUtil keyutil = new KeyUtil(Password,context);
        String[] x = keyutil.decryptPrivateKey(stored).split("%");
        BigInteger privateKeyModulus = new BigInteger(x[0]);
        BigInteger privateKeyExponent = new BigInteger(x[1]);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(privateKeyModulus,privateKeyExponent);
        return fact.generatePrivate(spec);
    }


    PublicKey loadPublicKey(String stored) throws GeneralSecurityException {
        String[] x = stored.split("%");
        BigInteger publicKeyModulus = new BigInteger(x[0]);
        BigInteger publicKeyExponent = new BigInteger(x[1]);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec spec = new RSAPublicKeySpec(publicKeyModulus,publicKeyExponent);
        return fact.generatePublic(spec);
    }

    private String savePrivateKey(PrivateKey privateKey) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPrivateKeySpec spec = fact.getKeySpec(privateKey,
                RSAPrivateKeySpec.class);
        BigInteger privateModulus = spec.getModulus();
        BigInteger privateExponent  = spec.getPrivateExponent();
        return privateModulus.toString()+"%"+privateExponent.toString();
    }


     private String savePublicKey(PublicKey publicKey) throws GeneralSecurityException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec spec = fact.getKeySpec(publicKey,
                RSAPublicKeySpec.class);
        BigInteger publicModulus = spec.getModulus();
        BigInteger publicExponent  = spec.getPublicExponent();
        return publicModulus.toString()+"%"+publicExponent.toString();
    }

}
