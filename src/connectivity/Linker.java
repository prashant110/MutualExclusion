/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.LinkedList;
import java.util.StringTokenizer;

import msg.Msg;
import synchronization.Lock;


/**
 *
 * @author Prashant
 * class is responsible for sending and receiving message from other nodes
 */
public class Linker 
{
    PrintWriter[]               dataOut;
    BufferedReader[]            dataIn;
    
    int                         myId, N;
    Connector                   connector;
    Lock                        process;
    public LinkedList<Integer>  neighbors = new LinkedList<>();
    public static final int     RIGHT     = 0;
    public static final int     DOWN      = 1;
    public static final int     TOP       = 2;
    public static final int     LEFT      = 3;
    
    
    public Linker(String basename, int id, int numProc, int appPort,String topology) throws Exception 
    {
        myId = id;
        N = numProc;
        dataIn = new BufferedReader[numProc];
        dataOut = new PrintWriter[numProc];
        // set neighbours of node
        Topology.readNeighbors(myId, N, neighbors,topology);
        connector = new Connector();
        // connect with other nodes
        connector.Connect(basename, myId, numProc, appPort, dataIn, dataOut);
     
    }
    
    
    public void setProcess(Lock process)
    {
        this.process = process;
        connector.setProcess(process);
    }
    
    public void sendMsg(int destId, String tag, String msg) 
    {     
        dataOut[destId].println(myId + " " + destId + " " + tag + " " + msg + "#");
        dataOut[destId].flush();
       
    }
    
    public void sendMsg(int destId, String tag, String msg, byte[] sign) 
    {
       int length = sign.length;
       String signature = Base64.getEncoder().encodeToString(sign);
       dataOut[destId].println(myId + " " + destId + " " + tag + " " + msg + "#"+ length + " " + signature +"#");
       dataOut[destId].flush();
    }
    
    public void sendMsg(int destId, String tag) 
    {
        sendMsg(destId, tag, " 0 ");
    }
    public void multicast(LinkedList<Integer> destIds, String tag, String msg)
    {
        for (int i=0; i<destIds.size(); i++) 
        {
            sendMsg(destIds.get(i), tag, msg);
        }
    }
    public Msg receiveMsg(int fromId) throws IOException  
    {        
        String getline = dataIn[fromId].readLine();
        StringTokenizer st = new StringTokenizer(getline);
        int srcId = Integer.parseInt(st.nextToken().trim());
        int destId = Integer.parseInt(st.nextToken().trim());
        String tag = st.nextToken().trim();
        String msg = st.nextToken("#");
        int signLength = 0;
        String signature = null;
        if(st.hasMoreTokens())
        {
            signLength = Integer.parseInt(st.nextToken(" ").trim().substring(1));
        }
            
        if(st.hasMoreTokens())
        {
            signature = st.nextToken("#").trim();
        }
            
        return new Msg(srcId, destId, tag, msg, signLength, signature);        
    }
    public int getMyId() 
    { 
        return myId; 
    }
    public int getNumProc() 
    { 
        return N; 
    }
    public void close() 
    {
        connector.closeSockets();
        
    }
    public void close(int processId)
    {
        connector.closeSocket(processId);
    }
    
    
}
