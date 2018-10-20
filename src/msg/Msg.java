/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msg;

import java.util.StringTokenizer;

/**
 *
 * @author Prashant
 */
public class Msg 
{
    int srcId;
    int destId;
    String tag;
    String message;
    int signLength;
    String signature;

    public int getDestId() {
        return destId;
    }

    public void setDestId(int destId) {
        this.destId = destId;
    }

    public int getSignLength() {
        return signLength;
    }

    public void setSignLength(int signLength) {
        this.signLength = signLength;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public Msg(int srcId, int destId, String tag, String message, int signLength, String signature)    
    {
        this.srcId = srcId;
        this.destId = destId;
        this.tag = tag;
        this.message = message;
        this.signLength = signLength;
        this.signature = signature;
    }

    public int getSrcId() 
    {
        return srcId;
    }

    public void setSrcId(int srcId) 
    {
        this.srcId = srcId;
    }

    public int getDesId() 
    {
        return destId;
    }

    public void setDesId(int destId) 
    {
        this.destId = destId;
    }

    public String getTag() 
    {
        return tag;
    }

    public void setTag(String tag) 
    {
        this.tag = tag;
    }

    public String getMessage() 
    {
        return message;
    }

    public void setMessage(String message) 
    {
        this.message = message;
    }
    
    public int getMessageInt()
    {
        StringTokenizer st = new StringTokenizer(message);
        return Integer.parseInt(st.nextToken());
    }
    
    public String toString()
    {
        return String.valueOf(srcId) + " " + String.valueOf(destId) + " " + tag + " " +message + "#";
    }
    
   
}
