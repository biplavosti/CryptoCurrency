/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Account implements Serializable {

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
        pubKey.display();
        privateKey = new PrivateKey(d);
    }

    public static Account create(String name) {
        return new Account(name);
    }

    public String getName() {
        return name;
    }

    public final String getAddress() {
        //return pubKey.hash();
        return CryptoService.generateAddressPubKey(pubKey);
    }

    public void display() {
        System.out.println("Name          -> " + name);
        System.out.println("Address       -> " + getEncryptedAddress());
    }

    private void sendTx(double coin) {
        sendTx(coin, this.getAddress(), true);
    }

    public final void sendTx(double coin, String receiverAddress, boolean isCoinBase) {
        Transaction tx = prepareTX(coin, receiverAddress, isCoinBase);
        Center.getInstance().broadcastTransaction(tx);
    }

    public Transaction prepareTX(double coin) {
        return prepareTX(coin, this.getAddress(), true);
    }

    public Transaction prepareTX(double coin, String receiverAddress, boolean isCoinBase) {
        String senderAddressHash = CryptoService.hash(getAddress());
        String receiverAddressHash = CryptoService.hash(receiverAddress);

        Transaction tx = new Transaction(coin + " coins transferred", isCoinBase);
        if (isCoinBase) {
            tx.addOutput(coin, CryptoService.encrypt(receiverAddressHash, pubKey));
        } else {
            double sum = 0.0;
            LinkedList<UTXO> inputsUtxo = new LinkedList();
            String senderAddressHashEncrypted = CryptoService.encrypt(senderAddressHash, pubKey);
            for (UTXO utxo : UTXOPool.getInstance().getList()) {
                if (senderAddressHashEncrypted.equals(utxo.getReceiverAddress())) {
                    sum += utxo.getCoin();
                    inputsUtxo.add(utxo);
                    if (sum >= coin) {
                        break;
                    }
                }
            }
            if (sum >= coin) {
                tx.addInput(inputsUtxo);
                tx.addOutput(coin, CryptoService.encrypt(receiverAddressHash, CryptoService.generatePubKey(receiverAddress)));
                double change = sum - coin;
                if (change > 0) {
                    tx.addOutput(change, CryptoService.encrypt(senderAddressHash, pubKey));
                }
            } else {
                System.out.println("ERROR : Not enough coins");
            }
            tx.setEncryptedHash(CryptoService.encrypt(tx.hash(), privateKey, pubKey));
            tx.setSenderAddress(getAddress());
        }
        return tx;
    }

    public double getNumberofCoins() {
        double numberofCoins = 0.0;
        String address = CryptoService.hash(getAddress());
        for (UTXO utxo : UTXOPool.getInstance().getList()) {
            if (address.equals(CryptoService.decrypt(utxo.getReceiverAddress(), privateKey, pubKey))) {
                numberofCoins += utxo.getCoin();
            }
        }
        return numberofCoins;
    }

    public String getEncryptedAddress() {
        return CryptoService.encrypt(getAddress(), pubKey);
    }
}
