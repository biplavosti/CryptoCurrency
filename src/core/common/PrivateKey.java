/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import java.io.Serializable;
import java.math.BigInteger;

/**
 *
 * @author Biplav
 */
public final class PrivateKey implements Serializable {

    private final BigInteger decryptionKey;
    private final String salt;
    private int addressSN;

    public PrivateKey(BigInteger decryptionKey, String salt) {
        this.decryptionKey = decryptionKey;
        this.salt = salt;
        addressSN = 1;
    }

    public BigInteger getDecryptionKey() {
        return decryptionKey;
    }
    
    public void setAddressSN(int addressSN){
        this.addressSN = addressSN;
    }
    
    public String getSalt(){
        return salt;
    }
    
    public int getAddressSN(){
        return addressSN;
    }

    public void display() {
        System.out.println("decryption key = " + decryptionKey);
        System.out.println("salt           = " + salt);
        System.out.println("No of hash     = " + addressSN);
    }
}
