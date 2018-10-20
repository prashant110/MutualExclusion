/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

import synchronization.Lock;
import synchronization.LockTest;
import util.PortAddr;
import util.Symbols;
import util.Util;
/**
 *
 * @author Prashant
 */
public class Connector {
    
    ServerSocket                listener;  
    Socket []                   link;
    Name                        name;
    BufferedReader[]            dataIn;
    PrintWriter[]               dataOut;
    String                      baseName;
    int                         myId;
    Lock                        process;

    
    
    public void Connect(String basename, int myId, int numProc, int appPort, BufferedReader[] dataIn, PrintWriter[] dataOut) throws Exception 
    {
        name = new Name();
        link = new Socket[numProc];
        int localport = appPort > 0 ? appPort : getLocalPort(myId);
        listener = new ServerSocket(localport);
        this.dataIn = dataIn;
        this.dataOut = dataOut;
        baseName = basename;
        
        /* register in the name server */
        name.insertName(basename + myId, LockTest.host, localport,myId);
       
        
            for (int i = 0; i < myId; i++) 
            {
                Socket s = listener.accept();
                Util.println("smaller "+i + " "+s.toString());
                BufferedReader dIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String getline = dIn.readLine();
                StringTokenizer st = new StringTokenizer(getline);
                int hisId = Integer.parseInt(st.nextToken());
                int destId = Integer.parseInt(st.nextToken());
                String tag = st.nextToken();
                if (tag.equals("hello")) 
                {
                    link[hisId] = s;
                    dataIn[hisId] = dIn;
                    dataOut[hisId] = new PrintWriter(s.getOutputStream());
                }
            }
            /* contact all the bigger processes */
            for (int i = myId + 1; i < numProc; i++) 
            {
                PortAddr addr;
                do
                {
                    addr = name.searchName(basename + i);
                    Thread.sleep(100);
                } while (addr.getPort() == -1);
                
                link[i] = new Socket(addr.getHostName(), addr.getPort());
                Util.println( "bigger " + i + " " +link[i].toString());
                dataOut[i] = new PrintWriter(link[i].getOutputStream());
                dataIn[i] = new BufferedReader(new
                InputStreamReader(link[i].getInputStream()));
                /* send a hello message to P_i */
                dataOut[i].println(myId +" "+ i +" "+ "hello" + " " + "null");
                dataOut[i].flush();
            }
        
    }
    
    public void setProcess(Lock process) {
        this.process = process;
    }
    
    int getLocalPort(int id) 
    { 
        return Symbols.NAMESERVERPORT + 10 + id; 
    }
    public void closeSockets()
    {
        try {
            listener.close();
            for (int i=0;i<link.length; i++) 
                link[i].close();
        } catch (Exception e) {
            Util.println("Exception in connector close sockets"+e.getMessage(),e);
        }
    }
    public void closeSocket(int processID)
    {
        try {
            link[processID].close();
        } catch (IOException ex) {
           Util.println("Exception in connector close socket socket closed for "+processID,ex);
        }
    }
    
}