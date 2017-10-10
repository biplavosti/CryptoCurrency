/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptography;

import cryptocurrency.core.Block;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Biplav
 */
public class Main {
    public void generate(){
        int p = 7, q = 11;
    }
    
    public static BigInteger hash(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new BigInteger(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
        }
        return BigInteger.valueOf(0);
    }
}
