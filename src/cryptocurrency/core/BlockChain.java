/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency.core;

import java.util.LinkedList;

/**
 *
 * @author Biplav
 */
public class BlockChain {

    public LinkedList<Block> blockChain;
    
    public BlockChain(){
        blockChain = new LinkedList();
    }
    
    public BlockChain(BlockChain bc){
        this();
        blockChain.addAll(bc.blockChain);
    }

    public boolean add(Block b) {
        return blockChain.add(b);
    }

    public void display() {
        for (Block block : blockChain) {
            System.out.println();
            block.display();
            System.out.println();
        }
    }
}
