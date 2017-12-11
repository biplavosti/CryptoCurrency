/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.wallet;

import core.BlockChain;
import core.TransactionPool;
import core.common.Account;
import core.common.Center;
import core.common.Peer;
import core.common.PeerPool;
import core.common.UTXOPool;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Biplav
 */
public class UserInterface {

    private final Center center = Center.getInstance();
    private final List<Account> ACCOUNTS = center.getUsers();
    private static final Scanner INPUT = new Scanner(System.in);

    private void door() {
        int option;
        boolean completed = false;
        while (!completed) {
            System.out.println("Server : " + center.SELF.getHost() + " : " + center.SELF.getPort());
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  0 : EXIT");
            System.out.println("ZZ  1 : CREATE NEW ACCOUNT");
            if (!ACCOUNTS.isEmpty()) {
                System.out.println("ZZ  2 : ENTER INTO EXISTING ACCOUNT");
            }
            System.out.println("ZZ  3 : SHOW BLOCKCHAIN");
            System.out.println("ZZ  4 : SHOW TRANSACTION POOL");
            System.out.println("ZZ  5 : SHOW UTXO POOL");
            System.out.println("ZZ  6 : GET NEW PEER LIST");
            System.out.println("ZZ  7 : SHOW PEERS");
            System.out.println("ZZ");
            System.out.println("Please enter your choice : ");
            option = INPUT.nextInt();
            switch (option) {
                case 2:
                    if (!ACCOUNTS.isEmpty()) {
                        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
                        System.out.println("ZZ  Accounts : ");
                        for (int i = 0; i < ACCOUNTS.size(); i++) {
                            System.out.println("ZZ    " + i + " : " + ACCOUNTS.get(i).getName());
                        }
                        System.out.println("ZZ");
                        System.out.println("Please choose an account : ");
                        try {
                            display(ACCOUNTS.get(INPUT.nextInt()));
                        } catch (IndexOutOfBoundsException ae) {

                        }

                    }
                    break;
                case 0:
                    completed = true;
                    Center.OPEN = false;
                    center.save();
                    try {
                        center.hitPeerNoWait("shutdown", center.SELF);
                    } catch (IOException | NullPointerException ex) {
                    }
                    break;
                case 1:
                    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
                    System.out.println("ZZ CREATE ACCOUNT");
                    System.out.println("ZZ");
                    System.out.println("ZZ Name : ");
                    INPUT.nextLine();
                    String name = INPUT.nextLine();
                    center.createAccount(name);
                    break;
                case 3:
                    BlockChain.getInstance().display();
                    break;
                case 4:
                    TransactionPool.getInstance().display();
                    break;
                case 5:
                    UTXOPool.getInstance().display();
                    break;
                case 6: {
                    try {
                        PeerPool.getInstance().addAll((List<Peer>) Center.getInstance().hitPeerWait("getPeerList", PeerPool.getInstance().getPeerList().getFirst()));
                    } catch (IOException ex) {
                    }
                    break;
                }
                case 7:
                    PeerPool.getInstance().display();
                    break;
            }
        }
        System.out.println("THE END");
    }

    private void display(Account account) {
        int option;
        boolean completed = false;
        while (!completed) {
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  Name          : " + account.getName());
            System.out.println("ZZ  Address       : " + account.getAddress());
            System.out.println("ZZ  Coin          : " + account.getNumberofCoins());
            System.out.println("ZZ");
            System.out.println("ZZ");
            System.out.println("ZZ  Actions");
            System.out.println("ZZ    0 : BACK");
            System.out.println("ZZ    1 : SEND");
            System.out.println("ZZ");
            System.out.println();
            System.out.println("Please select an option : ");
            option = INPUT.nextInt();

            switch (option) {
                case 0:
                    completed = true;
                    break;

                case 1:
                    sendInteface(account);
                    break;

                default:
                    break;
            }
        }
    }

    private void sendInteface(Account account) {
        INPUT.reset();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Enter receipent address :");
        INPUT.nextLine();
        String receipent = INPUT.nextLine();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Input number of Coins : ");
        System.out.println("ZZ");
        double coins = INPUT.nextDouble();
        account.sendTx(coins, receipent, false);
    }

    public static void main(String[] args) {
        Center.setup();
        new UserInterface().door();
    }
}
