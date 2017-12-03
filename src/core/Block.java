/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Transaction;
import core.common.CryptoService;
import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class Block implements Serializable {

    private final String prevHash;
    private List<Transaction> transactions;
    private final String timeStamp;
    private final String merkle_root;
    private BigInteger nonce = BigInteger.ZERO;

    public Block(String prevHash, List<Transaction> transactions) {
        this.prevHash = prevHash;
        this.transactions = transactions;
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String codes[] = new String[transactions.size()];
        for (int i = 0; i < transactions.size(); i++) {
            codes[i] = transactions.get(i).hash();
        }
        merkle_root = HelperService.merkleRoot(codes);
    }

    private String hash() {
        return CryptoService.hashTwice(
                merkle_root + ""
                + timeStamp + ""
                + prevHash + ""
                + nonce
        );
    }

    public void setTransactions(List<Transaction> tx) {
        transactions = tx;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public List<Transaction> getStoredTransactions() {
        return TransactionPool.getInstance().getList(getBlockHash());
    }

    public List<Transaction> getLiveTransactions() {
        return transactions;
    }

    public String getBlockHash() {
        return hash();
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void display() {
        System.out.println("Time Stamp    -> " + timeStamp);
        System.out.println("Previous Hash -> " + prevHash);
        System.out.println("Current Hash  -> " + this.getBlockHash());
        System.out.println("Merkle Root   -> " + merkle_root);
        System.out.println("Nonce         -> " + nonce);
        System.out.println("Transactions  -> ");
        for (Transaction tx : getStoredTransactions()) {
            tx.display();
        }
    }

    public boolean confirm() {
        int noOfCoinbaseTX = 0;
        for (Transaction tx : getLiveTransactions()) {
            if (!tx.verify()) {
                return false;
            }
            if (tx.isCoinBase()) {
                noOfCoinbaseTX++;
                if (noOfCoinbaseTX > 1) {
                    return false;
                }
            }
        }
        return verify();
    }

    public boolean verify() {
        return new BigInteger(getBlockHash()).remainder(BigInteger.valueOf(100)).equals(BigInteger.valueOf(0));
    }
}
