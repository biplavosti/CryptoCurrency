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
import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class UnspentTX {
    private final LinkedList<UTXO> txOutList;
    private static UnspentTX UNSPENTTXOUT;
    
    private UnspentTX(){
        txOutList = new LinkedList();
    }
    
    public static UnspentTX getInstance() {
        if (UNSPENTTXOUT == null) {
            try {
                FileInputStream fs = new FileInputStream("utxo.ser");
                ObjectInputStream os = new ObjectInputStream(fs);
                UNSPENTTXOUT = (UnspentTX) os.readObject();
            } catch (IOException | ClassNotFoundException e) {
                UNSPENTTXOUT = new UnspentTX();
            }
        }
        return UNSPENTTXOUT;
    }
    
    public void addUTXO(UTXO utxo){
        txOutList.add(utxo);
        save();
    }
    
    public void removeUTXO(UTXO utxo){
        txOutList.remove(utxo);
        save();
    }
    
    public LinkedList<UTXO> getList(){
        return txOutList;
    }
    
    public void save() {
        try {
            FileOutputStream fs = new FileOutputStream("utxo.ser");
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(this);            
            os.close();
        } catch (IOException ex) {
            System.out.println("ERROR : Could not save UTXO");
        }
    }
}
