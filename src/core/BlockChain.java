/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public final class BlockChain implements Serializable {

    private final LinkedList<Block> chain;
    private static BlockChain BLOCKCHAIN;
    private transient static TransactionList txList;

    private BlockChain() {
        chain = new LinkedList();
        txList = TransactionList.getInstance();
    }

    public static BlockChain getInstance() {
        if (BLOCKCHAIN == null) {
            try {
                FileInputStream fs = new FileInputStream("blockchain.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                BLOCKCHAIN = (BlockChain) os.readObject();
                txList = TransactionList.getInstance();
                BLOCKCHAIN.display();
            } catch (IOException | ClassNotFoundException e) {
                BLOCKCHAIN = new BlockChain();
            }
        }
        return BLOCKCHAIN;
    }

    public boolean add(Block block) {
        chain.add(block);
        txList.add(block.getLiveTransactions());
        save();
        return true;
    }

    public void display() {
        System.out.println();
        System.out.println("Block Chain-> " + chain.size() + " === " + txList.getList().size());
        for (Block block : chain) {
            System.out.println();
            block.display();
            System.out.println();
        }
        System.out.println("<-Block Chain");
        getLast().display();
        System.out.println();
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
