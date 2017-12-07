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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

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
    private int thisServerPort;
    private int peerServerPort;
    private transient LinkedList<String> broadCastedTXMemPool;
    private transient LinkedList<String> broadCastedBlockMemPool;
    public transient LinkedList<Transaction> newNoBlockTX = new LinkedList();
    public transient LinkedList<String> minedBlocksHash = new LinkedList();
    public transient volatile static Block CURRENTBLOCK;
    public transient volatile static boolean VALIDPEERBLOCK = false;

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
                Scanner scan = new Scanner(System.in);
                System.out.println("enter this server port :");
                CENTER.thisServerPort = scan.nextInt();
                System.out.println("enter peer server port :");
                CENTER.peerServerPort = scan.nextInt();
            }
            CENTER.COINBASE = 25;
            OPEN = true;
            CENTER.newNoBlockTX = new LinkedList();
            new Thread(CENTER.new Server(CENTER.thisServerPort)).start();
            System.out.println("Center Started");
            System.out.println("this server port : " + CENTER.thisServerPort);
            System.out.println("peer server port : " + CENTER.peerServerPort);
            CENTER.MINER.start();
            System.out.println("Miner Started");
        }
        return CENTER;
    }

    public static void setup() {
        getInstance();
        UTXOPool.getInstance();
        BlockChain.getInstance();
    }

    public static void reloadFromPeer() throws IOException {
        UTXOPool.getPoolFromPeer();
        BlockChain.getChainFromPeer();
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
                hitServer(transaction, false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            synchronized (this) {
                broadCastedTXMemPool.add(hash);
                newNoBlockTX.add(transaction);
            }
        }
    }

    public String broadcastBlock(Block block, boolean isMyBlock) {
        String confirmation = "CONFIRMED";
        String blockHash = block.getBlockHash();
        if (isMyBlock && VALIDPEERBLOCK) {
            System.out.println("wasted one block just before broadcast");
            return "UNCONFIRMED";
        }
        synchronized (MINER) {
            if (!block.confirm()) {
                System.out.println("ERROR : " + (isMyBlock ? "MY " : "PEER ") + "Block is not verified : " + blockHash);
                return "UNCONFIRMED";
            }
            if (!isMyBlock) {
                VALIDPEERBLOCK = true;
            }
            if (!broadCastedBlockMemPool.contains(blockHash)) {
                broadCastedBlockMemPool.add(blockHash);
                if (!isMyBlock) {
                    try {
                        hitServer(block, false);
                    } catch (IOException ex) {
                    }
                    confirmation = "CONFIRMED";
                } else {
                    CURRENTBLOCK = block;
                    try {
                        confirmation = (String) hitServer(block, true);
                    } catch (IOException ex) {
                        confirmation = "CONFIRMED";
                    }
                    if ("UNCONFIRMED".equals(confirmation)) {
                        BigInteger myCollectiveNonce = BlockChain.getInstance().getCollectiveNonce();
                        BigInteger peerCollectiveNonce;
                        try {
                            peerCollectiveNonce = (BigInteger) hitServer("getCollectiveNonce", true);
                        } catch (IOException e) {
                            peerCollectiveNonce = BigInteger.ZERO;
                        }
                        if (myCollectiveNonce.compareTo(peerCollectiveNonce) < 0) {
                            try {
                                reloadFromPeer();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                synchronized (this) {
                    if ("CONFIRMED".equals(confirmation)) {
                        for (Transaction tx : block.getLiveTransactions()) {
                            tx.setBlockHash(blockHash);
                            tx.removeUTXO();
                            tx.addUTXO();
                        }
                        UTXOPool.getInstance().save();
                        TransactionPool.getInstance().save();
                    }
                }
                synchronized (BlockChain.getInstance()) {
                    BlockChain.getInstance().add(block);
                    BlockChain.getInstance().save();
                }
                VALIDPEERBLOCK = false;
            }
            if (isMyBlock) {
                CURRENTBLOCK = null;
            }
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
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ERROR : Could not save Center");

        }
    }

    public Object hitServer(final Object obj, final boolean wait) throws IOException {
        if (obj instanceof String) {
            final String cmd = (String) obj;
            if ("shutdown".equals(cmd)) {
                return new Client("localhost", thisServerPort).run(this, wait);
            }
        }
        return new Client("localhost", peerServerPort).run(obj, wait);
    }

    public class Client {

        Socket client;

        public Client(String host, int port) throws IOException {
            client = new Socket(host, port);
            System.out.println("client connected");
        }

        public Object run(final Object msg, final boolean wait) {
            Object object = null;
            ObjectOutputStream outStream;
            ObjectInputStream inStream;
            try {
                outStream = new ObjectOutputStream(client.getOutputStream());
                outStream.writeObject(msg);
                outStream.flush();
                if (wait) {
                    inStream = new ObjectInputStream(client.getInputStream());
                    object = inStream.readObject();
                }
            } catch (IOException | ClassNotFoundException ex) {
                return null;
            }
//            finally {
//                try {
//                    if (outStream != null) {
//                        outStream.close();
//                    }
//                    if (inStream != null) {
//                        inStream.close();
//                    }
//                    if (client != null) {
//                        client.close();
//                    }
//                    System.out.println("client closed");
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
            return object;
        }
    }

    private class Server implements Runnable {

        private final int port;

        private Server(int port) {
            this.port = port;
        }

        private class ClientHandler implements Runnable {

            Socket sock;
            ObjectOutputStream outStream;
            ObjectInputStream inStream;

            private ClientHandler(Socket socket) {
                try {
                    sock = socket;
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
                        synchronized (BlockChain.getInstance()) {
                            switch (msg) {
                                case "getChain":
                                    outStream.writeObject(BlockChain.getInstance());
                                    break;
                                case "getTXPool":
                                    outStream.writeObject(TransactionPool.getInstance());
                                    break;
                                case "getUTXOPool":
                                    outStream.writeObject(UTXOPool.getInstance());
                                    break;
                                case "getCollectiveNonce":
                                    outStream.writeObject(BlockChain.getInstance()
                                            .getCollectiveNonce());
                                    break;
                                default:
                                    break;
                            }
                        }
                    } else if (obj instanceof Transaction) {
                        System.out.println("Transaction Received");
                        outStream.writeObject("OK");
                        broadcastTransaction((Transaction) obj);
                    } else if (obj instanceof Block) {
                        System.out.println("Block Received");
                        Block newIncomingBlock = (Block) obj;
//                        System.out.println("----incoming BLOCK----start------");
                        newIncomingBlock.display();
//                        System.out.println("----incoming BLOCK----end------");
                        try {
//                            System.out.println("----CURRENT BLOCK----start------");
//                            CURRENTBLOCK.display();
//                            System.out.println("----CURRENT BLOCK----end------");
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
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
//                finally {
//                    try {
//                        outStream.close();
//                        inStream.close();
//                        sock.close();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
            }
        }

        @Override
        public void run() {
            try {
                try (ServerSocket server = new ServerSocket(port)) {
                    System.out.println("server started at port: " + port);
                    while (OPEN) {
                        Socket sock = server.accept();
//                        if (!OPEN) {
//                            sock.close();
//                            server.close();
//                            break;
//                        }
                        if (OPEN) {
                            new Thread(new ClientHandler(sock)).start();
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
