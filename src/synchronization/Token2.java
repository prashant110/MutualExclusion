/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import msg.Converter;
import static msg.Converter.addArrayTOJson;
import static msg.Converter.jsonToString;
import org.json.simple.JSONObject;

/**
 *
 * @author Prashant
 */
public class Token2 {
    
    private int row = 0;
    //private int col = 0;
    private static Token2 token = null;
    private int[] csSeq;
    private HashSet<Integer> csExec;
    private int nextExecProcess = 0;
    private String direction = "down";

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getNextExecProcess() {
        return nextExecProcess;
    }

    public void setNextExecProcess(int nextExecProcess) {
        this.nextExecProcess = nextExecProcess;
    }
   
    private Token2()
    {
    }
    
    private Token2(int size)
    {
        this.csExec = new HashSet<>();
        this.csSeq = new int[size];
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int[] getCsSeq() {
        return csSeq;
    }

    public void setCsSeq(int[] csSeq) {
        this.csSeq = csSeq;
    }

    public HashSet<Integer> getCsExec() {
        return csExec;
    }

    public void setCsExec(HashSet<Integer> csExec) {
        this.csExec = csExec;
    }
    
    public static Token2 getInstance(int size)
    {
        if(token == null)
        {
            token = new Token2(size);
        }
        return token;
    }
    
    public void setCsSeqAtIndex(int idx, int val)
    {
        this.csSeq[idx] = val;
    }
    
    public int getCsSeqAtIndex(int idx)
    {
        return this.csSeq[idx];
    }
    public void addCsExec(int val)
    {
        this.csExec.add(val);
    }
    
    public void clearCsExec()
    {
        this.csExec.clear();
    }
    
    public  String convertTokenToString()
    {
        JSONObject obj  = Converter.toJson(null, "row",String.valueOf(row));
        obj  = Converter.toJson(obj, "nextProcess",String.valueOf(nextExecProcess));
        obj  = Converter.toJson(obj, "direction",direction);
        obj = Converter.addArrayTOJson(obj, "csSeq", csSeq);
        obj = Converter.addSetTOJson(obj, "csRow", csExec);
        return jsonToString(obj);
    }
    
    public void setToken(String token)
    {
        JSONObject obj = Converter.stringToJson(token);
        this.direction = obj.get("direction").toString();
        this.row = Integer.parseInt(obj.get("row").toString());
        this.nextExecProcess = Integer.parseInt(obj.get("nextProcess").toString());
        this.clearCsExec();
        Converter.getSet(obj, "csRow", csExec);
        Converter.getArray(obj, "csSeq", csSeq);
    }
    
    @Override
    public String toString()
    {
        return String.valueOf(row)+" "+String.valueOf(nextExecProcess) + " "+csExec +" "+Arrays.toString(csSeq);
    }
    
    public static void main(String[] args) {
        Token2 t = Token2.getInstance(15);
        t.setRow(5);
        t.setNextExecProcess(6);
        t.addCsExec(5);
        t.addCsExec(9);
        t.addCsExec(1);
        t.setCsSeqAtIndex(0, 4);
        t.setCsSeqAtIndex(5, 3);
        t.setCsSeqAtIndex(9, 2);
        t.setCsSeqAtIndex(6, 78);
        String tok = t.convertTokenToString();
        System.out.println("tok = " + tok);
        t.addCsExec(78);
        System.out.println("tok = " + t.convertTokenToString());
        t.setToken(tok);
        System.out.println("tok = " + t.convertTokenToString());
    }
}

