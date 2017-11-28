/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.Block;
import core.BlockChain;
import core.Miner;
import core.Transaction;
import core.UTXO;
import core.UnspentTX;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author Biplav
 */
public class Center implements Serializable {

    private final List<Account> USERS;
    private transient BlockChain BLOCKCHAIN;
    private final Miner MINER;
    private transient UnspentTX UNSPENTTXOUT;
    public transient double COINBASE = 25;

    private static Center CENTER;

    private Center() {
        USERS = new ArrayList();
        BLOCKCHAIN = BlockChain.getInstance();
        UNSPENTTXOUT = UnspentTX.getInstance();
        MINER = new Miner(createAccount("Miner"));
        COINBASE = 25;
    }

    public static Center getInstance() {
        if (CENTER == null) {
            try {
                FileInputStream fs = new FileInputStream("center.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                CENTER = (Center) os.readObject();
                CENTER.BLOCKCHAIN = BlockChain.getInstance();
                CENTER.UNSPENTTXOUT = UnspentTX.getInstance();
                CENTER.COINBASE = 25;
            } catch (IOException | ClassNotFoundException e) {
                CENTER = new Center();
            }
        }
        return CENTER;
    }

    public List<Account> getUsers() {
        return USERS;
    }

    public Miner getMiner() {
        return MINER;
    }

    public BlockChain getBlockChain() {
        return BLOCKCHAIN;
    }

    public UnspentTX getUTXO() {
        return UNSPENTTXOUT;
    }

    public void addUTXO(BigInteger txHash, BigInteger outHash,
            BigInteger address, double coin) {
        UNSPENTTXOUT.addUTXO(new UTXO(txHash, outHash, address, coin));
    }

    public void removeUnspent(UTXO utxo) {
        UNSPENTTXOUT.removeUTXO(utxo);
    }

    public void broadcastTransaction(List<Transaction> transaction) {
        MINER.receiveTransaction(transaction);
    }

    public void broadcastTransaction(Transaction transaction) {
        MINER.receiveTransaction(transaction);
    }

    public void broadcastBlock(Block block) {
        MINER.receiveBlock(block);
    }

    private Account registerAccount(Account user) {

        for (Account acc : USERS) {
            if (acc == user) {
                return acc;
            }
        }
        USERS.add(user);
        return user;
    }

    public void showBlockChain() {
        BLOCKCHAIN.display();
    }

    public final Account createAccount(String name) {
        return registerAccount(Account.create(name));
    }

    public void mineFirstCoin() {
        broadcastTransaction(MINER.getAccount().prepareTX(COINBASE));
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("center.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.close();
        } catch (IOException ex) {
            System.out.println("ERROR : Could not save Center");
        }
    }
}
