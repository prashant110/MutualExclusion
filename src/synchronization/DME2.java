/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import java.util.LinkedList;
import java.util.Queue;
import msg.Msg;
import util.Util;
import connectivity.Linker;
import java.util.HashMap;
import msg.Converter;
import org.json.simple.JSONObject;
import static synchronization.Process.N;

/**
 *
 * @author Prashant
 */


public class DME2 extends Process implements Lock
{
    
    int             col = 0;
    int             row = 0;
    boolean         chCol = false;
    boolean         CsPermission = false;
    private Object               lockObj = new Object();
    Queue<Integer> queue = new LinkedList<>();
    
    public DME2(Linker initComm, int coordinator) 
    {
    
        super(initComm);
        int sqrtN = (int)Math.sqrt(N);
        CsPermission = (myId == coordinator);
        
        //calculating columns 
        
        
        if(CsPermission)
            Util.println(myId,-1,"received_real_token","starting algo","..........inside of real token.............");
    }
    
    
    @Override
    public void requestCS() {
       
        queue.add(myId);
        //Util.println("..........requesting for cs...........");
        Util.println(myId, -1, "request_cs", queue.toString(), "..........requesting for cs...........");
        if(queue.size() == 1 && queue.peek() == myId)
        {
          //int next = (int)this.comm.neighbors.get(Linker.RIGHT);
           sendMessage("right","request",myId,null);
        }
        synchronized(lockObj)
        {
           while(!CsPermission)
            {
                //Util.println("waiting");
               try
               {
                    lockObj.wait();
               }
               catch(InterruptedException ex)
               {
                   Util.println("exception in dme request cs.. "+ex);
               }
            } 
        }
    }

    @Override
    public void releaseCS() {
        CsPermission = false;
        queue.clear();
        Util.println(myId, -1, "queue_clear", "", "Queue Clear");
        if(col == Math.sqrt(N))
        {
            col = 0;
            sendMessage("down","token",0,null);
        }
        else
        {
            col++;
            sendMessage("right","token",0,null);
        }
        Util.println("Releasing of cs completed");
    }
    void sendMessage(String direction, String tag, int message1, String message2)
    {
        
        int next = -1;
        int right = comm.neighbors.get(Linker.RIGHT);
        int down = comm.neighbors.get(Linker.DOWN);
        if(direction.equals("right"))
            next = right;
        else
            next = down;
        Util.println("........right........."+right+".......down"+down + " .......next.... "+next);
        if(tag.equals("request"))
        {
            Util.println(myId, next, "cs_request_forward", "request send", "sending request message from "+myId + " to "+next);
            sendMsg(next, "request", message1);
        }
        else if(tag.equals("token"))
        {
            
            HashMap<String,String> map = new HashMap<>();
            map.put("rowCounter", String.valueOf(row));
            map.put("colCounter", String.valueOf(col));
            map.put("chCol",String.valueOf(chCol));
            String token = Converter.jsonToString(Converter.toJson(null,map));
            Util.println(myId, next, "token_send", token,"sending token to "+next + " token "+token );
            sendMsg(next,"token",token);
        }
        
    }
    
   
  
    public synchronized void handleMsg(Msg m, int src, String tag) {
        Util.println("..............message received....."+tag + " " +m.getMessage());
        if(tag.equals("request"))
        {
            int j = Integer.parseInt(m.getMessage().trim());
            if(myId != j && queue.isEmpty())
            {
                //Util.println("adding request of "+j+" to "+myId);
                queue.add(j);
                Util.println(myId,-1,"add_request",queue.toString(),"adding request of "+j+" to "+myId);
                sendMessage("right", "request", j, null);
            }
        }
        else if(tag.equals("token"))
        {
            //Util.println("token received from " + src +" to "+myId + " having value "+m.getMessage());
            String message = m.getMessage();
            JSONObject obj = Converter.stringToJson(message);
            col = Integer.parseInt(obj.get("colCounter").toString());
            row = Integer.parseInt(obj.get("rowCounter").toString());
            chCol = Boolean.valueOf(obj.get("chCol").toString());
            //Util.println("..........inside of real token.............");
            Util.println(myId,-1,"received_real_token",message,"..........inside of real token............."+message);
            if(col == 0)
            {
                row++;
                if(row == Math.sqrt(N))
                {
                    Util.println("One row completed so sending token to right");
                    chCol = true;
                    col = -1;
                    sendMessage("right","token",0,null);
                }
                else if(queue.isEmpty())
                {
                    //sendToken("down");// send to right neighbor
                    sendMessage("down","token",0,null);
                }
                else
                {
                    if(queue.contains(myId))
                    {
                        //Util.println("..........permission granted to execute CS.........1");
                        synchronized(lockObj)
                        {
                            CsPermission = true;
                            lockObj.notify();
                        }
                    }
                    else
                    {
                      queue.clear();
                      Util.println(myId, -1, "queue_clear", "", "Queue Clear");
                       col = 1;
                       sendMessage("right","token",0,null);
                    }
                }
            }
            else if(col > 0)
            {
                if(queue.contains(myId))
                {
                    //Util.println("..........permission granted to execute CS.........2");
                    synchronized(lockObj)
                    {
                        CsPermission = true;
                        lockObj.notify();
                    }
                }
                else
                {
                   queue.clear();
                   Util.println(myId, -1, "queue_clear", "", "Queue Clear");

                   if(col == Math.sqrt(N))
                   {
                       col = 0;
                       // oldColProcess = colProcess;

                       sendMessage("down","token",0,null);
                   }
                   else
                   {
                       col++;
                      //sendToken("right");// send to right neighbor
                      sendMessage("right","token",0,null);
                   }
                }
            }
            else if(col == -1)
            {
                chCol = false;
                row = 0;
                if(queue.isEmpty())
                {
                    col = 0;
                    //Util.println("queue is empty first if sending token to down");

                    sendMessage("down","token",0,null);
                }
                else
                {

                    
                    if(queue.contains(myId))
                    {
                        col = 0;
                        //Util.println("queue contains me and going to execute cs");
                       // Util.println("..........permission granted to execute CS.........3");
                        synchronized(lockObj)
                        {
                            CsPermission = true;
                            lockObj.notify();
                        }
                    }
                    else
                    {
                       // Util.println("going to empty queue and sending token to right");
                        col = 1;
                        queue.clear();
                        Util.println(myId, -1, "queue_clear", "", "Queue Clear");

                        //sendToken("right");// send to right neighbor
                        sendMessage("right","token",0,null);
                    }
                }
            }
        }
        
    }
}
