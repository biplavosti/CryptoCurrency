/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Center;
import core.common.Peer;
import core.common.PeerPool;
import core.common.Transaction;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class TransactionPool implements Serializable {

    private volatile LinkedList<Transaction> txList;
    private static volatile TransactionPool TXLIST;

    private TransactionPool() {
        txList = new LinkedList();
    }

    public static TransactionPool getInstance() {
        if (TXLIST == null) {
            System.out.println("TransactionPool is null");
            try {
                TXLIST = loadPoolFromFile();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    getPoolFromPeer(PeerPool.getInstance().getPeerList().getFirst());
                } catch (Exception ex) {
                    TXLIST = new TransactionPool();
                }
            }
            TXLIST.display();
            System.out.println("TransactionPool Started");
        }
        return TXLIST;
    }

    public static TransactionPool getPoolFromPeer(Peer peer) throws IOException, NullPointerException {
        Center center = Center.getInstance();

        Object object = center.hitPeerWait("getTXPool", peer);
        if (object == null) {
            TXLIST = new TransactionPool();
        } else {
            TXLIST = (TransactionPool) object;
            TXLIST.save();
        }

        return TXLIST;
    }

    public static TransactionPool loadPoolFromFile() throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream("transactions.ser");
        ObjectInputStream os = new ObjectInputStream(fs);
        return (TransactionPool) os.readObject();
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
//            save();
            return true;
        }
        return false;
    }

    public boolean add(final List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            txList.add(tx);
        }
//            save();
        return true;
    }

    public LinkedList<Transaction> getList() {
        return txList;
    }

    public LinkedList<Transaction> getList(final String blockHash) {
        LinkedList<Transaction> list = new LinkedList();
        for (Transaction tx : txList) {
            if (blockHash.equals(tx.getBlockHash())) {
                list.add(tx);
            }
        }
        return list;
    }

    public void display() {
        System.out.println();
        System.out.println("Transaction POOL-> ");
        for (Transaction tx : txList) {
            System.out.println();
            tx.display();
            System.out.println();
        }
        System.out.println("<-Transaction POOL");
        System.out.println();
    }
}
