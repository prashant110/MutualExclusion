/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author Prashu
 */
public class PortAddr {
    String hostname;
    int portnum;
    public PortAddr(String s, int i) {
        hostname = s;
        portnum = i;
    }
    public String getHostName() {
        return hostname;
    }
    public int getPort() {
        return portnum;
    }
}
