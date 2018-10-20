
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Prashant
 */

class RequestMsg 
{
	int nodeid;
	int seqNo;
	int distance;
	
	
	public RequestMsg(int nodeid, int seqNo, int distance) {
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
		return nodeid + "#" + seqNo;
	}
	
	
}
public class DME5 extends Process implements Lock
{
	
    private int         seqNo = 0;
    private Token       token;
    private boolean     CsPermission = false;
    private Object      lockObj = new Object();
    private RequestMsg[]       reqRow ;
    private RequestMsg[]       reqCol ;
    
    
    public DME5(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        CsPermission = (myId == coordinator);
        token = Token.getInstance(N);
        reqRow = new RequestMsg[3];
        reqCol = new RequestMsg[2];
        
        
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
        seqNo++;
        reqRow[1] = new RequestMsg(myId, seqNo,0); 
        //Util.println("..........requesting for cs...........");
        Util.println(myId, -1, "request_cs", String.valueOf(seqNo), "..........requesting for cs..........."+seqNo + " "+Arrays.toString(reqRow));
        
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
        reqRow[1] = null;
        releaseToken();
       
        Util.println("Releasing of cs completed");
    }
    
    int isAnyPendingRequest()
    {
        
        int sqrtN = (int)Math.sqrt(N);
        HashSet<Integer> set = token.getCsExec();
        HashSet<Integer> row = token.getRow();
        int nextRequest = -1;
        /*if(set.size() == sqrtN) // all nodes have executed CS atleast once
            return -1;*/
       /* if(reqRow[0] == null && reqRow[1] == null)
        	return -1;*/
        if(set.size() == sqrtN)
        {
        	nextRequest = -1;
        }
        else if(reqRow[0] != null && reqRow[2] != null)
        {
        	if(!set.contains(reqRow[0].getNodeid()) && !set.contains(reqRow[2].getNodeid()))
        	{
        		if(reqRow[0].getDistance() > reqRow[2].getDistance())
        			nextRequest = 2;
        		else
        			nextRequest = 0;
        	}
        	else if(!set.contains(reqRow[0].getNodeid()))
        		nextRequest = 0;
        	else
        		nextRequest = 2;
        }
        else if(reqRow[0] != null && !set.contains(reqRow[0].getNodeid()))
        	nextRequest =  0;
        else if(reqRow[2] != null && !set.contains(reqRow[2].getNodeid()))
        	nextRequest =  2;
        
        if(nextRequest == -1)
        {
        	if(row.size() == sqrtN)
        	{
        		nextRequest = -1;
        	}
        	else if(reqCol[0] != null && reqCol[1] != null)
        	{
        		if(!row.contains(reqCol[0].getNodeid()) && !set.contains(reqCol[1].getNodeid()))
            	{
            		if(reqCol[0].getDistance() > reqCol[1].getDistance())
            			nextRequest = 4;
            		else
            			nextRequest = 3;
            	}
            	else if(!set.contains(reqCol[0].getNodeid()))
            		nextRequest = 3;
            	else
            		nextRequest = 4;
        	}
        	else if(reqCol[0] != null && !set.contains(reqCol[0].getNodeid()))
            	nextRequest =  3;
            else if(reqCol[1] != null && !set.contains(reqCol[1].getNodeid()))
            	nextRequest =  4;
        }
        return nextRequest;
    }
    void releaseToken()
    {
    	 int sqrtN = (int)Math.sqrt(N);
		 if(token.getNextExecProcess() == myId)
	     {
	         int reqIdx = isAnyPendingRequest();
	         if(reqIdx == -1) // no pending request or all nodes execute cs atleast once
	         {
	        	 
	             // check other things
	             token.clearCsExec();
	             if(token.getRow().size() == sqrtN)
	             {
	                 token.clearRow();
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
	        	 if(reqIdx == 0 || reqIdx == 2)
	        	 {
	        		 
	        		 token.setDirection(reqIdx == 0 ? "left" : "right");
		             token.setNextExecProcess(reqRow[reqIdx].getNodeid());
		             reqRow[reqIdx] = null;
		             sendMessage(reqIdx == 0 ? "left" : "right", "token", null);
	        	 }
	        	 else
	        	 {
	        		 
	        		 token.setDirection(reqIdx == 3 ? "up" : "down");
		             token.setNextExecProcess(reqCol[reqIdx].getNodeid());
		             reqRow[reqIdx] = null;
		             sendMessage(reqIdx == 3 ? "up" : "down", "token", null);
	        	 }
	             
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
        int up = comm.neighbors.get(Linker.TOP);
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
            case "up":
                next = up;
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
        
        int[] arr = token.getCsSeq();
        if(reqRow[0] != null && reqRow[0].getSeqNo() <= arr[reqRow[0].getNodeid()])
        	reqRow[0] = null;
        if(reqRow[2] != null && reqRow[2].getSeqNo() <= arr[reqRow[2].getNodeid()])
        	reqRow[2] = null;
        if(reqCol[0] != null && reqCol[0].getSeqNo() <= arr[reqCol[0].getNodeid()])
        	reqCol[0] = null;
        if(reqCol[1] != null && reqCol[1].getSeqNo() <= arr[reqCol[1].getNodeid()])
        	reqCol[1] = null;
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
            String direction = data[2];
            boolean canForwardRequest = false;
            if(myId != node)
            {
            	if(direction.equals("left") || direction.equals("right"))
            	{ 
            		RequestMsg temp = new RequestMsg(node, seq,0);
                    boolean isPresentInLeft = reqRow[0] == null ? false : (temp.toString().equals(reqRow[0].toString())); 
                    boolean isPresentInRight = reqRow[2] == null ? false : (temp.toString().equals(reqRow[2].toString()));
                    if(reqRow[1] == null && !isPresentInLeft && !isPresentInRight)
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
                    	RequestMsg r = reqRow[2];
                    	if(r == null)
                    	{
                    		reqRow[2] = new RequestMsg(node, seq,rightDistance);
                    		Util.println(myId,-1,"add_request",Arrays.toString(reqRow),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(reqRow));
                    	}
                    		
                    	else
                    	{
                    		int nodeid = reqRow[2].getNodeid();
                    		if(nodeid > node || nodeid == node)
                    		{
                    			reqRow[2] = new RequestMsg(node, seq,rightDistance);
                    			Util.println(myId,-1,"add_request",Arrays.toString(reqRow),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(reqRow));
                    		}
                    	}
                    }
                    else // when distance from left side is less or equal to right
                    {
                    	RequestMsg r = reqRow[0];
                    	if(r == null)
                    	{
                    		reqRow[0] = new RequestMsg(node, seq,leftDistance);
                    		Util.println(myId,-1,"add_request",Arrays.toString(reqRow),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(reqRow));
                    	}
                    	else
                    	{
                    		int nodeid = r.getNodeid();
                    		if(nodeid < node || nodeid == node)
                    		{
                    			reqRow[0] = new RequestMsg(node, seq,leftDistance);
                    			Util.println(myId,-1,"add_request",Arrays.toString(reqRow),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(reqRow));
                    		}
                    	}
                    }
                    if(canForwardRequest)
                    {
                        sendMessage(data[2], "request", node+"_"+seq);
                    }
            	}
            	else
            	{
            		RequestMsg temp = new RequestMsg(node, seq,0);
                    boolean isPresentInLeft = reqCol[0] == null ? false : (temp.toString().equals(reqCol[0].toString())); 
                    boolean isPresentInRight = reqCol[2] == null ? false : (temp.toString().equals(reqCol[2].toString()));
                    if(!isPresentInLeft && !isPresentInRight)
                    	canForwardRequest = true;
                    
                    int myRow = myId / sqrtN;
                    int nodeRow = node / sqrtN;
                    
                    int topDistance = 0;
                    int downDistance = 0; 
                    if(myId < node)
                    {
                    	downDistance = nodeRow - myRow - 1;
                    	topDistance = sqrtN - downDistance - 2; // 2 is node i and j
                    }
                    else
                    {
                    	topDistance =  myId - node - 1;
                    	downDistance = sqrtN - topDistance - 2; // 2 is node i and j
                    }
                    
                    if(topDistance > downDistance)
                    {
                    	RequestMsg r = reqCol[1];
                    	if(r == null)
                    	{
                    		reqCol[1] = new RequestMsg(node, seq,downDistance);
                    		Util.println(myId,-1,"add_request",Arrays.toString(reqCol),"adding request of "+node+" with seq No "+seq+" in "+myId+" final col"+Arrays.toString(reqCol));
                    	}
                    		
                    	else
                    	{
                    		int nodeid = reqCol[1].getNodeid();
                    		if(nodeid < node || nodeid == node)
                    		{
                    			reqCol[1] = new RequestMsg(node, seq,downDistance);
                    			Util.println(myId,-1,"add_request",Arrays.toString(reqCol),"adding request of "+node+" with seq No "+seq+" in "+myId+" final col"+Arrays.toString(reqCol));
                    		}
                    	}
                    }
                    else
                    {
                    	RequestMsg r = reqCol[0];
                    	if(r == null)
                    	{
                    		reqCol[0] = new RequestMsg(node, seq,topDistance);
                    		Util.println(myId,-1,"add_request",Arrays.toString(reqCol),"adding request of "+node+" with seq No "+seq+" in "+myId+" final col"+Arrays.toString(reqCol));
                    	}
                    	else
                    	{
                    		int nodeid = r.getNodeid();
                    		if(nodeid > node || nodeid == node)
                    		{
                    			reqCol[0] = new RequestMsg(node, seq,topDistance);
                    			Util.println(myId,-1,"add_request",Arrays.toString(reqCol),"adding request of "+node+" with seq No "+seq+" in "+myId+" final col"+Arrays.toString(reqCol));
                    		}
                    	}
                    }
                    if(canForwardRequest)
                    {
                        sendMessage(data[2], "request", node+"_"+seq);
                    }
            	}
            	
            }
        }
        else if(tag.equals("token"))
        {
            //Util.println("token received from " + src +" to "+myId + " having value "+m.getMessage());
            String message = m.getMessage().trim();
            token.setToken(message);
            int sqrtN = (int)Math.sqrt(N);
            //Util.println("..........inside of real token.............");
            Util.println(myId,-1,"received_real_token",message," token received at "+LocalDateTime.now() + " from "+src + " "+message);
            if(token.getDirection().equals("down") || token.getDirection().equals("up"))
            {
                Util.println("direction "+token.getDirection() + " "+ (token.getDirection().equals("down")));
                if(token.getRow().size() == sqrtN)
                	token.clearRow();
                if(reqRow[0] != null || reqRow[1] != null || reqRow[2] != null) 
                	token.addRow(myId/sqrtN);
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
