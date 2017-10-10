/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wallet;

import cryptocurrency.core.BlockChain;
import cryptocurrency.Center;
import cryptocurrency.Miner;
import cryptocurrency.core.UTXO;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Biplav
 */
public class UserInterface {

    private static final List<Account> ACCOUNTS = new LinkedList();
    private static final Scanner INPUT = new Scanner(System.in);

    private static void door() {
        int option;
        boolean completed = false;
        while (!completed) {
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  0 : EXIT");
            System.out.println("ZZ  1 : CREATE NEW ACCOUNT");
            if (!ACCOUNTS.isEmpty()) {
                System.out.println("ZZ  2 : ENTER INTO EXISTING ACCOUNT");
            }
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
                        display(ACCOUNTS.get(INPUT.nextInt()));
                    }
                    break;
                case 0:
                    completed = true;
                    break;
                case 1:
                    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
                    System.out.println("ZZ CREATE ACCOUNT");
                    System.out.println("ZZ");
                    System.out.println("ZZ Name : ");
                    INPUT.nextLine();
                    String name = INPUT.nextLine();
                    Account acc = new Account(name);
                    ACCOUNTS.add(acc);
                    Center.registerAccount(acc);
                    break;
            }

        }
    }

    private static void display(Account account) {
        int option;
        boolean completed = false;
        while (!completed) {
            double numberofCoins = 0.0;
            for(UTXO utxo : Center.getUTXO()){
                if(account.getAddress().equals(utxo.getReceiverAddress())){
                   numberofCoins += utxo.getCoin();
               } 
            }
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  Name      : " + account.getName());
            System.out.println("ZZ  Address   : " + account.getAddress().toString());
            System.out.println("ZZ  Coin      : " + numberofCoins);
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
            }
        }
    }

    private static void sendInteface(Account account) {
        List<Account> users = Center.getUsers();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Receipents");
        for (int i = 0; i < users.size(); i++) {
            System.out.println("ZZ    " + i + " : " + users.get(i).getName());
        }
        System.out.println("ZZ");
        System.out.println("Please choose a receipent : ");
        int receipent = INPUT.nextInt();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Input number of Coins : ");
        System.out.println("ZZ");
        int coins = INPUT.nextInt();
        account.sendTx(account.getAddress() + ":" + users.get(receipent).getAddress() + ":" + coins);
        System.out.println();
        System.out.println("Block Chain->");
        Center.getBlockChain().display();
        System.out.println("<-Block Chain");
        System.out.println();
    }
    
    

    public static void main(String[] args) {
        Account user1 = new Account("user 1");
        Center.registerAccount(user1);
        BlockChain blockChain = new BlockChain(Center.getBlockChain());
        Center.registerMiner(new Miner(user1.getName(), blockChain));
        
        door();
    }
}
