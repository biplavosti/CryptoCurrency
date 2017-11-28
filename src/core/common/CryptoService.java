/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.Block;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Biplav
 */
public class CryptoService {

    public static BigInteger hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new BigInteger(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigInteger.valueOf(0);
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

    private static BigInteger encrypt(BigInteger msg, BigInteger key, BigInteger product) {
        return mod(msg, key, product);
    }

    public static BigInteger encrypt(BigInteger msg, PublicKey pubKey) {
        return encrypt(msg, pubKey.getEncryptionKey(), pubKey.getPrimeProduct());
    }

    private static BigInteger decrypt(BigInteger msg, BigInteger key, BigInteger product) {
        return mod(msg, key, product);
    }

    public static BigInteger decrypt(BigInteger msg, PrivateKey privateKey, PublicKey pubKey) {
        return encrypt(msg, privateKey.getDecryptionKey(), pubKey.getPrimeProduct());
    }

    public static BigInteger generatePrime(int bit) {
        return BigInteger.probablePrime(bit, new Random());
    }
}
