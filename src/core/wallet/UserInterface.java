/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.wallet;

import core.common.Account;
import core.common.Center;
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
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  0 : EXIT");
            System.out.println("ZZ  1 : CREATE NEW ACCOUNT");
            if (!ACCOUNTS.isEmpty()) {
                System.out.println("ZZ  2 : ENTER INTO EXISTING ACCOUNT");
            }
            System.out.println("ZZ  3 : MINE COINBASE TX");
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
                    center.save();
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
                    center.mineFirstCoin();
                    break;
            }

        }
    }

    private void display(Account account) {
        int option;
        boolean completed = false;
        while (!completed) {
            System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
            System.out.println("ZZ  Name          : " + account.getName());
            System.out.println("ZZ  Address       : " + account.getEncryptedAddress());
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
            }
        }
    }

    private void sendInteface(Account account) {
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Receipents");
        for (int i = 0; i < ACCOUNTS.size(); i++) {
            System.out.println("ZZ    " + i + " : " + ACCOUNTS.get(i).getName());
        }
        System.out.println("ZZ");
        System.out.println("Please choose a receipent : ");
        int receipent = INPUT.nextInt();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ");
        System.out.println("ZZ  Input number of Coins : ");
        System.out.println("ZZ");
        double coins = INPUT.nextDouble();
        account.sendTx(coins, ACCOUNTS.get(receipent), false);
    }

    public static void main(String[] args) {
        new UserInterface().door();
    }
}
