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
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Prashant
 */


public class DME extends Process implements Lock
{
    private int         seqNo = 0;
    private Token       token;
    private boolean     CsPermission = false;
    private Object      lockObj = new Object();
    private int[]       req ;
    private int         myCol;
    private String      direction;
    private boolean 	hasToken;
    private boolean		noPendingRequest;
    
    static ArrayList<Integer>    columnNeighbour ;
    static ArrayList<Integer>    rowNeighbour ;
    
    public DME(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        int sqrtN = (int)Math.sqrt(N);
        CsPermission = (myId == coordinator);
        hasToken = (myId == coordinator);
        token = Token.getInstance(N);
        req = new int[N];
        myCol = myId % sqrtN;
        columnNeighbour = new ArrayList<>(sqrtN);
        rowNeighbour = new ArrayList<>(sqrtN);
        int myRow = myId / sqrtN;
        for(int k = 0; k < sqrtN; k++)
            columnNeighbour.add(myRow * sqrtN + k);
        
        
        int idx = (myId / sqrtN);
        int firstNode = (myId - idx * sqrtN) % N;
        for(int k = 0; k < sqrtN; k++)
        {
            rowNeighbour.add((firstNode + k * sqrtN) % N);
        }
        
        
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
        seqNo++;
        req[myId] = seqNo; 
        //Util.println("..........requesting for cs...........");
        Util.println(myId, -1, "request_cs", String.valueOf(seqNo), "..........requesting for cs..........."+seqNo + " "+Arrays.toString(req));
        /*if(queue.size() == 1 && queue.peek() == myId)
        {
          //int next = (int)this.comm.neighbors.get(Linker.RIGHT);
           sendMessage("right","request",myId,null);
        }*/
        sendMessage("right","request",myId+"_"+seqNo);
        sendMessage("left","request",myId+"_"+seqNo);
        sendMessage("up","request",myId+"_"+seqNo);
        sendMessage("down","request",myId+"_"+seqNo);
        if(hasToken)
        	CsPermission = true;
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
        Util.println("Releasing of cs completed");
        releaseToken();
    }
    int checkPendingRequestInColumn(boolean skipRowCheck)
    {
    	int sqrtN = (int)Math.sqrt(N);
    	int idx = (sqrtN + myCol - 1) % sqrtN;
        int[] csSeq = token.getCsSeq();
        HashSet<Integer> row = token.getRow();
    	int arr[] = new int[sqrtN];
    	int pendingRow = 0;
    	Util.println("checkPendingRequestInColumn " + Arrays.toString(req));
    	for(int i=0; i < sqrtN; i++)
    	{
    		if(i == (myId / sqrtN))
    			continue;
    		for(int j = 0; j < sqrtN; j++)
    		{
    			if(req[i*sqrtN + j] > csSeq[i*sqrtN + j])
    			{
    				arr[i] = 1; // request present in row i
    				pendingRow++;
    				break;
    			}
    		}
    	}
    	Util.println("pending request "+pendingRow + " :: "+Arrays.toString(arr));
    	if(pendingRow == 0)
    		return -1;
    	
    	 int myRow = myId / sqrtN;
    	 int iterationUp = 0;
    	 int firstUp = -1;
    	 idx = (sqrtN + myRow - 1) % sqrtN;
         while(idx != myRow)
         {
             
             if(arr[idx] == 1 && (skipRowCheck || !row.contains(idx)))
             {
            	 firstUp = idx;
                 break;
             }
             idx = (sqrtN + idx - 1) % sqrtN;
             iterationUp++;
         }
         
         idx = (myRow + 1) % sqrtN;
         int iterationdown = 0;
         int firstDown = -1;
         while(idx != myRow)
         {
             
             if(arr[idx] == 1 && (skipRowCheck || !row.contains(idx)))
             {
            	 firstDown = idx;
                 break;
             }
             idx = (idx + 1) % sqrtN;
             iterationdown++;
         }
         if(firstDown == -1 && firstUp == -1)
        	 return  -1;
    	 if(iterationdown > iterationUp)
    	 {
    		 this.direction = "up";
    		 return firstUp;
    	 }
    	 this.direction = "down";
    	 return firstDown;
    }
    int checkPendingRequestInRow(boolean skipSetCheck)
    {
    	int firstLeft = -1;
        int firstRight = -1;
        int sqrtN = (int)Math.sqrt(N);
        int idx = myCol;
        idx = (sqrtN + idx - 1) % sqrtN;
        int[] csSeq = token.getCsSeq();
        HashSet<Integer> set = token.getCsExec();
        HashSet<Integer> row = token.getRow();
        /*if(set.size() == sqrtN) // all nodes have executed CS atleast once
            return -1;*/
        int iterationLeft = 0;
        while(idx != myCol)
        {
            
            if(req[columnNeighbour.get(idx)] > csSeq[columnNeighbour.get(idx)] && (skipSetCheck|| !set.contains(columnNeighbour.get(idx))))
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
            
            if(req[columnNeighbour.get(idx)] > csSeq[columnNeighbour.get(idx)] && (skipSetCheck|| !set.contains(columnNeighbour.get(idx))))
            {
                firstRight = idx;
                break;
            }
            idx = (idx + 1) % sqrtN;
            iterationRight++;
        }
        if((firstLeft == -1 && firstRight == -1))
        	return -1;
        
        else if(iterationLeft > iterationRight)
        {
            this.direction = "right";
            return firstRight;
        }
        this.direction = "left";
        return firstLeft;
        
    }
    int isAnyPendingRequest()
    {
        
        int req = checkPendingRequestInRow(false);
        Util.println("Request found at 0"+req + " direction "+this.direction);
        if(req != -1)
        	return req;
        else
        {
        	req = checkPendingRequestInColumn(false);
        	Util.println("Request found at 1"+req + " direction "+this.direction);
        	if(req != -1)
        		return req;
        	else
        	{
        		req = checkPendingRequestInColumn(true);
        		Util.println("Request found at 2"+req + " direction "+this.direction);
        		if(req != -1)
        			return req;
        		else
        		{
        			req = checkPendingRequestInRow(true);
        			Util.println("Request found at 3"+req + " direction "+this.direction);
        			if(req != -1)
        				return req;
        		}
        	}
        }
        return -1;
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
        int sqrtN = (int)Math.sqrt(N);
        int[] arr = token.getCsSeq();
        for(int i = 0; i < N; i++)
        {
            if(req[i] < arr[i])
                req[i] = arr[i];
        }
    }
  
    public void releaseToken()
    {
    	int sqrtN = (int)Math.sqrt(N);
        if(token.getNextExecProcess() == myId)
        {
            this.direction = "";
            int req = isAnyPendingRequest();
            Util.println("req = " + req + " direction "+direction);
            
            if(req == -1) // no pending request or all nodes execute cs atleast once
            {
                // do nothing;
            	hasToken = true;
            	noPendingRequest = true;
            }
            else
            {
            	noPendingRequest = false;
            	hasToken = false;
                token.setDirection(this.direction);
                if(this.direction.equals("left") || this.direction.equals("right"))
                	token.setNextExecProcess(columnNeighbour.get(req));
                else
                {
                	token.clearCsExec();
                	token.setNextExecProcess(rowNeighbour.get(req));
                }
                sendMessage(this.direction, "token", null);
            }
        }
        else
        {
        	hasToken = false;
            sendMessage(token.getDirection(),"token",null);
        }
    }
    /**
     *
     * @param m
     * @param src
     * @param tag
     */
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
            int idx = node;
            if(req[idx] < seq)
            {
                //Util.println("adding request of "+j+" to "+myId);
                req[idx] = seq;
                Util.println(myId,-1,"add_request",Arrays.toString(req),"adding request of "+node+" with seq No "+seq+" in "+myId+" final "+Arrays.toString(req));
                if(data[2].equals("left") || data[2].equals("right"))
                {
                	sendMessage("up", "request", node+"_"+seq);
                	sendMessage("down", "request", node+"_"+seq);
                }
                else
                	sendMessage(data[2], "request", node+"_"+seq);
                
                if(noPendingRequest && hasToken && !CsPermission) /// if till now there is no pending request and node has token then it will release token
                {
                	hasToken = false;
                	releaseToken();
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
                token.addRow(myId/sqrtN);
            }
            updateReqSet();
            
            /*if(token.getRow() == sqrtN)
            {
                token.setRow(0);
                token.setNextExecProcess(comm.neighbors.get(Linker.RIGHT));
                sendMessage("right","token",null);

            }*/
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
