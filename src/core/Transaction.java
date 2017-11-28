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
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Transaction implements Serializable {

    private final String entry;
    private final boolean coinBase;
    private final String timeStamp;
    private final LinkedList<Input> inputs = new LinkedList();
    private final LinkedList<Output> outputs = new LinkedList();

    public Transaction(String entry, boolean isCoinBase) {
        this.entry = entry;
        this.coinBase = isCoinBase;
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public final BigInteger hash() {
        return CryptoService.hash(
                entry + ""
                + timeStamp
        );
    }

    public boolean isCoinBase() {
        return coinBase;
    }

    public void addInput(LinkedList<UTXO> inputsUtxo) {
        inputsUtxo.forEach((utxo) -> {
            inputs.add(new Input(utxo.getTxHash(), utxo.getTxOutHash()));
        });
    }

    public void addOutput(double coin, BigInteger address) {
        Output out = new Output(coin, address);
        outputs.add(out);
    }

    public void addUTXO() {
        outputs.forEach((out) -> {
            Center.addUTXO(hash(), out.hash(), out.getReceiverAddress(), out.getCoin());
        });
    }

    public void removeUTXO() {
        for (Input input : inputs) {
            for (UTXO utxo : Center.getUTXO()) {
                if (input.getPrevTxHash() == utxo.getTxHash()
                        && input.getPrevTxOutputHash() == utxo.getTxOutHash()) {
                    Center.getUTXO().remove(utxo);
                    break;
                }
            }
        }

    }

    public void display() {
        System.out.println("  [T]   -> " + entry);
        System.out.println("     Inputs  ->");
        inputs.forEach((input) -> {
            System.out.println("        [TXI]  -> " + input.getPrevTxHash());
        });
        System.out.println("     Outputs  ->");
        outputs.forEach((output) -> {
            System.out.println("        [TXO]  -> " + output.getCoin() + " to "
                    + output.getReceiverAddress());
        });
    }

    public boolean verify() {
        if (outputs.isEmpty()) {
            return false;
        }
        for (Output out : outputs) {
            if (!out.verify()) {
                return false;
            }
        }

        boolean isInputEmpty = inputs.isEmpty();
        if (coinBase) {
            if (isInputEmpty) {
                return true;
            }
        } else {
            if (isInputEmpty) {
                return false;
            }
        }

        double inputSum = 0.0;
        double outputSum = 0.0;
        for (Input input : inputs) {
            UTXO matchedUtxo = input.getUTXO();
            if (matchedUtxo == null) {
                return false;
            } else {
                inputSum += matchedUtxo.getCoin();
            }
        }
        for (Output out : outputs) {
            outputSum += out.getCoin();
        }
        return inputSum == outputSum;
    }

    private class Input implements Serializable {

        private BigInteger prevTxHash;
        private BigInteger prevTxOutputHash;

        private Input(BigInteger prevTxHash, BigInteger prevTxOutputHash) {
            this.prevTxHash = prevTxHash;
            this.prevTxOutputHash = prevTxOutputHash;
        }

        private BigInteger getPrevTxHash() {
            return prevTxHash;
        }

        private void setPrevTxHash(BigInteger prevTxHash) {
            this.prevTxHash = prevTxHash;
        }

        private BigInteger getPrevTxOutputHash() {
            return prevTxOutputHash;
        }

        private void setPrevTxOutputHash(BigInteger prevTxOutputHash) {
            this.prevTxOutputHash = prevTxOutputHash;
        }

        private BigInteger hash() {
            return CryptoService.hash(prevTxHash + "" + prevTxOutputHash);
        }

        private UTXO getUTXO() {
            for (UTXO utxo : Center.getUTXO()) {
                if (getPrevTxHash() == utxo.getTxHash()
                        && getPrevTxOutputHash() == utxo.getTxOutHash()) {
                    return utxo;
                }
            }
            return null;
        }

    }

    private class Output implements Serializable {

        private Double coin;
        private BigInteger receiverAddress;

        private Output(double coin, BigInteger receiverAddress) {
            this.coin = coin;
            this.receiverAddress = receiverAddress;
        }

        private double getCoin() {
            return coin;
        }

        private void setCoin(double coin) {
            this.coin = coin;
        }

        private BigInteger getReceiverAddress() {
            return receiverAddress;
        }

        private void setReceiverAddress(BigInteger receiverAddress) {
            this.receiverAddress = receiverAddress;
        }

        private BigInteger hash() {
            return CryptoService.hash(receiverAddress + "" + coin);
        }

        private boolean verify() {
            return coin != null && receiverAddress != null;
        }
    }
}
