/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Center;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public final class BlockChain implements Serializable {

    private volatile LinkedList<Block> chain;
    private static volatile BlockChain BLOCKCHAIN;
    private transient static volatile TransactionPool txList;

    private BlockChain() {
        chain = new LinkedList();
    }

    public static BlockChain getInstance() {
        if (BLOCKCHAIN == null) {
            try {
                BLOCKCHAIN = loadChainFromFile();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    BLOCKCHAIN = getChainFromPeer();
                } catch (IOException ex) {
                    BLOCKCHAIN = new BlockChain();
                }
            }
            BLOCKCHAIN.display();
            System.out.println("BlockChain Started");
            txList = TransactionPool.getInstance();
        }
        return BLOCKCHAIN;
    }

    public static BlockChain getChainFromPeer() throws IOException {
        Center center = Center.getInstance();

        Object object = center.hitServer("getChain", true);
        if (object == null) {
            BLOCKCHAIN = new BlockChain();
        } else {
            BlockChain bc = (BlockChain) object;
            bc.save();
            BLOCKCHAIN = bc;
        }
        txList = TransactionPool.getPoolFromPeer();
        return BLOCKCHAIN;
    }

    public BigInteger getCollectiveNonce() {
        BigInteger nonceSum = BigInteger.ZERO;
        for (Block block : chain) {
            nonceSum.add(block.getNonce());
        }
        return nonceSum;
    }

    public static BlockChain loadChainFromFile() throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream("blockchain.ser");
        ObjectInputStream os = new ObjectInputStream(fs);
        BLOCKCHAIN = (BlockChain) os.readObject();
        txList = TransactionPool.getInstance();
        return BLOCKCHAIN;
    }

    public boolean add(Block block) {
        txList.add(block.getLiveTransactions());
//        block.setTransactions(null);
        chain.add(block);
//        save();
//        display();
        return true;
    }

    public void display() {
        synchronized (BlockChain.getInstance()) {
            System.out.println();
            System.out.println("Block Chain-> ");
            for (Block block : chain) {
                System.out.println();
                block.display();
                System.out.println();
            }
            System.out.println("<-Block Chain");
            System.out.println();
        }

    }

    public boolean isEmpty() {
        return chain.isEmpty();
    }

    public Block getLast() {
        return chain.getLast();
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("blockchain.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.close();
        } catch (IOException ex) {
            System.out.println("ERROR : Could not save BlockChain");
        }
    }
}
