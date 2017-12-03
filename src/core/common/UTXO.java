/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import java.io.Serializable;

/**
 *
 * @author Biplav
 */
public class UTXO implements Serializable {

    private final String txHash;
    private final String txOutHash;
    private final String receiverAddress;
    private final double coin;

    public UTXO(String txHash, String OutTxHash,
            String receiverAddress, double coin) {
        this.txHash = txHash;
        this.txOutHash = OutTxHash;
        this.receiverAddress = receiverAddress;
        this.coin = coin;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getTxOutHash() {
        return txOutHash;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public double getCoin() {
        return coin;
    }

    public void display() {
        System.out.println("[UTXO]  ->");
        System.out.println("     tx    -> " + txHash);
        System.out.println("     txout -> " + txOutHash);
        System.out.println("     coin  -> " + coin);
        System.out.println("     add   -> " + receiverAddress);
    }
}
