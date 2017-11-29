/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Transaction;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class TransactionPool implements Serializable {

    private final LinkedList<Transaction> txList;
    private static TransactionPool TXLIST;

    private TransactionPool() {
        txList = new LinkedList();
    }

    public static TransactionPool getInstance() {
        if (TXLIST == null) {
            try {
                FileInputStream fs = new FileInputStream("transactions.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                TXLIST = (TransactionPool) os.readObject();
            } catch (IOException | ClassNotFoundException e) {
                TXLIST = new TransactionPool();
            }
        }
        return TXLIST;
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("transactions.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.close();
        } catch (IOException ex) {
            System.out.println("ERROR : Could not save Transaction list");
        }
    }

    public boolean add(final Transaction tx) {
        if (txList.add(tx)) {
            save();
            return true;
        }
        return false;
    }

    public boolean add(final List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            txList.add(tx);
        }
        save();
        return true;
    }

    public LinkedList<Transaction> getList() {
        return txList;
    }

    public LinkedList<Transaction> getList(final BigInteger blockHash) {
        LinkedList<Transaction> list = new LinkedList();
        for (Transaction tx : txList) {
            if (blockHash.equals(tx.getBlockHash())) {
                list.add(tx);
            }
        }
        return list;
    }
}
