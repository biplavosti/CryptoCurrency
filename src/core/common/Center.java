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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Biplav
 */
public class Center implements Serializable {

    private final List<Account> USERS;
    private final Miner MINER;
    public transient double COINBASE = 25;
    private static Center CENTER;
    public int thisServerPort;
    public int peerServerPort;
    public transient volatile static Boolean OPEN = false;
    public transient LinkedList<String> txMemPool;
    public transient LinkedList<String> blockMemPool;

    private Center() {
        USERS = new ArrayList();
        MINER = new Miner(createAccount("Miner"));
        txMemPool = new LinkedList();
        blockMemPool = new LinkedList();
    }

    public static Center getInstance() {
        if (CENTER == null) {
            try {
                FileInputStream fs = new FileInputStream("center.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                CENTER = (Center) os.readObject();
                CENTER.txMemPool = new LinkedList();
                CENTER.blockMemPool = new LinkedList();
                System.out.println("Center Started");
                System.out.println("this server port : " + CENTER.thisServerPort);
                System.out.println("peer server port : " + CENTER.peerServerPort);
            } catch (IOException | ClassNotFoundException e) {
                CENTER = new Center();
                Scanner scan = new Scanner(System.in);
                System.out.println("enter this server port :");
                CENTER.thisServerPort = scan.nextInt();
                System.out.println("enter peer server port :");
                CENTER.peerServerPort = scan.nextInt();
            }
        }
        CENTER.COINBASE = 25;
        return CENTER;
    }

    public List<Account> getUsers() {
        OPEN = true;
        new Thread(new Server()).start();
        return USERS;
    }

    public Miner getMiner() {
        return MINER;
    }

    public void broadcastTransaction(List<Transaction> transaction) {
        MINER.receiveTransaction(transaction);
    }

    public void broadcastTransaction(Transaction transaction) {
        String hash = transaction.hash();
        if (!txMemPool.contains(hash)) {
            System.out.println("new transaction");
            txMemPool.add(hash);
            try {
                new Client(new Socket("localhost", peerServerPort)).run(transaction);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MINER.receiveTransaction(transaction);
        }
    }

    public void broadcastBlock(Block block) {
        String hash = block.getBlockHash();
        if (!blockMemPool.contains(hash)) {
            blockMemPool.add(hash);
            try {
                new Client(new Socket("localhost", peerServerPort)).run(block);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            MINER.receiveBlock(block);
        }
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
        MINER.receiveTransaction(MINER.getAccount().prepareTX(COINBASE));
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

    public class Client {

        Socket client;
        ObjectOutputStream outStream;
        ObjectInputStream inStream;

        public Client(Socket sock) {
            client = sock;
            try {
                outStream = new ObjectOutputStream(client.getOutputStream());
                inStream = new ObjectInputStream(client.getInputStream());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public Object run(Object msg) {
            Object object = null;
            try {
                System.out.println("client connected");

                outStream.writeObject(msg);
                outStream.flush();
                object = inStream.readObject();

            } catch (IOException | ClassNotFoundException ex) {
                return null;
            } finally {
                try {
                    outStream.close();
                    inStream.close();
                    client.close();
                    System.out.println("client closed");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return object;
        }
    }

    private class Server implements Runnable {

        public class ClientHandler implements Runnable {

            Socket sock;
            ObjectOutputStream outStream;
            ObjectInputStream inStream;

            public ClientHandler(Socket socket) {
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
                    if (obj instanceof String) {
                        String msg = (String) obj;
                        System.out.println(msg);
                        switch (msg) {
                            case "getChain":
                                outStream.writeObject(BlockChain.getInstance());
                                break;
                            case "getTXPool":
                                System.out.println("sending transaction pool");
                                outStream.writeObject(TransactionPool.getInstance());
                                break;
                            case "getUTXOPool":
                                outStream.writeObject(UTXOPool.getInstance());
                                break;
                            default:
                                break;
                        }
                    } else if (obj instanceof Transaction) {
                        System.out.println("transaction received");
                        outStream.writeObject("OK");
                        Transaction tx = (Transaction) obj;
                        tx.display();
                        broadcastTransaction(tx);
                    } else if (obj instanceof Block) {
                        System.out.println("block received");
                        outStream.writeObject("OK");
                        Block bk = (Block) obj;
                        bk.display();
                        broadcastBlock(bk);
                    } else {
                        System.out.println("here comes unrecognized");
                        outStream.writeObject("OK");
                    }
                    outStream.flush();
                } catch (IOException | ClassNotFoundException ex) {
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
                try (ServerSocket server = new ServerSocket(thisServerPort)) {
                    System.out.println("server started at port: " + thisServerPort);
                    while (OPEN) {
                        Socket sock = server.accept();
                        if (!OPEN) {
                            sock.close();
                            server.close();
                            break;
                        }
                        Thread t = new Thread(new ClientHandler(sock));
                        t.start();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
