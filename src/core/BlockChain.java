/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public final class BlockChain {

    private final LinkedList<Block> chain;
    private static final BlockChain BLOCKCHAIN = new BlockChain();
    
    private BlockChain(){
        chain = new LinkedList();
    }
    
    public static BlockChain getInstance(){
        return BLOCKCHAIN;
    }        

    public boolean add(Block b) {
        return chain.add(b);
    }

    public void display() {
        for (Block block : chain) {
            System.out.println();
            block.display();
            System.out.println();
        }
    }
    
    public boolean isEmpty(){
        return chain.isEmpty();
    }
    
    public Block getLast(){
        return chain.getLast();
    }
}
