/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency;

import cryptocurrency.core.Block;
import cryptocurrency.core.Transaction;
import cryptocurrency.core.BlockChain;
import cryptocurrency.core.UTXO;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import wallet.Account;

/**
 *
 * @author Biplav
 */
public class Center {

    private static final List<Account> USERS = new ArrayList();
    private static final List<Miner> MINERS = new LinkedList();
    private static final BlockChain BLOCKCHAIN = new BlockChain();
    private static final LinkedList<UTXO> UNSPENTTXOUT = new LinkedList();

    public static List<Account> getUsers() {
        return USERS;
    }

    public static List<Miner> getMiners() {
        return MINERS;
    }

    public static BlockChain getBlockChain() {
        return BLOCKCHAIN;
    }
    
    public static LinkedList<UTXO> getUTXO(){
        return UNSPENTTXOUT;
    }
    
    public static void addUTXO(BigInteger txHash, BigInteger outHash, 
            BigInteger address, double coin){
        UNSPENTTXOUT.add(new UTXO(txHash, outHash, address, coin));
    }
    
    public static void removeUnspent(UTXO utxo){
        UNSPENTTXOUT.remove(utxo);
    }

    public static void broadcastTransaction(Transaction[] transaction) {
        for (Miner miner : MINERS) {
            miner.receiveTransaction(transaction);
        }
    }
    
    public static void broadcastTransaction(String transaction) {
        for (Miner miner : MINERS) {
            miner.receiveTransaction(new Transaction(transaction));
        }
    }

    public static void broadcastBlock(Block block) {
        int trueCounter = 0;
        for (Miner miner : MINERS) {
            if(miner.receiveBlock(block)){
                trueCounter++;
            }
        }
        if(trueCounter > 50 / 100 * MINERS.size()) {
            BLOCKCHAIN.add(block);
        }
    }
    
    public static void registerAccount(Account user){
        if(user != null){
            USERS.add(user);
        }
    }
    
    public static void registerMiner(Miner miner) {
        if (miner != null) {
            MINERS.add(miner);
        }
    }
}
