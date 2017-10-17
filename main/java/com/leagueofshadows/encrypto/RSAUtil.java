package com.leagueofshadows.encrypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

class RSAUtil
{

    static byte[] RSAEncrypt(final byte[] plain, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // System.out.println(new String(encryptedBytes));
        return cipher.doFinal(plain);
    }

    static byte[] RSADecrypt(final byte[] encryptedBytes, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher1 = Cipher.getInstance("RSA");
        cipher1.init(Cipher.DECRYPT_MODE, privateKey);
        //System.out.println(decrypted);
        return cipher1.doFinal(encryptedBytes);
    }
}
