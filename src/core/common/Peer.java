/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.common;

import java.io.Serializable;

/**
 *
 * @author Biplav
 */
public class Peer implements Serializable{
    private final String host;
    private final int port;

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
    public void display(){
        System.out.println("PEER : Host : " + host +" : Port : " + port);
    }
}
