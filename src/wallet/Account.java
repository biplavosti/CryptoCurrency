/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wallet;

import cryptocurrency.Center;
import cryptocurrency.core.Transaction;
import cryptocurrency.core.UTXO;
import cryptography.Main;
import java.math.BigInteger;
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class Account {

    private final String name;
    private BigInteger address;

    public Account(String name) {
        this.name = name;
        address = Main.hash(name);
        this.sendTx(":" + address + ":" + 2);
        this.sendTx(":" + address + ":" + 3);
        this.sendTx(":" + address + ":" + 2);
    }

    public String getName() {
        return name;
    }

    public BigInteger getAddress() {
        return address;
    }

    public void setAddress(BigInteger address) {
        this.address = address;
    }

    public void display() {
        System.out.println("Name          -> " + name);
        System.out.println("Address       -> " + address);
    }

    public final void sendTx(String entry) {
        final String[] entries = entry.split(":");
        String senderAddress = entries[0];
        String receiverAddress = entries[1];
        double coin = Double.valueOf(entries[2]);        
        
        Transaction tx = new Transaction(coin + " coins transferred");
        if ("".equals(senderAddress)) {            
            tx.addOutput(coin, new BigInteger(receiverAddress));
        }else{ 
            double sum = 0.0;
            LinkedList<UTXO> inputsUtxo = new LinkedList();
            for(UTXO utxo : Center.getUTXO()){
               if(senderAddress.equals(utxo.getReceiverAddress().toString(0))){
                   sum+= utxo.getCoin();
                   inputsUtxo.add(utxo);
                   if(sum >= coin) break;
               } 
            }
            
            tx.addInput(inputsUtxo);
            tx.addOutput(coin, new BigInteger(receiverAddress));
            double change = sum - coin;
            if(change > 0){
               tx.addOutput(change, new BigInteger(senderAddress)); 
            }            
        }
        
        Center.broadcastTransaction(new Transaction[]{tx});
    }
}
