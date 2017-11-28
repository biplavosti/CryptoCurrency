/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import core.common.Account;

/**
 *
 * @author Biplav
 */
public class Center {

    private static final List<Account> USERS = new ArrayList();
    private static final BlockChain BLOCKCHAIN = BlockChain.getInstance();
    private static final Miner MINER = new Miner(Center.createAccount("Miner"), BLOCKCHAIN);
    private static final LinkedList<UTXO> UNSPENTTXOUT = new LinkedList();
    public static double COINBASE = 25;

    public static List<Account> getUsers() {
        return USERS;
    }

    public static Miner getMiner() {
        return MINER;
    }

    public static BlockChain getBlockChain() {
        return BLOCKCHAIN;
    }

    public static LinkedList<UTXO> getUTXO() {
        return UNSPENTTXOUT;
    }

    public static void addUTXO(BigInteger txHash, BigInteger outHash,
            BigInteger address, double coin) {
        UNSPENTTXOUT.add(new UTXO(txHash, outHash, address, coin));
    }

    public static void removeUnspent(UTXO utxo) {
        UNSPENTTXOUT.remove(utxo);
    }

    public static void broadcastTransaction(List<Transaction> transaction) {
        MINER.receiveTransaction(transaction);
    }

    public static void broadcastTransaction(Transaction transaction) {
        MINER.receiveTransaction(transaction);
    }

    public static void broadcastBlock(Block block) {
        MINER.receiveBlock(block);
    }

    private static Account registerAccount(Account user) {        
        
        for(Account acc : USERS){
            if(acc == user) return acc;
        }
        USERS.add(user);
        return user;
    }

    public static void showBlockChain() {
        System.out.println();
        System.out.println("Block Chain->");
        BLOCKCHAIN.display();
        System.out.println("<-Block Chain");
        System.out.println();
    }
    
    public static Account createAccount(String name){
        return registerAccount(Account.create(name));
    }
    
    public static void mineFirstCoin(){
        broadcastTransaction(MINER.account.prepareTX(COINBASE, MINER.account, true));
    }
}
