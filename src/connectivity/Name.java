/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

import util.PortAddr;
import util.Symbols;

/**
 *
 * @author Prashant
 */
public class Name 
{
    BufferedReader din;
    PrintStream pout;
    
    public void getSocket() throws IOException 
    {
        Socket server = new Socket(Symbols.NAMESERVER,  Symbols.NAMESERVERPORT);
        din = new BufferedReader(new InputStreamReader(server.getInputStream()));
        pout = new PrintStream(server.getOutputStream());
    }
    
    public int insertName(String name, String hname, int portnum, int id)throws IOException 
    {
        // add the entry in nameserver
        getSocket();
        pout.println("insert " + name + " " + hname + " " + portnum + " "+id);
        pout.flush();
        return Integer.parseInt(din.readLine());
    }
    
    public PortAddr searchName(String name) throws IOException 
    {
        // search name server by name
        getSocket();
        pout.println("search " + name);
        pout.flush();
        String result = din.readLine();
        StringTokenizer st = new StringTokenizer(result);
        int portnum = Integer.parseInt(st.nextToken());
        String hname = st.nextToken();
        return new PortAddr(hname, portnum);
    }
    
    public void removeName(int id) throws IOException
    {
        //delete entry from nameserver
        getSocket();
        pout.println("remove " + id);
        pout.flush();
    }
}
