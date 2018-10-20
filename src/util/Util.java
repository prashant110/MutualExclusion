/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringTokenizer;
import java.util.logging.Level;

import synchronization.LockTest;

/**
 *
 * @author Prashu
 */
public class Util {
    static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
    
     public static void mySleep(int time) {
         
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    public static void myWait(Object obj) {
        println("waiting");
        try {
            obj.wait();
        } catch (InterruptedException e) {
        }
    }
    public static boolean lessThan(int A[], int B[]) {
        for (int j = 0; j < A.length; j++)
            if (A[j] > B[j]) return false;
        for (int j = 0; j < A.length; j++)
            if (A[j] < B[j]) return true;
        return false;
    }
    public static int maxArray(int A[]) {
        int v = A[0];
        for (int i=0; i<A.length; i++)
            if (A[i] > v) v = A[i];
        return v;
    }
    public static String writeArray(int A[]){
        StringBuilder s = new StringBuilder();
        for (int j = 0; j < A.length; j++)
            s.append(String.valueOf(A[j])).append(" ");
        return s.toString();
    }
   public static void readArray(String s, int A[]) {
        StringTokenizer st = new StringTokenizer(s);
        for (int j = 0; j < A.length; j++)
            A[j] = Integer.parseInt(st.nextToken());
    }
    public static int searchArray(int A[], int x) {
        for (int i = 0; i < A.length; i++)
            if (A[i] == x) return i;
        return -1;
    }
    public static synchronized  void println(String s){
       
       LockTest.LOGGER.info(s);
       if(LockTest.printonconsole)
       {
       LocalDateTime now = LocalDateTime.now();
        System.out.println(now + ": "+s);
    }
       /*LocalDateTime now = LocalDateTime.now();
       if(!LockTest.enableLog)
        System.out.println(now + ": "+s);*/
    }
    public static synchronized  void println(String msg, Throwable ex)
    {
        LockTest.LOGGER.log(Level.SEVERE,msg,ex);
    }
    public static synchronized  void println(int src, int des, String tag, String msg,String other){
       
        //if(tag.equals("token_send") || tag.equals("cs_request_forward") || tag.equals("execution_completed"))
       // {
        	if(LockTest.enableLog)
        		LockTest.log.sendMessage(src,des,tag,msg,other);
        //}
        println(other);
    }
    
}
