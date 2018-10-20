/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import java.util.Arrays;
import java.util.HashSet;
import msg.Converter;
import static msg.Converter.jsonToString;
import org.json.simple.JSONObject;

/**
 *
 * @author Prashant
 */
public class Token {
    
    private HashSet<Integer> row;
    //private int col = 0;
    private static Token token = null;
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
   
    private Token()
    {
    }
    
    private Token(int size)
    {
        this.csExec = new HashSet<>();
        this.row  = new HashSet<>();
        this.csSeq = new int[size];
    }

    

    public HashSet<Integer> getRow() {
		return row;
	}

	public void setRow(HashSet<Integer> row) {
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
    
    public static Token getInstance(int size)
    {
        if(token == null)
        {
            token = new Token(size);
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
    
    public void addRow(int val)
    {
        this.row.add(val);
    }
    
    public void clearRow()
    {
        this.row.clear();
    }
    
    public  String convertTokenToString()
    {
        JSONObject obj  = Converter.addSetTOJson(null, "row", row);
        obj  = Converter.toJson(obj, "nextprocess",String.valueOf(nextExecProcess));
        obj  = Converter.toJson(obj, "direction",direction);
        obj = Converter.addArrayTOJson(obj, "csseq", csSeq);
        obj = Converter.addSetTOJson(obj, "csexec", csExec);
        return jsonToString(obj);
    }
    
    public void setToken(String token)
    {
        JSONObject obj = Converter.stringToJson(token);
        this.direction = obj.get("direction").toString();
        this.clearRow();
        Converter.getSet(obj, "row", row);
        this.nextExecProcess = Integer.parseInt(obj.get("nextprocess").toString());
        this.clearCsExec();
        Converter.getSet(obj, "csexec", csExec);
        Converter.getArray(obj, "csseq", csSeq);
    }
    
    @Override
    public String toString()
    {
        return row+" "+String.valueOf(nextExecProcess) + " "+csExec +" "+Arrays.toString(csSeq);
    }
    
    public static void main(String[] args) {
        Token t = Token.getInstance(15);
        t.addRow(5);
        t.addRow(50);
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

