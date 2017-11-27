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
public class BlockChain {

    private LinkedList<Block> chain;
    
    public BlockChain(){
        chain = new LinkedList();
    }
    
    public BlockChain(BlockChain bc){
        this();
        chain.addAll(bc.chain);
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
