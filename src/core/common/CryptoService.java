/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 *
 * @author Biplav
 */
public class CryptoService {

    public static String hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new BigInteger(digest.digest(key.getBytes(StandardCharsets.UTF_8))).toString();
        } catch (NoSuchAlgorithmException ex) {
        }
        return "0";
    }

    public static String hashTwice(String key) {
        return hash(hash(key));
    }

    public static BigInteger mod(BigInteger base, BigInteger exponent, BigInteger dividend) {
        return modulus(base, exponent, dividend, BigInteger.ONE);
    }

    private static BigInteger modulus(BigInteger base, BigInteger exp, BigInteger div, BigInteger rem) {
        if (base.equals(BigInteger.ONE) || exp.equals(BigInteger.ZERO)) {
            return rem;
        } else if (exp.equals(BigInteger.ONE)) {
            return base.multiply(rem).remainder(div);
        } else if (base.equals(BigInteger.ZERO)) {
            return BigInteger.ZERO;
        }

        if (base.compareTo(div) >= 0) {
            return modulus(base.remainder(div), exp, div, rem);
        }

        return (modulus(
                base.multiply(base).remainder(div),
                exp.divide(BigInteger.valueOf(2)), div,
                exp.remainder(BigInteger.valueOf(2))
                        .equals(BigInteger.ONE)
                ? base.multiply(rem).remainder(div) : rem));
    }

    private static String encrypt(BigInteger msg, BigInteger key, BigInteger product) {
        return mod(msg, key, product).toString();
    }

    public static String encrypt(String msg, PublicKey pubKey) {
        return encrypt(new BigInteger(msg), pubKey.getEncryptionKey(), pubKey.getPrimeProduct());
    }

    public static String encrypt(String msg, PrivateKey privateKey, PublicKey pubKey) {
        return encrypt(new BigInteger(msg), privateKey.getDecryptionKey(), pubKey.getPrimeProduct());
    }

    private static String decrypt(BigInteger msg, BigInteger key, BigInteger product) {
        return mod(msg, key, product).toString();
    }

    public static String decrypt(String msg, PrivateKey privateKey, PublicKey pubKey) {
        return decrypt(new BigInteger(msg), privateKey.getDecryptionKey(), pubKey.getPrimeProduct());
    }

    public static String decrypt(String msg, PublicKey pubKey) {
        return decrypt(new BigInteger(msg), pubKey.getEncryptionKey(), pubKey.getPrimeProduct());
    }

    public static BigInteger generatePrime(int bit) {
        return BigInteger.probablePrime(bit, new Random());
    }

    public static String generateAddressPubKey(PublicKey pub) {
        int enc = pub.getEncryptionKey().intValue();
        int counter = 0;
        while (enc > 0) {
            enc = enc / 10;
            counter++;
        }
//        generatePubKey(counter + "" + pub.getEncryptionKey() + "" + pub.getPrimeProduct()).display();
        return (counter + "" + pub.getEncryptionKey() + "" + pub.getPrimeProduct());
    }

    public static PublicKey generatePubKey(String address) {
        int enc = Integer.parseInt(address.charAt(0) + "");
        BigInteger eKey = new BigInteger(address.substring(1, enc + 1));
        BigInteger product = new BigInteger(address.substring(enc + 1));
        return new PublicKey(eKey, product);
    }
    
    public static String base64Encode(BigInteger originalStr){
        return Base64.encode(originalStr);
    }
    
    public static String base64Decode(String encodedStr){
        try {
            return new BigInteger(Base64.decode(encodedStr)).toString();
        } catch (Base64DecodingException ex) {
        }
        return "0";
    }
}
