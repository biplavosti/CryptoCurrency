/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

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

    private final BigInteger prevHash;
    private final List<Transaction> transactions;
    private final String timeStamp;
    private final BigInteger merkle_root;
    private BigInteger nonce = BigInteger.ZERO;

    public Block(BigInteger prevHash, List<Transaction> transactions) {
        this.prevHash = prevHash;
        this.transactions = transactions;
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        BigInteger codes[] = new BigInteger[transactions.size()];
        for (int i = 0; i < transactions.size(); i++) {
            codes[i] = transactions.get(i).hash();
        }
        merkle_root = HelperService.merkleRoot(codes);
    }

    public final BigInteger hash() {
        return CryptoService.hash(CryptoService.hash(
                merkle_root + ""
                + timeStamp + ""
                + prevHash + ""
                + nonce
        ) + ""
        );
    }

    public BigInteger getPrevHash() {
        return prevHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public BigInteger getBlockHash() {
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
        System.out.println("Previous Hash -> " + prevHash.toString());
        System.out.println("Current Hash  -> " + this.getBlockHash());
        System.out.println("Merkle Root   -> " + merkle_root.toString());
        System.out.println("Nonce         -> " + nonce);
        System.out.println("Transactions  -> ");
        for (Transaction tx : transactions) {
            tx.display();
        }
    }

    public boolean confirm() {
        int noOfCoinbaseTX = 0;
        for (Transaction tx : transactions) {
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
        return getBlockHash().remainder(BigInteger.valueOf(100)).equals(BigInteger.valueOf(0));
    }
}
