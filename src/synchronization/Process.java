/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import java.io.IOException;

import connectivity.Linker;
import msg.Msg;
import msg.MsgHandler;
import util.Util;

/**
 *
 * @author Prashant
 */
public class Process implements MsgHandler 
{
    static int N, myId;
    static Linker comm;

    public Process(Linker initComm) 
    {
        comm = initComm;
        myId = comm.getMyId();
        N = comm.getNumProc();
    }

    
    @Override
    public synchronized void handleMsg(Msg m, int src, String tag) 
    {
        //Util.println(" Message:: "+m.getMessage() + " :: "+m.getTag());
    }
    public void sendMsg(int destId, String tag, String msg) 
    {
        //Util.println("Sending msg to " + destId + ":" +tag + " " + msg);
        comm.sendMsg(destId, tag, msg);
    }
    public void sendMsg(int destId, String tag, String msg, byte[] signature)  
    {
        //Util.println("Sending msg to " + destId + ":" +tag + " " + msg);
        if(signature == null)
            comm.sendMsg(destId, tag, msg);
        else
            comm.sendMsg(destId, tag, msg,signature);
    }
    public void sendMsg(int destId, String tag, int msg) 
    {
        sendMsg(destId, tag, String.valueOf(msg)+" ");
    }
    public void sendMsg(int destId, String tag, int msg1, int msg2) 
    {
        sendMsg(destId,tag,String.valueOf(msg1) + " "+ String.valueOf(msg2) + " ");
    }
    public void sendMsg(int destId, String tag) 
    {
        sendMsg(destId, tag, " 0 ");
    }
   
    
    public Msg receiveMsg(int fromId) 
    {
        try {
            return comm.receiveMsg(fromId);
        } catch (IOException e){
            //Util.println("Exception in process:: because of "+fromId + " :: " +e);
            Util.println(-1, -1, "crash", String.valueOf(fromId), "Exception in process:: because of "+fromId + " :: " +e);
            comm.close(fromId);
            return null;
        }
    }
    public synchronized void myWait() 
    {
        try 
        {
            wait();
        } 
        catch (InterruptedException e) 
        {
            Util.println("Exception in process mywait "+e,e);
        }
    }
    
}

