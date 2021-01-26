package me.jeybi.uztaxi.utils;

import com.google.android.gms.common.util.Base64Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;


import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public  class NaiveHmacSigner {

    public static byte[] hmac(String algorithm, byte[] secret, String data)
            throws NoSuchAlgorithmException, InvalidKeyException {

        final Mac mac = Mac.getInstance(algorithm);
        final SecretKeySpec spec = new SecretKeySpec(secret, algorithm);
        mac.init(spec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String DateSignature() {
        return OffsetDateTime.now().format(RFC_1123_DATE_TIME);
    }

    public static String AuthSignature(long id , String key, String method, String path)
            throws InvalidKeyException, NoSuchAlgorithmException {

        String identity = String.valueOf(id);
        byte[] secret = Base64Utils.decode(key);

        final String date = OffsetDateTime.now().format(RFC_1123_DATE_TIME);
        final String nonce = String.valueOf(System.currentTimeMillis());

        final String data = method + path + date + nonce;

        final String digest = Base64Utils.encode(
                hmac("HmacSHA256", secret, data)
        );


        return String.format("hmac %s:%s:%s", identity, nonce, digest).trim();
    }




}