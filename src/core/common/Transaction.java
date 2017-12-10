/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.TransactionPool;
import java.io.Serializable;
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
    private String blockHash;
    private String senderAddress;
    private String encryptedHash;

    public Transaction(String entry, boolean isCoinBase) {
        this.entry = entry;
        this.coinBase = isCoinBase;
        timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
    }

    public Transaction(Transaction tx) {
        entry = tx.entry;
        coinBase = tx.coinBase;
        timeStamp = tx.timeStamp;
        for (Input input : tx.inputs) {
            inputs.add(input);
        }
        for (Output out : tx.outputs) {
            outputs.add(out);
        }
        blockHash = tx.blockHash;
        senderAddress = tx.senderAddress;
        encryptedHash = tx.encryptedHash;
    }

    public final String hash() {
        return CryptoService.hash(
                entry + ""
                + timeStamp
        );
    }

    public boolean isCoinBase() {
        return coinBase;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getEncryptedHash() {
        return encryptedHash;
    }

    public void setEncryptedHash(String txEncHash) {
        encryptedHash = txEncHash;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String address) {
        senderAddress = address;
    }

    public void addInput(LinkedList<UTXO> inputsUtxo) {
        inputsUtxo.forEach((utxo) -> {
            inputs.add(new Input(utxo.getTxHash(), utxo.getTxOutHash()));
        });
    }

    public void addOutput(double coin, String address) {
        Output out = new Output(coin, address);
        outputs.add(out);
    }
    
    public LinkedList<Output> getOutput(){
        return outputs;
    }

    public void addUTXO() {
        outputs.forEach((out) -> {
            UTXOPool.getInstance().addUTXO(new UTXO(hash(), out.hash(), out.getReceiverAddress(), out.getCoin()));
        });

    }

    public void removeUTXO() {
        LinkedList<UTXO> UTXOList = UTXOPool.getInstance().getList();
        for (Input input : inputs) {
            for (UTXO utxo : UTXOList) {
                if (input.getPrevTxHash().equals(utxo.getTxHash())
                        && input.getPrevTxOutputHash().equals(utxo.getTxOutHash())) {
                    UTXOList.remove(utxo);
                    break;
                }
            }
        }
    }

    public void display() {
        System.out.println("  [T]   -> " + entry);
        System.out.println("     Timestamp  ->" + timeStamp);
        System.out.println("     Inputs     ->");
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

        PublicKey senderPubKey = CryptoService.generatePubKey(senderAddress);
        if (!hash().equals(CryptoService.decrypt(encryptedHash, senderPubKey))) {
            return false;
        }

        for (Transaction trans : TransactionPool.getInstance().getList()) {
            if (hash().equals(trans.hash())) {
                return false;
            }
        }

        double inputSum = 0.0;
        double outputSum = 0.0;
        final String senderAddressHashEncrypted = CryptoService.encrypt(CryptoService.hash(senderAddress), senderPubKey);
        for (Input input : inputs) {
            UTXO matchedUtxo = input.getUTXO();
            if (matchedUtxo == null) {
                return false;
            } else if (!senderAddressHashEncrypted.equals(matchedUtxo.getReceiverAddress())) {
                return false;
            } else {
                inputSum += matchedUtxo.getCoin();
            }
        }
        for (Output out : outputs) {
            outputSum += out.getCoin();
        }
        return inputSum >= outputSum;
    }

    private class Input implements Serializable {

        private String prevTxHash;
        private String prevTxOutputHash;

        private Input(String prevTxHash, String prevTxOutputHash) {
            this.prevTxHash = prevTxHash;
            this.prevTxOutputHash = prevTxOutputHash;
        }

        private String getPrevTxHash() {
            return prevTxHash;
        }

        private void setPrevTxHash(String prevTxHash) {
            this.prevTxHash = prevTxHash;
        }

        private String getPrevTxOutputHash() {
            return prevTxOutputHash;
        }

        private void setPrevTxOutputHash(String prevTxOutputHash) {
            this.prevTxOutputHash = prevTxOutputHash;
        }

        private String hash() {
            return CryptoService.hash(timeStamp + "" + prevTxHash + "" + prevTxOutputHash);
        }

        private UTXO getUTXO() {
            for (UTXO utxo : UTXOPool.getInstance().getList()) {
                if (prevTxHash.equals(utxo.getTxHash())
                        && prevTxOutputHash.equals(utxo.getTxOutHash())) {
                    return utxo;
                }
            }
            return null;
        }

    }

    public class Output implements Serializable {

        private Double coin;
        private String receiverAddress;

        private Output(double coin, String receiverAddress) {
            this.coin = coin;
            this.receiverAddress = receiverAddress;
        }

        public double getCoin() {
            return coin;
        }

        private void setCoin(double coin) {
            this.coin = coin;
        }

        public String getReceiverAddress() {
            return receiverAddress;
        }

        private void setReceiverAddress(String receiverAddress) {
            this.receiverAddress = receiverAddress;
        }

        private String hash() {
            return CryptoService.hash(timeStamp + "" + receiverAddress + "" + coin);
        }

        private boolean verify() {
            return coin != null && receiverAddress != null;
        }
    }
}
