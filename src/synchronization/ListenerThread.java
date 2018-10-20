/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import msg.Msg;
import msg.MsgHandler;
import util.Util;

/**
 *
 * @author Prashant
 */
public class ListenerThread extends Thread{
    int channel;
    MsgHandler process;
    private volatile boolean running = true;
    public ListenerThread(int channel, MsgHandler process)
    {
        this.channel = channel;
        this.process = process;
    }
    
    public void run()
    {
        while(running)
        {
            try
            {
                // receive the message from socket of channel( channel is node id)
                Msg message = process.receiveMsg(channel);
                // if message is null then generate the exception
                if(message == null)
                    throw new NullPointerException();
                // send the message to handler
                process.handleMsg(message, message.getSrcId(), message.getTag());
            }
            catch(Exception ex)
            {
                //System.err.println(ex);
                 Util.println("Exception in listenerthread " +channel,ex );
               // ex.printStackTrace();
                running = false;
                
            }
        }
    }
}
