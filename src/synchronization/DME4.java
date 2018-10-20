
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import msg.Msg;
import util.Util;
import connectivity.Linker;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

class Request 
{
	int nodeid;
	int seqNo;
	int distance;
	
	
	public Request(int nodeid, int seqNo, int distance) {
		super();
		this.nodeid = nodeid;
		this.seqNo = seqNo;
		this.distance = distance;
	}
	
	public int getDistance() {
		return distance;
	}
	public void setDistance(int distance) {
		this.distance = distance;
	}
	public int getNodeid() {
		return nodeid;
	}
	public void setNodeid(int nodeid) {
		this.nodeid = nodeid;
	}
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return nodeid + "#" + seqNo;
	}
	
	
}
public class DME4 extends Process implements Lock
{
	
    private int         seqNo = 0;
    private Token2       token;
    private boolean     CsPermission = false;
    private Object      lockObj = new Object();
    private int[] 		seqArr = {0,0};
    private int[] 		nodeArr = {-1,-1};
    private int[]		distanceArr = {0,0};
    private boolean 	isRequested;
    
    public DME4(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        CsPermission = (myId == coordinator);
        token = Token2.getInstance(N);
        //req = new Request[3];
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
        seqNo++;
        /*nodeArr[1] = myId;
        seqArr[1] = seqNo;
        distanceArr[1] = 0;*/
        isRequested = true;
        //req[1] = new Request(myId, seqNo,0); 
        //Util.println("..........requesting for cs...........");
        Util.println(myId, -1, "request_cs", String.valueOf(seqNo), "..........requesting for cs..........."+seqNo + " "+Arrays.toString(nodeArr));
        
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
        isRequested = false;
        releaseToken();
       
        Util.println("Releasing of cs completed");
    }
    
    int isAnyPendingRequest()
    {
       
    	 int sqrtN = (int)Math.sqrt(N);
         HashSet<Integer> set = token.getCsExec();
         int nextRequest = -1;
         
         if(set.size() == sqrtN)
         {
         	nextRequest = -1;
         }
         else if(nodeArr[0] != -1 && nodeArr[1] != -1)
         {
         	if(!set.contains(nodeArr[0]) && !set.contains(nodeArr[1]))
         	{
         		if(distanceArr[0] > distanceArr[1])
         			nextRequest = 1;
         		else
         			nextRequest = 0;
         	}
         	else if(!set.contains(nodeArr[0]))
         		nextRequest = 0;
         	else
         		nextRequest = 1;
         }
         else if(nodeArr[0] != -1 && !set.contains(nodeArr[0]))
         	nextRequest =  0;
         else if(nodeArr[1] != -1 && !set.contains(nodeArr[1]))
         	nextRequest =  1;
         return nextRequest;
        
    }
    void releaseToken()
    {
    	 int sqrtN = (int)Math.sqrt(N);
		 if(token.getNextExecProcess() == myId)
	     {
	         
	         int reqIdx = isAnyPendingRequest();
	         Util.println("reqidx = " +  reqIdx);
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
	             token.setNextExecProcess(nodeArr[reqIdx]);
	             nodeArr[reqIdx] = -1;
	             seqArr[reqIdx] = 0;
	             distanceArr[reqIdx] = 0;
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
        if(nodeArr[0] != -1 && seqArr[0] <= arr[nodeArr[0]])
        {
        	nodeArr[0] = -1;
        	seqArr[0] = 0;
        	distanceArr[0] = 0;
        }
        if(nodeArr[1] != -1 && seqArr[1] <= arr[nodeArr[1]])
        {
        	nodeArr[1] = -1;
        	seqArr[1] = 0;
        	distanceArr[1] = 0;
        }
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
            	 Instant now = Instant.now();
            	Request temp = new Request(node, seq,0);
                boolean isPresentInLeft = nodeArr[0] == -1 ? false : nodeArr[0] == node && seqArr[0] == seq; 
                boolean isPresentInRight = nodeArr[1] == -1 ? false : nodeArr[1] == node && seqArr[1] == seq;
                if(!isRequested && !isPresentInLeft && !isPresentInRight)
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
                	if(nodeArr[1] == -1)
                	{
                		//req[2] = new Request(node, seq,rightDistance);
                		nodeArr[1] = node;
                		seqArr[1] = seq;
                		distanceArr[1] = rightDistance;
                		Util.println(myId,-1,"add_request",Arrays.toString(nodeArr),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(nodeArr));
                	}
                	else
                	{
                		int nodeid = nodeArr[1];
                		if(nodeid > node || nodeid == node)
                		{
                			nodeArr[1] = node;
                    		seqArr[1] = seq;
                    		distanceArr[1] = rightDistance;
                			Util.println(myId,-1,"add_request",Arrays.toString(nodeArr),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(nodeArr));
                		}
                	}
                }
                else // when distance from left side is less or equal to right
                {
                	if(nodeArr[0] == -1)
                	{
                		nodeArr[0] = node;
                		seqArr[0] = seq;
                		distanceArr[0] = leftDistance;
                		Util.println(myId,-1,"add_request",Arrays.toString(nodeArr),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(nodeArr));
                	}
                	else
                	{
                		int nodeid = nodeArr[0];
                		if(nodeid < node || nodeid == node)
                		{
                			nodeArr[0] = node;
                    		seqArr[0] = seq;
                    		distanceArr[0] = leftDistance;
                			Util.println(myId,-1,"add_request",Arrays.toString(nodeArr),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(nodeArr));
                		}
                	}
                }
                if(canForwardRequest)
                {
                    sendMessage(data[2], "request", node+"_"+seq);
                }
                Instant t = Instant.now();
                Util.println("Time elapsed in Request handling is "+ChronoUnit.MICROS.between(now, t));
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
