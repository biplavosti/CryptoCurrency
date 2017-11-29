/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.Block;
import core.BlockChain;
import core.Miner;
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
    private final Miner MINER;
    public transient double COINBASE = 25;
    private static Center CENTER;

    private Center() {
        USERS = new ArrayList();
        MINER = new Miner(createAccount("Miner"));
        COINBASE = 25;
    }

    public static Center getInstance() {
        if (CENTER == null) {
            try {
                FileInputStream fs = new FileInputStream("center.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                CENTER = (Center) os.readObject();
                BlockChain.getInstance();
                UTXOPool.getInstance();
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
