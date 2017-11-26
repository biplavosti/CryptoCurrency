/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wallet;

import cryptocurrency.Center;
import cryptocurrency.core.Transaction;
import cryptocurrency.core.UTXO;
import cryptography.Main;
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

    public Account(String name) {
        this.name = name;

        BigInteger p = Main.generatePrime(128);
        BigInteger q = Main.generatePrime(129);
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
        pubKey.display();

        privateKey = new PrivateKey(d);
        privateKey.display();

    }

    public void initialTx() {
        this.sendTx(2);
        this.sendTx(3);
        this.sendTx(2);
    }

    public String getName() {
        return name;
    }

    public final BigInteger getAddress() {
        return Main.hash(pubKey.getEncryptionKey() + "" + pubKey.getPrimeProduct());
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
    
    public Transaction prepareTX(double coin, Account receiver, boolean isCoinBase){
        BigInteger senderAddress = getAddress();
        BigInteger receiverAddress = receiver.getAddress();
        
        Transaction tx = new Transaction(coin + " coins transferred", isCoinBase);
        if (isCoinBase) {
            tx.addOutput(coin, Main.encrypt(receiverAddress, pubKey.getEncryptionKey(), pubKey.getPrimeProduct()));            
        } else {
            double sum = 0.0;
            LinkedList<UTXO> inputsUtxo = new LinkedList();
            for (UTXO utxo : Center.getUTXO()) {
                if (senderAddress.equals(Main.decrypt(utxo.getReceiverAddress(), privateKey.getDecryptionKey(), pubKey.getPrimeProduct()))) {
                    sum += utxo.getCoin();
                    inputsUtxo.add(utxo);
                    if (sum >= coin) {
                        break;
                    }
                }
            }
            if (sum >= coin) {
                tx.addInput(inputsUtxo);
                tx.addOutput(coin, Main.encrypt(receiverAddress, receiver.pubKey.getEncryptionKey(), receiver.pubKey.getPrimeProduct()));
                double change = sum - coin;
                if (change > 0) {
                    tx.addOutput(change, Main.encrypt(senderAddress, pubKey.getEncryptionKey(), pubKey.getPrimeProduct()));
                }                
            }
        }
        return tx;
    }    
    
    public double getNumberofCoins() {
        double numberofCoins = 0.0;
        BigInteger address = getAddress();
        for (UTXO utxo : Center.getUTXO()) {
            if (address.equals(Main.decrypt(utxo.getReceiverAddress(), privateKey.getDecryptionKey(), pubKey.getPrimeProduct()))) {
                numberofCoins += utxo.getCoin();
            }
        }
        return numberofCoins;
    }

    public BigInteger getEncryptedAddress() {
        return Main.encrypt(getAddress(), pubKey.getEncryptionKey(), pubKey.getPrimeProduct());
    }
}
