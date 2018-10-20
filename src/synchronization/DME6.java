
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


public class DME6 extends Process implements Lock
{
	
    private int         seqNo = 0;
    private Token2       token;
    private boolean     CsPermission = false;
    private Object      lockObj = new Object();
    private Request[]       req ;
    
    public DME6(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        CsPermission = (myId == coordinator);
        token = Token2.getInstance(N);
        req = new Request[3];
        
        
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
        seqNo++;
        req[1] = new Request(myId, seqNo,0); 
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
        req[1] = null;
        releaseToken();
       
        Util.println("Releasing of cs completed");
    }
    
    int isAnyPendingRequest()
    {
       
    	int sqrtN = (int)Math.sqrt(N);
        HashSet<Integer> set = token.getCsExec();
        int nextRequest = -1;
        /*if(set.size() == sqrtN) // all nodes have executed CS atleast once
            return -1;*/
       /* if(reqRow[0] == null && reqRow[1] == null)
        	return -1;*/
        if(set.size() == sqrtN)
        {
        	nextRequest = -1;
        }
        else if(req[0] != null && req[2] != null)
        {
        	if(!set.contains(req[0].getNodeid()) && !set.contains(req[2].getNodeid()))
        	{
        		if(req[0].getDistance() > req[2].getDistance())
        			nextRequest = 2;
        		else
        			nextRequest = 0;
        	}
        	else if(!set.contains(req[0].getNodeid()))
        		nextRequest = 0;
        	else
        		nextRequest = 2;
        }
        else if(req[0] != null && !set.contains(req[0].getNodeid()))
        	nextRequest =  0;
        else if(req[2] != null && !set.contains(req[2].getNodeid()))
        	nextRequest =  2;
        return nextRequest;
       /* int iterationLeft = 0;
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
        return firstLeft;*/
        
    }
    void releaseToken()
    {
    	 int sqrtN = (int)Math.sqrt(N);
		 if(token.getNextExecProcess() == myId)
	     {
	         
	         int reqIdx = isAnyPendingRequest();
	         Util.println("reqidx = " + req );
	         if(reqIdx == -1) // no pending request or all nodes execute cs atleast once
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
	             token.setDirection(reqIdx == 0 ? "left" : "right");
	             token.setNextExecProcess(req[reqIdx].getNodeid());
	             sendMessage(reqIdx == 0 ? "left" : "right", "token", null);
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
        /*for(int n : columnNeighbour)
        {
            if(req[n% sqrtN] < arr[n])
                req[n % sqrtN] = arr[n];
        }*/
        if(req[0] != null && req[0].getSeqNo() <= arr[req[0].getNodeid()])
        	req[0] = null;
        if(req[2] != null && req[2].getSeqNo() <= arr[req[2].getNodeid()])
        	req[2] = null;
    }
  
    
    @Override
    public synchronized void handleMsg(Msg m, int src, String tag) 
    {
       // Util.println("..............message received....."+tag + " " +m.getMessage());
        if(tag.equals("request"))
        {
             Util.println("..............message received....."+tag + " from " + src +" " +m.getMessage() + " at "+LocalDateTime.now());
            int sqrtN = (int)Math.sqrt(N);
            String[] data = m.getMessage().trim().split("_");
            int node = Integer.parseInt(data[0]);
            int seq = Integer.parseInt(data[1]);
            boolean canForwardRequest = false;
            if(myId != node)
            {
            	Request temp = new Request(node, seq,0);
                boolean isPresentInLeft = req[0] == null ? false : (temp.toString().equals(req[0].toString())); 
                boolean isPresentInRight = req[2] == null ? false : (temp.toString().equals(req[2].toString()));
                if(req[1] == null && !isPresentInLeft && !isPresentInRight)
                	canForwardRequest = true;
                int leftDistance = 0;
                int rightDistance = 0; 
                if(myId < node)
                {
                	rightDistance = node - myId - 1;
                	leftDistance = sqrtN - rightDistance - 2; // 2 is node i and j
                }
                else
                {
                	leftDistance =  myId - node - 1;
                	rightDistance = sqrtN - leftDistance - 2; // 2 is node i and j
                }  
                if(leftDistance > rightDistance) // when distance from right side is less
                {
                	Request r = req[2];
                	if(r == null)
                	{
                		req[2] = new Request(node, seq,rightDistance);
                		Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                	}
                		
                	else
                	{
                		int nodeid = req[2].getNodeid();
                		if(nodeid > node || nodeid == node)
                		{
                			req[2] = new Request(node, seq,rightDistance);
                			Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                		}
                	}
                }
                else // when distance from left side is less or equal to right
                {
                	Request r = req[0];
                	if(r == null)
                	{
                		req[0] = new Request(node, seq,leftDistance);
                		Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                	}
                	else
                	{
                		int nodeid = r.getNodeid();
                		if(nodeid < node || nodeid == node)
                		{
                			req[0] = new Request(node, seq,leftDistance);
                			Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                		}
                	}
                }
                if(canForwardRequest)
                {
                    sendMessage(data[2], "request", node+"_"+seq);
                }
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
