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
class PublicKey {
    private final BigInteger encryptionKey;
    private final BigInteger primeProduct;

    public PublicKey(BigInteger encryptionKey, BigInteger primeProduct) {
        this.encryptionKey = encryptionKey;
        this.primeProduct = primeProduct;
    }

    public BigInteger getEncryptionKey() {
        return encryptionKey;
    }

    public BigInteger getPrimeProduct() {
        return primeProduct;
    }
    
    public void display(){
        System.out.println("encryption key = "+ encryptionKey);
        System.out.println("primeProduct = " + primeProduct);
    }
}
