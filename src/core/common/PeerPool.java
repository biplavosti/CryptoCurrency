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
import java.util.List;

/**
 *
 * @author Biplav
 */
public class PeerPool implements Serializable {

    private volatile LinkedList<Peer> peerList;
    private static volatile PeerPool PEERPOOL;

    private PeerPool() {
        peerList = new LinkedList();
    }

    public static PeerPool getInstance() {
        if (PEERPOOL == null) {
            System.out.println("PeerPool is null");
            try {
                FileInputStream fs = new FileInputStream("peerpool.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                PEERPOOL = (PeerPool) os.readObject();
            } catch (IOException | ClassNotFoundException e) {
                PEERPOOL = new PeerPool();
            }
        }
        return PEERPOOL;
    }

    public boolean addPeer(Peer peer) {
        peerList.add(peer);
        save();
        return true;
    }

    public boolean addPeer(final String host, final int port) {
        if (get(host, port) == null) {
            return this.addPeer(new Peer(host, port));
        }
        return false;
    }

    public Peer get(final String host, final int port) {
        for (Peer peer : peerList) {
            if (peer.getHost().equals(host) && peer.getPort() == port) {
                return peer;
            }
        }
        return null;
    }

    public LinkedList<Peer> getPeerList() {
        return peerList;
    }

    public void addAll(List<Peer> list) {
        for (Peer peer : list) {
            if (get(peer.getHost(), peer.getPort()) != null) {
                list.remove(peer);
            }
        }
        peerList.addAll(list);
        Peer self = get(Center.getInstance().SELF.getHost(), Center.getInstance().SELF.getPort());
        peerList.remove(self);
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("peerpool.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.close();
        } catch (IOException ex) {
            System.out.println("ERROR : Could not save PeerPool");
        }
    }

    public void display() {
        System.out.println();
        System.out.println("PEER ->");
        for (Peer peer : peerList) {
            peer.display();
        }
        System.out.println("<- PEER");
        System.out.println();
    }
}
