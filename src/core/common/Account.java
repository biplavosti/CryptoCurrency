/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.Center;
import core.Transaction;
import core.UTXO;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Account {

    private final String name;
    private final PrivateKey privateKey;
    private final PublicKey pubKey;

    private Account(String name) {
        this.name = name;

        BigInteger p = CryptoService.generatePrime(128);
        BigInteger q = CryptoService.generatePrime(129);
        BigInteger e = BigInteger.ZERO;
        BigInteger d = BigInteger.ZERO;
        BigInteger pMinus1 = p.subtract(BigInteger.ONE);
        BigInteger qMinus1 = q.subtract(BigInteger.ONE);
        BigInteger phi = pMinus1.multiply(qMinus1);
        BigInteger sum;
        boolean found = false;
        for (BigInteger ee = BigInteger.ONE; ee.compareTo(phi) < 0;) {
            ee = ee.add(BigInteger.valueOf(2));
            if (pMinus1.remainder(ee).equals(BigInteger.ZERO)
                    || qMinus1.remainder(ee).equals(BigInteger.ZERO)) {
                continue;
            }
            sum = BigInteger.ONE;
            for (BigInteger k = BigInteger.ZERO; k.compareTo(phi) < 0;) {
                k = k.add(BigInteger.ONE);
                if (k.remainder(ee).equals(BigInteger.ZERO)) {
                    continue;
                }
                sum = sum.add(phi);
                if (sum.remainder(ee).equals(BigInteger.ZERO)) {
                    e = ee;
                    d = sum.divide(e);
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        pubKey = new PublicKey(e, p.multiply(q));
        privateKey = new PrivateKey(d);
    }
    
    public static Account create(String name){
        return new Account(name);
    }    

    public String getName() {
        return name;
    }

    public final BigInteger getAddress() {
        return CryptoService.hash(pubKey.getEncryptionKey() + "" + pubKey.getPrimeProduct());
    }

    public void display() {
        System.out.println("Name          -> " + name);
        System.out.println("Address       -> " + getEncryptedAddress());
    }

    private void sendTx(double coin) {
        sendTx(coin, this, true);
    }

    public final void sendTx(double coin, Account receiverAccount, boolean isCoinBase) {
        Transaction tx = prepareTX(coin,receiverAccount,isCoinBase);      
        Center.broadcastTransaction(tx); 
    }
    
    public Transaction prepareTX(double coin){
        return prepareTX(coin, this, true);
    }
    
    public Transaction prepareTX(double coin, Account receiver, boolean isCoinBase){
        BigInteger senderAddress = getAddress();
        BigInteger receiverAddress = receiver.getAddress();
        
        Transaction tx = new Transaction(coin + " coins transferred", isCoinBase);
        if (isCoinBase) {
            tx.addOutput(coin, CryptoService.encrypt(receiverAddress, pubKey));            
        } else {
            double sum = 0.0;
            LinkedList<UTXO> inputsUtxo = new LinkedList();
            for (UTXO utxo : Center.getUTXO()) {
                if (senderAddress.equals(CryptoService.decrypt(utxo.getReceiverAddress(), privateKey, pubKey))) {
                    sum += utxo.getCoin();
                    inputsUtxo.add(utxo);
                    if (sum >= coin) {
                        break;
                    }
                }
            }
            if (sum >= coin) {
                tx.addInput(inputsUtxo);
                tx.addOutput(coin, CryptoService.encrypt(receiverAddress, receiver.pubKey));
                double change = sum - coin;
                if (change > 0) {
                    tx.addOutput(change, CryptoService.encrypt(senderAddress, pubKey));
                }                
            }else{
                System.out.println("ERROR : Not enough coins");
            }
        }
        return tx;
    }    
    
    public double getNumberofCoins() {
        double numberofCoins = 0.0;
        BigInteger address = getAddress();
        for (UTXO utxo : Center.getUTXO()) {
            if (address.equals(CryptoService.decrypt(utxo.getReceiverAddress(), privateKey, pubKey))) {
                numberofCoins += utxo.getCoin();
            }
        }
        return numberofCoins;
    }

    public BigInteger getEncryptedAddress() {
        return CryptoService.encrypt(getAddress(), pubKey);
    }
}
