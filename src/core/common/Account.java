/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
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

        BigInteger p = CryptoService.generatePrime(130);
        BigInteger q = CryptoService.generatePrime(130);
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

        byte[] salt = new byte[4];
        new SecureRandom().nextBytes(salt);
        privateKey = new PrivateKey(d, Base64.encode(salt));
        privateKey.display();
    }

    public static Account create(String name) {
        return new Account(name);
    }

    public String getName() {
        return name;
    }

    public final String getAddress() {
        String hash = CryptoService.hash(CryptoService.generateAddressPubKey(pubKey));
        String prevHash;
        for (int i = 1; i <= privateKey.getAddressSN(); i++) {
            prevHash = CryptoService.hash(CryptoService.encrypt(CryptoService.base64Decode(privateKey.getSalt() + i), pubKey) + hash);
            hash = CryptoService.hash(prevHash);
        }
        return hash;
    }

    public void display() {
        System.out.println("Name          -> " + name);
        System.out.println("Address       -> " + getAddress());
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
        String senderAddressHashPrev;
        String senderAddressHash = CryptoService.hash(CryptoService.generateAddressPubKey(pubKey));
        String receiverAddressHash = receiverAddress;

        Transaction tx = new Transaction(coin + " coins transferred", isCoinBase);
        if (isCoinBase) {
            tx.addOutput(coin, getAddress());
        } else {
            double sum = 0.0;
            LinkedList<UTXO> utxoPool = UTXOPool.getInstance().getList();
            for (int i = 1; i <= privateKey.getAddressSN(); i++) {
                senderAddressHashPrev = CryptoService.hash(CryptoService.encrypt(CryptoService.base64Decode(privateKey.getSalt() + i), pubKey) + senderAddressHash);
                senderAddressHash = CryptoService.hash(senderAddressHashPrev);
                for (UTXO utxo : utxoPool) {
                    if (senderAddressHash.equals(utxo.getReceiverAddress())) {
                        sum += utxo.getCoin();
                        tx.addInput(utxo, CryptoService.encrypt(senderAddressHashPrev, privateKey, pubKey));
                        if (sum >= coin) {
                            break;
                        }
                    }
                }
                if (sum >= coin) {
                    break;
                }
            }
            if (sum >= coin) {
                tx.addOutput(coin, receiverAddressHash);
                double change = sum - coin;
                if (change > 0) {
                    tx.addOutput(change, getAddress());
                }
            } else {
                System.out.println("ERROR : Not enough coins");
            }
        }
        tx.setEncryptedHash(CryptoService.encrypt(tx.hash(), privateKey, pubKey));
        tx.setSenderPubKey(pubKey);
        privateKey.setAddressSN(privateKey.getAddressSN() + 1);
        return tx;
    }

    public double getNumberofCoins() {
        double numberofCoins = 0.0;
        String address = CryptoService.hash(CryptoService.generateAddressPubKey(pubKey));
        LinkedList<UTXO> utxoPool = UTXOPool.getInstance().getList();
        for (int i = 1; i <= privateKey.getAddressSN(); i++) {
            address = CryptoService.hash(CryptoService.hash(CryptoService.encrypt(CryptoService.base64Decode(privateKey.getSalt() + i), pubKey) + address));
            for (UTXO utxo : utxoPool) {
                if (address.equals(utxo.getReceiverAddress())) {
                    numberofCoins += utxo.getCoin();
                }
            }
        }
        return numberofCoins;
    }

    public String getEncodedAddress() {
        return CryptoService.base64Encode(new BigInteger(getAddress()));
    }
}
