/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.math.BigInteger;
import core.common.Account;

/**
 *
 * @author Biplav
 */
public class Miner {    
    BlockChain blockChain;
    Account account;

    public Miner(Account account, BlockChain chain) {
        this.account = account;
        blockChain = chain;        
    }

    public void receiveTransaction(Transaction[] transaction) {
        Block block = processTransaction(transaction);
        if (block != null) {
            Center.broadcastBlock(block);
        }
    }

    public void receiveTransaction(Transaction transaction) {
        if (transaction.verify()) {
            Block block = accumulateTransactions(transaction);
            if (block != null) {
                Center.broadcastBlock(block);
            }
        }else{
            System.out.println("ERROR : Transaction is not verified");
        }
    }

    private Block accumulateTransactions(Transaction transaction) {
        Transaction tx = account.prepareTX(Center.COINBASE,account,true);
        return processTransaction(new Transaction[]{transaction, tx});
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
        }else{
            System.out.println("ERROR : Block is not verfied");
        }
        return false;
    }

    public Block processTransaction(Transaction[] transaction) {
        Block block;                   
        if (blockChain.isEmpty()) {
            block = new Block(BigInteger.ZERO, transaction);
        } else {
            block = new Block(blockChain.getLast().getBlockHash(), transaction);
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
