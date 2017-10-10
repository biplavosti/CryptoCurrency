/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency;

import cryptocurrency.core.Block;
import cryptocurrency.core.Transaction;
import cryptocurrency.core.BlockChain;
import java.math.BigInteger;

/**
 *
 * @author Biplav
 */
public class Miner {

    String name;
    BlockChain blockChain;

    public Miner(String name, BlockChain chain) {
        this.name = name;
        blockChain = chain;
    }

    public void receiveTransaction(Transaction[] transaction) {
        Block block = processTransaction(transaction);
        if (block != null) {
            Center.broadcastBlock(block);
        }
    }
    
    public void receiveTransaction(Transaction transaction) {
        Block block = accumulateTransactions(transaction);
        if (block != null) {
            Center.broadcastBlock(block);
        }
    }
    
    private Block accumulateTransactions(Transaction transaction){
        return processTransaction(new Transaction[]{transaction});
    }

    public boolean receiveBlock(Block block) {
        if (verifyBlock(block)) {
            blockChain.add(block);
            return true;
        }
        return false;
    }

    public Block processTransaction(Transaction[] transaction) {
        Block block;
        if (blockChain.blockChain.isEmpty()) {
            block = new Block(BigInteger.valueOf(0), transaction);
        } else {
            block = new Block(blockChain.blockChain.getLast().getBlockHash(), transaction);
        }
        BigInteger nonce;
        BigInteger limit = BigInteger.valueOf(1000000);
        for (nonce = BigInteger.valueOf(0); nonce.compareTo(limit) < 0;) {            
            block.setNonce(nonce);
            if (verifyBlock(block)) {
                System.out.println("Verified: "+nonce.toString());
                break;
            }   
            nonce = nonce.add(BigInteger.ONE);
        }        
        return block;
    }

    public boolean verifyBlock(Block block) {
        if (block == null) {
            return false;
        }
        return block.getBlockHash().remainder(BigInteger.valueOf(100000)).equals(BigInteger.valueOf(0));
    }
}
