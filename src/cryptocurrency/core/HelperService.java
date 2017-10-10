/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptocurrency.core;

import cryptography.Main;
import java.math.BigInteger;

/**
 *
 * @author Biplav
 */
public class HelperService {
    public static BigInteger merkleRoot(BigInteger[] codes) {
        if (codes.length < 1) {
            return BigInteger.valueOf(0);
        } else if (codes.length == 2) {
            return Main.hash(codes[0] + "" + codes[1]);
        } else if (codes.length == 1) {
            return Main.hash(codes[0] + "" + codes[0]);
        } else {
            BigInteger hash1 = BigInteger.valueOf(0);
            BigInteger hash2;
            int j = 0;
            BigInteger codes1[] = new BigInteger[codes.length / 2 + 1];
            boolean pair = false;
            for (int i = 0; i < codes.length; i++) {
                if (i % 2 == 0) {
                    hash1 = Main.hash(codes[i] + "");
                    pair = false;
                } else {
                    hash2 = Main.hash(codes[i] + "");
                    codes1[j++] = Main.hash(hash1 + "" + hash2);
                    pair = true;
                }
            }
            if (!pair) {
                codes1[j] = Main.hash(hash1 + "" + hash1);
            }
            return merkleRoot(codes1);
        }
    }
}
