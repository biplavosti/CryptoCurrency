/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.common.Transaction;
import core.common.CryptoService;
import java.math.BigInteger;
import java.util.List;

/**
 *
 * @author Biplav
 */
public class HelperService {

    public static BigInteger merkleRoot(BigInteger[] codes) {
        if (codes.length < 1) {
            return BigInteger.valueOf(0);
        } else if (codes.length == 2) {
            return CryptoService.hash(codes[0] + "" + codes[1]);
        } else if (codes.length == 1) {
            return CryptoService.hash(codes[0] + "" + codes[0]);
        } else {
            BigInteger hash1 = BigInteger.valueOf(0);
            BigInteger hash2;
            int j = 0;
            BigInteger codes1[] = new BigInteger[codes.length / 2 + 1];
            boolean pair = false;
            for (int i = 0; i < codes.length; i++) {
                if (i % 2 == 0) {
                    hash1 = CryptoService.hash(codes[i] + "");
                    pair = false;
                } else {
                    hash2 = CryptoService.hash(codes[i] + "");
                    codes1[j++] = CryptoService.hash(hash1 + "" + hash2);
                    pair = true;
                }
            }
            if (!pair) {
                codes1[j] = CryptoService.hash(hash1 + "" + hash1);
            }
            return merkleRoot(codes1);
        }
    }

    public static int getNoOfCoinbaseTX(List<Transaction> transactions) {
        int noOfCoinbaseTX = 0;
        for (Transaction tx : transactions) {
            if (tx.isCoinBase()) {
                noOfCoinbaseTX++;
                if (noOfCoinbaseTX > 1) {
                    break;
                }
            }
        }
        return noOfCoinbaseTX;
    }
}
