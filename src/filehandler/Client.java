/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filehandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Prashant
 */
public class Client {
    
 public static Socket getConnection(String host,int port) throws IOException
    {
        Socket s = new Socket(host,port);
        return s;
    }
    
    public static void sendMsg(Socket s, String msg) throws IOException
    {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        PrintWriter out = new PrintWriter(dos, true);
        out.println(msg);
    }
    
    public static void closeSocket(Socket s) throws IOException
    {
        s.close();
    }
    
    }

