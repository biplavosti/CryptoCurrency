/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import core.Block;
import core.BlockChain;
import core.Miner;
import core.TransactionPool;
import java.util.ArrayList;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Biplav
 */
public class Center implements Serializable {

    public transient double COINBASE = 25;
    public transient volatile static Boolean OPEN = false;

    private final List<Account> USERS;
    private final Miner MINER;
    private static Center CENTER;
    private transient LinkedList<String> broadCastedTXMemPool;
    private transient LinkedList<String> broadCastedBlockMemPool;
    public transient LinkedList<Transaction> newNoBlockTX = new LinkedList();
    public transient LinkedList<String> minedBlocksHash = new LinkedList();
    public transient volatile static Block CURRENTBLOCK;
    public transient volatile static boolean VALIDPEERBLOCK = false;
    public transient volatile static Block PEERBLOCK;
    public Peer SELF;

    private Center() {
        USERS = new ArrayList();
        MINER = new Miner(createAccount("Miner"));
        broadCastedTXMemPool = new LinkedList();
        broadCastedBlockMemPool = new LinkedList();
    }

    public static Center getInstance() {
        if (CENTER == null) {
            try {
                FileInputStream fs = new FileInputStream("center.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                CENTER = (Center) os.readObject();
                CENTER.broadCastedTXMemPool = new LinkedList();
                CENTER.broadCastedBlockMemPool = new LinkedList();
            } catch (IOException | ClassNotFoundException e) {
                CENTER = new Center();
            }
            CENTER.COINBASE = 25;
            OPEN = true;
            CENTER.newNoBlockTX = new LinkedList();
            System.out.println("Center Started");

            ServerSocket server = null;
            try {
                server = new ServerSocket(CENTER.SELF.getPort());
            } catch (NullPointerException | IOException ne) {
                try {
                    server = new ServerSocket(29236);
                } catch (IOException ex) {
                    try {
                        server = new ServerSocket(0);
                    } catch (IOException ex1) {
                        System.out.println("ERROR : Server start failed.");
                    }
                }
            } finally {
                if (server != null) {
                    CENTER.SELF = new Peer("localhost", server.getLocalPort());
                    new Thread(CENTER.new Server(server)).start();
                }
            }
        }
        return CENTER;
    }

    public static void setup() {
        getInstance();
        PeerPool pool = PeerPool.getInstance();
        if (CENTER.SELF.getPort() != 29236) {
            pool.addPeer(CENTER.SELF.getHost(), 29236);
        }
        try {
            Peer firstPeer = pool.getPeerList().getFirst();
            pool.addAll(((PeerPool) CENTER.hitPeerWait("getPeerPool", firstPeer)).getPeerList());
            CENTER.hitPeerWait("addMeasPeer-" + CENTER.SELF.getPort(), firstPeer);
        } catch (Exception ex) {
        }
        UTXOPool.getInstance();
        BlockChain.getInstance();

        CENTER.MINER.start();
        System.out.println("Miner Started");
    }

    public static void reloadFromPeer(Peer peer) throws IOException {
        UTXOPool.getPoolFromPeer(peer);
        BlockChain.getChainFromPeer(peer);
    }

    public List<Transaction> submitNewTXList() {
        List<Transaction> list;
        synchronized (this) {
            list = new LinkedList(newNoBlockTX);
            newNoBlockTX.clear();
        }
        return list;
    }

    public List<Account> getUsers() {
        return USERS;
    }

    public Miner getMiner() {
        return MINER;
    }

    public void addNewTX(Transaction tx) {
        newNoBlockTX.add(tx);
    }

    public void broadcastTransaction(Transaction transaction) {
        String hash = transaction.hash();
        if (!broadCastedTXMemPool.contains(hash)) {
            if (!transaction.verify()) {
                System.out.println("ERROR : Transaction is not verified");
                return;
            }
            try {
                hitMultiplePeerNoWait(transaction);
            } catch (IOException | NullPointerException ex) {
                ex.printStackTrace();
            }
            synchronized (this) {
                broadCastedTXMemPool.add(hash);
                newNoBlockTX.add(transaction);
            }
        }
    }

    public synchronized String broadcastBlock(Block block, boolean isMyBlock) {
        String confirmation = "CONFIRMED";
        String blockHash = block.getBlockHash();
        if (isMyBlock && VALIDPEERBLOCK) {
            return "UNCONFIRMED";
        }
        BigInteger myCollectiveNonce = BlockChain.getInstance().getCollectiveNonce();
        if (!block.confirm()) {
            System.out.println("ERROR : " + (isMyBlock ? "MY " : "PEER ") + "Block is not verified : " + blockHash);
            return "UNCONFIRMED";
        }
        if (!isMyBlock) {
            VALIDPEERBLOCK = true;
            PEERBLOCK = block;
        }
        if (!broadCastedBlockMemPool.contains(blockHash)) {
            broadCastedBlockMemPool.add(blockHash);
            if (!isMyBlock) {
                try {
                    hitMultiplePeerNoWait(block);
                } catch (IOException | NullPointerException ex) {
                }
                confirmation = "CONFIRMED";
            } else {
                CURRENTBLOCK = block;
                Object[] bucket = null;
                try {
                    bucket = hitMultiplePeerWait(block);
                } catch (IOException | NullPointerException | InterruptedException ex) {
                    confirmation = "CONFIRMED";
                }
                if (bucket != null) {
                    HashMap<String, Integer> map = new HashMap();
                    map.put("CONFIRMED", 1); // first confirmation from me.
                    map.put("UNCONFIRMED", 0);
                    String key;
                    for (Object object : bucket) {
                        if (object != null) {
                            key = (String) object;
                            System.out.println("confirmation : " + key);
                            if (map.containsKey(key)) {
                                map.put(key, map.get(key) + 1);
                            } else {
                                map.put(key, 1);
                            }
                        }
                    }
                    if (map.get("UNCONFIRMED") > map.get("CONFIRMED")) {
                        confirmation = "UNCONFIRMED";
                    }
                }
                if ("UNCONFIRMED".equals(confirmation)) {
                    try {
                        bucket = hitMultiplePeerWait("getCollectiveNonce");
                    } catch (IOException | NullPointerException | InterruptedException ex) {
                    }
                    if (bucket != null) {
                        HashMap<String, Integer> map = new HashMap();
                        for (Object object : bucket) {
                            String key;
                            if (object != null) {
                                key = (String) object;
                                if (map.containsKey(key)) {
                                    map.put(key, map.get(key) + 1);
                                } else {
                                    map.put(key, 1);
                                }
                            }
                        }
                        int max = 0;
                        String nonce = "";
                        for (String key : map.keySet()) {
                            int value = map.get(key);
                            if (value > max) {
                                max = value;
                                nonce = key;
                            }
                        }
                        BigInteger peerCollectiveNonce = new BigInteger(nonce);
                        if (myCollectiveNonce.compareTo(peerCollectiveNonce) < 0) {
                            try {
                                Peer peer = null;
                                for (int i = 0; i < bucket.length; i++) {
                                    if (bucket[i].equals(nonce)) {
                                        peer = PeerPool.getInstance().getPeerList().get(i);
                                        break;
                                    }
                                }
                                reloadFromPeer(peer);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }

            if ("CONFIRMED".equals(confirmation)) {
                for (Transaction tx : block.getLiveTransactions()) {
                    tx.setBlockHash(blockHash);
                    tx.removeUTXO();
                    tx.addUTXO();
                }
                synchronized (TransactionPool.getInstance()) {
                    UTXOPool.getInstance().save();
                    TransactionPool.getInstance().save();
                }
            }
            synchronized (BlockChain.getInstance()) {
                BlockChain.getInstance().add(block);
                BlockChain.getInstance().save();
            }
            broadCastedBlockMemPool.remove(blockHash);
            CENTER.save();

            try {
                List<Transaction> tempList = new LinkedList();
                for (Transaction trans : PEERBLOCK.getLiveTransactions()) {
                    for (Transaction tx : MINER.newNoBlockTX) {
                        if (trans.hash().equals(tx.hash())) {
                            tempList.add(tx);
                        }
                    }
                }
                MINER.newNoBlockTX.removeAll(tempList);
            } catch (NullPointerException ne) {
                MINER.newNoBlockTX.clear();
            }
            VALIDPEERBLOCK = false;
            PEERBLOCK = null;
        }
        if (isMyBlock) {
            CURRENTBLOCK = null;
        }
        return confirmation;
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
        //MINER.receiveTransaction(MINER.getAccount().prepareTX(COINBASE));
    }

    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("center.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);
            os.close();
            fs.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ERROR : Could not save Center");
        }
    }

    public Object hit(Object obj, Peer peer, boolean wait,
            Object[] bucket, int index, CountDownLatch latch) throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    bucket[index] = new Client(peer.getHost(), peer.getPort()).run(obj, wait, bucket, index);
                } catch (IOException ex) {
                }
                latch.countDown();
            }
        }.start();

        return null;
    }

    public void hitPeerNoWait(final Object obj, final Peer peer) throws IOException {
        new Client(peer.getHost(), peer.getPort()).run(obj, false);
    }

    public Object hitPeerWait(final Object obj, final Peer peer) throws IOException {
        return new Client(peer.getHost(), peer.getPort()).run(obj, true);
    }

    public void hitMultiplePeerNoWait(final Object obj) throws IOException {
        for (Peer peer : PeerPool.getInstance().getPeerList()) {
            hitPeerNoWait(obj, peer);
        }
    }

    public Object[] hitMultiplePeerWait(final Object obj) throws NullPointerException,
            InterruptedException,
            IOException {
        LinkedList<Peer> peerList = PeerPool.getInstance().getPeerList();
        if (peerList.isEmpty()) {
            throw new NullPointerException();
        }
        final CountDownLatch latch = new CountDownLatch(peerList.size());
        int index = 0;
        final Object[] bucket = new Object[peerList.size()];
        for (Peer peer : peerList) {
            hit(obj, peer, true, bucket, index, latch);
            index++;
        }
        latch.await();
        return bucket;
    }

    public class Client {

        Socket client;

        public Client(String host, int port) throws IOException {
            client = new Socket(InetAddress.getByName(host), port);
//            client = new Socket(host, port);
            client.setSoTimeout(20000);
            System.out.println("client connected");
        }

        public Object run(final Object msg, final boolean wait) {
            return run(msg, wait, null, 0);
        }

        public Object run(final Object msg, final boolean wait, final Object[] returnObjects, final int index) {
            Object object = null;
            ObjectOutputStream outStream = null;
            ObjectInputStream inStream = null;
            try {
                outStream = new ObjectOutputStream(client.getOutputStream());
                outStream.writeObject(msg);
                outStream.flush();
                if (wait) {
                    inStream = new ObjectInputStream(client.getInputStream());
                    object = inStream.readObject();
                    if (returnObjects != null) {
                        returnObjects[index] = object;
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                return null;
            } finally {
                try {
                    if (outStream != null) {
                        outStream.close();
                    }
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (client != null) {
                        client.close();
                    }
                    System.out.println("client closed");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return object;
        }
    }

    private class Server implements Runnable {

        private final ServerSocket server;

        private Server(ServerSocket server) {
            this.server = server;
        }

        private class ClientHandler implements Runnable {

            Socket sock;
            ObjectOutputStream outStream;
            ObjectInputStream inStream;

            private ClientHandler(Socket socket) {
                try {
                    sock = socket;
                    sock.setSoTimeout(20000);
                    outStream = new ObjectOutputStream(sock.getOutputStream());
                    inStream = new ObjectInputStream(sock.getInputStream());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void run() {
                try {
                    Object obj = inStream.readObject();
                    if (obj == null) {
                        return;
                    }
                    if (obj instanceof String) {
                        String msg = (String) obj;
                        System.out.println("server : " + msg);
                        switch (msg.split("-")[0]) {
                            case "getChain":
                                synchronized (BlockChain.getInstance()) {
                                    outStream.writeObject(BlockChain.getInstance());
                                }
                                break;
                            case "getTXPool":
                                synchronized (TransactionPool.getInstance()) {
                                    outStream.writeObject(TransactionPool.getInstance());
                                }
                                break;
                            case "getUTXOPool":
                                synchronized (TransactionPool.getInstance()) {
                                    outStream.writeObject(UTXOPool.getInstance());
                                }
                                break;
                            case "getCollectiveNonce":
                                synchronized (BlockChain.getInstance()) {
                                    outStream.writeObject(BlockChain.getInstance()
                                            .getCollectiveNonce().toString());
                                }
                                break;
                            case "getPeerPool":
                                synchronized (PeerPool.getInstance()) {
                                    outStream.writeObject(PeerPool.getInstance());
                                }
                                break;
                            case "addMeasPeer":
                                synchronized (PeerPool.getInstance()) {
                                    outStream.writeObject(PeerPool.getInstance()
                                            .addPeer(sock.getInetAddress().getHostAddress(), Integer.parseInt(msg.split("-")[1])));
                                }
                                break;
                            default:
                                break;
                        }
                    } else if (obj instanceof Transaction) {
                        System.out.println("Transaction Received");
                        ((Transaction) obj).display();
                        outStream.writeObject("OK");
                        broadcastTransaction((Transaction) obj);
                    } else if (obj instanceof Block) {
                        System.out.println("Block Received");
                        Block newIncomingBlock = (Block) obj;
                        newIncomingBlock.liveDisplay();
                        try {
                            if (CURRENTBLOCK.getNonce().compareTo(newIncomingBlock.getNonce()) >= 0) {
                                outStream.writeObject("UNCONFIRMED");
                            }
                        } catch (NullPointerException ne) {
                            outStream.writeObject(broadcastBlock(newIncomingBlock, false));
                        }
                    } else {
                        System.out.println("here comes unrecognized");
                        outStream.writeObject("OK");
                    }
                    outStream.flush();
                } catch (IOException | ClassNotFoundException | ConcurrentModificationException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        outStream.close();
                        inStream.close();
                        sock.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                System.out.println("server started at port: " + server.getLocalPort());
                while (OPEN) {
                    Socket sock = server.accept();
                    if (OPEN) {
                        new Thread(new ClientHandler(sock)).start();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
