/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.math.BigInteger;
import core.common.Account;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class Miner implements Serializable{

    BlockChain blockChain;
    Account account;    
    
    public Miner(Account account){
        this.account = account;
        blockChain = BlockChain.getInstance();
    }

    public void receiveTransaction(List<Transaction> transactions) {
        Block block = processTransaction(transactions);
        if (block != null) {
            Center.getInstance().broadcastBlock(block);
        }
    }

    public void receiveTransaction(Transaction transaction) {
        if (transaction.verify()) {
            Block block = accumulateTransactions(transaction);
            if (block != null) {
                Center.getInstance().broadcastBlock(block);
            }
        } else {
            System.out.println("ERROR : Transaction is not verified");
        }
    }

    private Block accumulateTransactions(Transaction transaction) {
        ArrayList<Transaction> transactions = new ArrayList();
        transactions.add(transaction);
        return processTransaction(transactions);
    }

    public boolean receiveBlock(Block block) {
        if (block == null) {
            return false;
        }
        if (block.confirm()) {
            if (blockChain.add(block)) {
                for (Transaction tx : block.getTransactions()) {
                    tx.removeUTXO();
                    tx.addUTXO();
                }
                return true;
            }
        } else {
            System.out.println("ERROR : Block is not verfied");
        }
        return false;
    }

    public Block processTransaction(List<Transaction> transactions) {
        Block block;
        int noOfCoinbaseTX = HelperService.getNoOfCoinbaseTX(transactions);
        if (noOfCoinbaseTX == 0) {
            transactions.add(account.prepareTX(Center.getInstance().COINBASE, account, true));
        } else if (noOfCoinbaseTX > 1) {
            return null;
        }
        if (blockChain.isEmpty()) {
            block = new Block(BigInteger.ZERO, transactions);
        } else {
            block = new Block(blockChain.getLast().getBlockHash(), transactions);
        }
        BigInteger nonce;
        BigInteger limit = BigInteger.valueOf(1000000);
        for (nonce = BigInteger.ZERO; nonce.compareTo(limit) < 0;) {
            block.setNonce(nonce);
            if (block.verify()) {
                break;
            }
            nonce = nonce.add(BigInteger.ONE);
        }
        return block;
    }
}
