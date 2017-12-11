/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Center;
import core.common.Transaction;
import java.math.BigInteger;
import core.common.Account;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class Miner implements Serializable, Runnable {

    private final Account account;
    private transient Thread minerThread;
    public transient List<Transaction> newNoBlockTX;

    public Miner(Account account) {
        this.account = account;
    }

    public void start() {
        newNoBlockTX = new LinkedList();
        minerThread = new Thread(this);
        minerThread.start();
    }

    public Account getAccount() {
        return account;
    }

    public Block receiveTransaction(List<Transaction> transactions) {
        return processTransaction(transactions);
    }

    public Block processTransaction(List<Transaction> transactions) {
        Block block;
        BlockChain blockChain = BlockChain.getInstance();
        int noOfCoinbaseTX = HelperService.getNoOfCoinbaseTX(transactions);
        System.out.println("no of coinbase tx : " + noOfCoinbaseTX);
        if (noOfCoinbaseTX == 0) {
            transactions.add(account.prepareTX(Center.getInstance().COINBASE));
        } else if (noOfCoinbaseTX > 1) {
            return null;
        }
        if (blockChain.isEmpty()) {
            block = new Block("0", transactions);
        } else {
            block = new Block(blockChain.getLast().getBlockHash(), transactions);
        }
        BigInteger nonce;
        BigInteger limit = BigInteger.valueOf(1999999999);
        if (!Center.OPEN) {
            return block;
        }
        if (Center.VALIDPEERBLOCK) {
            return block;
        }
        for (nonce = BigInteger.ZERO; nonce.compareTo(limit) < 0;) {
            block.setNonce(nonce);
            if (!Center.OPEN || Center.VALIDPEERBLOCK || block.verify()) {
                break;
            }
            nonce = nonce.add(BigInteger.ONE);
        }
        return block;
    }

    @Override
    public void run() {
        while (Center.OPEN) {
            if (!Center.VALIDPEERBLOCK) {
                newNoBlockTX.addAll(Center.getInstance().submitNewTXList());
                System.out.println("miner transactions ------ starts-------");
                for (Transaction tx : newNoBlockTX) {
                    tx.display();
                }
                System.out.println("miner transactions ------ ends-------");
                if (!Center.OPEN) {
                    return;
                }
                if (Center.VALIDPEERBLOCK) {
                    continue;
                }
                Block newFoundBlock = receiveTransaction(newNoBlockTX);
                if (!newFoundBlock.verify()) {
                    continue;
                }
                if (Center.VALIDPEERBLOCK) {
                    continue;
                }
                System.out.println("------New BLOCK MINED-----START---");
                newFoundBlock.liveDisplay();
                System.out.println("------New BLOCK MINED-----END---");
                if (!Center.OPEN) {
                    return;
                }
                if (Center.VALIDPEERBLOCK) {
                    continue;
                }

                Center.getInstance().broadcastBlock(newFoundBlock, true);
            }
        }
    }
}
