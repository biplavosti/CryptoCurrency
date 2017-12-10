/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

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
public class UTXOPool implements Serializable {

    private volatile LinkedList<UTXO> txOutList;
    private static volatile UTXOPool UNSPENTTXOUT;

    private UTXOPool() {
        txOutList = new LinkedList();
    }

    public static UTXOPool getInstance() {
        if (UNSPENTTXOUT == null) {
            System.out.println("utxo pool is null");
            try {
                UNSPENTTXOUT = loadPoolFromFile();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    getPoolFromPeer(PeerPool.getInstance().getPeerList().getFirst());
                } catch (Exception ex) {
                    UNSPENTTXOUT = new UTXOPool();
                }
            }
            UNSPENTTXOUT.display();
            System.out.println("UTXOPool Started");
        }
        return UNSPENTTXOUT;
    }

    public static UTXOPool getPoolFromPeer(Peer peer) throws IOException, NullPointerException {
        Center center = Center.getInstance();

        Object object = center.hitPeerWait("getUTXOPool", peer);
        if (object == null) {
            UNSPENTTXOUT = new UTXOPool();
        } else {
            UNSPENTTXOUT = (UTXOPool) object;
            UNSPENTTXOUT.save();
        }

        return UNSPENTTXOUT;
    }

    public static UTXOPool loadPoolFromFile() throws IOException, ClassNotFoundException {
        FileInputStream fs = new FileInputStream("utxo.ser");
        ObjectInputStream os = new ObjectInputStream(fs);
        return (UTXOPool) os.readObject();
    }

    public void addUTXO(UTXO utxo) {
        txOutList.add(utxo);
//        save();
    }

    public void removeUTXO(UTXO utxo) {
        txOutList.remove(utxo);
//        save();
    }

    public LinkedList<UTXO> getList() {
        return txOutList;
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("utxo.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.flush();
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ERROR : Could not save UTXO");
        }
    }

    public void display() {
        System.out.println();
        System.out.println("UTXO POOL-> ");
        for (UTXO utxo : txOutList) {
            System.out.println();
            utxo.display();
            System.out.println();
        }
        System.out.println("<-UTXO POOL");
        System.out.println();
    }
}
