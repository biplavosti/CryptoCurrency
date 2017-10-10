/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency.core;

import java.math.BigInteger;

/**
 *
 * @author Biplav
 */
public class UTXO {
    private final BigInteger txHash;
    private final BigInteger txOutHash;
    private final BigInteger receiverAddress;
    private final double coin;

    public UTXO(BigInteger txHash, BigInteger OutTxHash, 
            BigInteger receiverAddress, double coin) {
        this.txHash = txHash;
        this.txOutHash = OutTxHash;
        this.receiverAddress = receiverAddress;
        this.coin = coin;
    }  

    public BigInteger getTxHash() {
        return txHash;
    }

    public BigInteger getTxOutHash() {
        return txOutHash;
    }

    public BigInteger getReceiverAddress() {
        return receiverAddress;
    }

    public double getCoin() {
        return coin;
    }
}
