/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wallet;

import java.math.BigInteger;

/**
 *
 * @author Biplav
 */
class PrivateKey {

    private final BigInteger decryptionKey;

    public PrivateKey(BigInteger decryptionKey) {
        this.decryptionKey = decryptionKey;
    }

    public BigInteger getDecryptionKey() {
        return decryptionKey;
    }

    public void display() {
        System.out.println("decryption key = " + decryptionKey);        
    }
}