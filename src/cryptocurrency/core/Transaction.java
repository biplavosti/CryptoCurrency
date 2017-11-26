/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency.core;

import cryptocurrency.Center;
import cryptography.Main;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Transaction {

    private final String entry;
    private final boolean isCoinBase;
    private final String timeStamp;
    private final LinkedList<Input> inputs = new LinkedList();
    private final LinkedList<Output> outputs = new LinkedList();

    public Transaction(String entry, boolean isCoinBase) {
        this.entry = entry;
        this.isCoinBase = isCoinBase;
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public final BigInteger hash() {
        return Main.hash(
                entry + ""
                + timeStamp
        );
    }

    public void addInput(LinkedList<UTXO> inputsUtxo) {
        for (UTXO utxo : inputsUtxo) {
            inputs.add(new Input(utxo.getTxHash(), utxo.getTxOutHash()));
        }
    }

    public void addOutput(double coin, BigInteger address) {
        Output out = new Output(coin, address);
        outputs.add(out);
    }

    public void addUTXO() {
        for (Output out : outputs) {
            Center.addUTXO(hash(), out.hash(), out.getReceiverAddress(), out.getCoin());
        }
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
        for (Input input : inputs) {
            System.out.println("        [TXI]  -> " + input.getPrevTxHash());
        }
        System.out.println("     Outputs  ->");
        for (Output output : outputs) {
            System.out.println("        [TXO]  -> " + output.getCoin() + " to "
                    + output.getReceiverAddress());
        }
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
        if (isCoinBase) {
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
        if (inputSum != outputSum) {
            return false;
        }

        return true;
    }

    private class Input {

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
            return Main.hash(prevTxHash + "" + prevTxOutputHash);
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

    private class Output {

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
            return Main.hash(receiverAddress + "" + coin);
        }

        private boolean verify() {
            return coin != null && receiverAddress != null;
        }
    }
}
