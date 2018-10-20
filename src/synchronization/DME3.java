/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import msg.Msg;
import util.Util;
import connectivity.Linker;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import msg.Converter;
import org.json.simple.JSONObject;

/**
 *
 * @author Prashant
 */


public class DME3 extends Process implements Lock
{
	
    private int         seqNo = 0;
    private Token2       token;
    private boolean     CsPermission = false;
    private Object      lockObj = new Object();
    private int[]       req ;
    private int         myCol;
    private String      direction;
    
    static ArrayList<Integer>    columnNeighbour ;
    
    public DME3(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        int sqrtN = (int)Math.sqrt(N);
        CsPermission = (myId == coordinator);
        token = Token2.getInstance(N);
        req = new int[sqrtN];
        myCol = myId % sqrtN;
        columnNeighbour = new ArrayList<>(sqrtN);
        
        int rows = myId / sqrtN;
        for(int k = 0; k < sqrtN; k++)
            columnNeighbour.add(rows * sqrtN + k);
        
        
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
        seqNo++;
        req[myCol] = seqNo; 
        //Util.println("..........requesting for cs...........");
        Util.println(myId, -1, "request_cs", String.valueOf(seqNo), "..........requesting for cs..........."+seqNo + " "+Arrays.toString(req));
        
        sendMessage("right","request",myId+"_"+seqNo);
        sendMessage("left","request",myId+"_"+seqNo);
        synchronized(lockObj)
        {
           while(!CsPermission)
            {
                //Util.println("waiting");
               try
               {
                   Util.println("goint to sleep");
                    lockObj.wait();
               }
               catch(InterruptedException ex)
               {
                   Util.println("exception in dme request cs.. "+ex.getMessage(),ex);
               }
            } 
        }
    }

    @Override
    public void releaseCS() {
        CsPermission = false;
        token.setCsSeqAtIndex(myId, seqNo);
        token.addCsExec(myId);
        releaseToken();
       
        Util.println("Releasing of cs completed");
    }
    
    int isAnyPendingRequest()
    {
        int firstLeft = -1;
        int firstRight = -1;
        int sqrtN = (int)Math.sqrt(N);
        int idx = myCol;
        idx = (sqrtN + idx - 1) % sqrtN;
        int[] csSeq = token.getCsSeq();
        HashSet<Integer> set = token.getCsExec();
        
        if(set.size() == sqrtN) // all nodes have executed CS atleast once
            return -1;
        int iterationLeft = 0;
        while(idx != myCol)
        {
            
            if(req[idx] > csSeq[columnNeighbour.get(idx)] && !set.contains(columnNeighbour.get(idx)))
            {
                firstLeft = idx;
                break;
            }
            idx = (sqrtN + idx - 1) % sqrtN;
            iterationLeft++;
        }
        idx = myCol;
        idx = (idx + 1) % sqrtN;
        int iterationRight = 0;
        while(idx != myCol)
        {
            
            if(req[idx] > csSeq[columnNeighbour.get(idx)] && !set.contains(columnNeighbour.get(idx)))
            {
                firstRight = idx;
                break;
            }
            idx = (idx + 1) % sqrtN;
            iterationRight++;
        }
        
        if(firstLeft == -1 && firstRight == -1)
            return -1;
        
        //int diffLeft = Math.abs(firstLeft - myCol);
        //int diffRight = Math.abs(firstRight - myCol);
        if(iterationLeft > iterationRight)
        {
            this.direction = "right";
            return firstRight;
        }
        
        this.direction = "left";
        return firstLeft;
        
    }
    void releaseToken()
    {
    	 int sqrtN = (int)Math.sqrt(N);
		 if(token.getNextExecProcess() == myId)
	     {
	         this.direction = "";
	         int req = isAnyPendingRequest();
	         Util.println("req = " + req + " direction "+direction);
	         if(req == -1) // no pending request or all nodes execute cs atleast once
	         {
	             // check other things
	             token.clearCsExec();
	             if(token.getRow() == sqrtN)
	             {
	                 token.setRow(0);
	                 token.setNextExecProcess(comm.neighbors.get(Linker.RIGHT));
	                 sendMessage("right","token",null);
	                 
	             }
	             else
	             {
	                 token.setNextExecProcess(comm.neighbors.get(Linker.DOWN));
	                 sendMessage("down","token",null);
	             }
	         }
	         else
	         {
	             token.setDirection(this.direction);
	             token.setNextExecProcess(columnNeighbour.get(req));
	             sendMessage(this.direction, "token", null);
	         }
	     }
	     else
	     {
	         sendMessage(token.getDirection(),"token",null);
	     }
    }
    void sendMessage(String dir, String tag, String message)
    {
        
        int next = -1;
        int right = comm.neighbors.get(Linker.RIGHT);
        int down = comm.neighbors.get(Linker.DOWN);
        int left = comm.neighbors.get(Linker.LEFT);
        switch (dir)
        {
            case "down":
                next = down;
                break;
            case "right":
                next = right;
                break;
            case "left":
                next = left;
                break;
        }
        //Util.println("........right........."+right+".......down"+down + " .......next.... "+next);
        if(LockTest.propogationDelay > 0)
        {
           try{
            TimeUnit.MICROSECONDS.sleep(LockTest.propogationDelay);
            }
            catch(InterruptedException ex)
            {
                Util.println("Exception in sleep"+ex.getMessage(),ex);
            } 
        }
        
        
        if(tag.equals("request"))
        {
            Util.println(myId, next, "cs_request_forward", "request send", "sending request message "+message+" from "+myId + " to "+next+" at "+LocalDateTime.now());
            sendMsg(next, "request", message+"_"+dir);
        }
        else if(tag.equals("token"))
        {
           token.setDirection(dir);
           String tok = token.convertTokenToString();
           Util.println(myId, next, "token_send", tok,"sending token to "+next + " at" + LocalDateTime.now()+ " token "+tok);
           sendMsg(next,"token",tok);
        }
    }
    
    public void updateReqSet()
    {
        int sqrtN = (int)Math.sqrt(N);
        int[] arr = token.getCsSeq();
        for(int n : columnNeighbour)
        {
            if(req[n% sqrtN] < arr[n])
                req[n % sqrtN] = arr[n];
        }
    }
  
    
    @Override
    public synchronized void handleMsg(Msg m, int src, String tag) 
    {
       // Util.println("..............message received....."+tag + " " +m.getMessage());
        if(tag.equals("request"))
        {
             Util.println("..............message received....."+tag + " " +m.getMessage() + " at "+LocalDateTime.now());
            int sqrtN = (int)Math.sqrt(N);
            String[] data = m.getMessage().trim().split("_");
            int node = Integer.parseInt(data[0]);
            int seq = Integer.parseInt(data[1]);
            int idx = node % sqrtN;
            if(req[idx] < seq)
            {
                //Util.println("adding request of "+j+" to "+myId);
                req[idx] = seq;
                Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                sendMessage(data[2], "request", node+"_"+seq);
                
            }
        }
        else if(tag.equals("token"))
        {
            //Util.println("token received from " + src +" to "+myId + " having value "+m.getMessage());
            String message = m.getMessage().trim();
            token.setToken(message);
            //Util.println("..........inside of real token.............");
            Util.println(myId,-1,"received_real_token",message," token received at "+LocalDateTime.now() + " from "+src + " "+message);
            if(token.getCsExec().isEmpty() && token.getDirection().equals("down"))
            {
                Util.println("direction "+token.getDirection() + " "+ (token.getDirection().equals("down")));
                token.setRow(token.getRow()+1);
            }
            updateReqSet();
            int sqrtN = (int)Math.sqrt(N);
            if(token.getCsSeqAtIndex(myId) < seqNo) // it requested for cs
            {
                synchronized(lockObj)
                {
                    CsPermission = true;
                    lockObj.notify();
                }
            }
            else
            {
                
            	releaseToken();
            }
        }
    }
	
}
