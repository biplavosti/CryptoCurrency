/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency.core;

import cryptocurrency.Center;
import cryptography.Main;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Transaction {

    private final String entry;
    private final LinkedList<Input> inputs = new LinkedList();
    private final LinkedList<Output> outputs = new LinkedList();

    public Transaction(String entry) {
        this.entry = entry;
    }

    public final BigInteger hash() {
        return Main.hash(entry);
    }

    public void addInput(LinkedList<UTXO> inputsUtxo) {
        for(UTXO utxo : inputsUtxo){
            inputs.add(new Input(utxo.getTxHash(),utxo.getTxOutHash()));
            Center.getUTXO().remove(utxo);
        }
    }

    public void addOutput(double coin, BigInteger address) {
        Output out = new Output(coin, address);
        outputs.add(out);
        Center.addUTXO(hash(), out.hash(), address, coin);
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

    private class Input {

        private BigInteger prevTxHash;
        private BigInteger prevTxOutputHash;        

        public Input(BigInteger prevTxHash, BigInteger prevTxOutputHash) {
            this.prevTxHash = prevTxHash;
            this.prevTxOutputHash = prevTxOutputHash;
        }

        public BigInteger getPrevTxHash() {
            return prevTxHash;
        }

        public void setPrevTxHash(BigInteger prevTxHash) {
            this.prevTxHash = prevTxHash;
        }

        public BigInteger getPrevTxOutputHash() {
            return prevTxOutputHash;
        }

        public void setPrevTxOutputHash(BigInteger prevTxOutputHash) {
            this.prevTxOutputHash = prevTxOutputHash;
        }

        public final BigInteger hash() {
            return Main.hash(prevTxHash + "" + prevTxOutputHash);
        }

    }

    private class Output {

        private double coin;
        private BigInteger receiverAddress;

        public Output(double coin, BigInteger receiverAddress) {
            this.coin = coin;
            this.receiverAddress = receiverAddress;
        }

        public double getCoin() {
            return coin;
        }

        public void setCoin(double coin) {
            this.coin = coin;
        }

        public BigInteger getReceiverAddress() {
            return receiverAddress;
        }

        public void setReceiverAddress(BigInteger receiverAddress) {
            this.receiverAddress = receiverAddress;
        }

        public final BigInteger hash() {
            return Main.hash(receiverAddress + "" + coin);
        }
    }
}
