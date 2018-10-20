/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package synchronization;

import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import connectivity.Linker;
import filehandler.Client;
import logger.Log;
import logger.SendLog;
import msg.MsgHandler;
import util.Symbols;
import util.Util;

/**
 *
 * @author Prashu
 */
public class LockTest {
    static int                  myId;
    static Lock                 lock = null;
    static Linker               comm = null;
    static String               baseName;
    static int                  port;
    public static String        path;
    public static int           k;  
    public static  boolean      useSignature;
    public static boolean       enableLog = false;
    static DateTimeFormatter    dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
    public static Logger        LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static SendLog       log;
    public static boolean       printonconsole;
    static String 				fileServerHost = "127.0.0.1";
    static int 					fileServerPort = 13267;
    static Instant 		startTime;
    public static long 			propogationDelay = 0;
    public static String host = "127.0.0.1";
    public static void main(String[] args) 
    {
    
        try 
        {
            HashMap<Integer,Integer> map = new HashMap<>();
            map.put(10000,0);
            map.put(5000,1);
            map.put(3333,2);
            map.put(2500,3);
            map.put(2000,4);
            map.put(1666,5);
            map.put(1428,6);
            map.put(1250,7);
            map.put(1111,8);
            map.put(1000,9);
            String fileName = args[0];
            FileReader reader=new FileReader(fileName);
            Properties p=new Properties();  
            p.load(reader);  
            
            
            baseName = p.getProperty("base");
            propogationDelay = Long.parseLong(p.getProperty("propogationDelay"));
            myId = Integer.parseInt(p.getProperty("myid"));
            int numProc = Integer.parseInt(p.getProperty("n"));
            enableLog = Boolean.valueOf(p.getProperty("log"));
            fileServerHost = p.getProperty("fileserverhost");
            fileServerPort = Integer.parseInt(p.getProperty("fileserverport"));
            String logServerHost = p.getProperty("logserverhost");
            int logserverport = Integer.parseInt(p.getProperty("logserverport"));
            log = new SendLog("http://"+logServerHost+":"+logserverport+"/log");
            Symbols.NAMESERVER = p.getProperty("nameserver");
            Symbols.NAMESERVERPORT = Integer.parseInt(p.getProperty("nameserverport"));
            int appPort = p.getProperty("appport") != null ? Integer.parseInt(p.getProperty("appport")) : 0;
            //System.out.println(baseName+" "+myId+ " "+numProc+" "+path + " "+detectionLimit + " "+useSignature+" "+isRestarted);
            host = p.getProperty("host");
            printonconsole = Boolean.valueOf(p.getProperty("printonconsole"));
            String topology = p.getProperty("topology");
            int waitingTime = Integer.parseInt(p.getProperty("wait"));
            
            Log.setup("log\\log_"+map.get(waitingTime)+"\\log_"+myId+".txt");
            comm = new Linker(baseName, myId, numProc,appPort,topology);
            lock = new DME6(comm,0);
            comm.setProcess(lock);
            
            
            for (int i = 0; i < numProc; i++)
               if (i != myId)
                  (new ListenerThread(i, (MsgHandler)lock)).start();
            
            
            int request = Integer.parseInt(p.getProperty("request"));
            int wait = (int)(Math.random()*((waitingTime - 0)+1))+0;
            Util.println("starting wait of : "+wait);
            Util.mySleep(wait);
            Util.println("wait ends here");
            boolean canRequest = Boolean.valueOf(p.get("canRequest").toString());
            while(request > 0 && canRequest)
            {
                //boolean run = r.nextBoolean();
                    
               // if(run)
                {
                    Util.println(myId, -1, "idle", "outside critical section", myId + " is not in CS");
                    //Util.mySleep(3000);
                    startTime = Instant.now();
                    lock.requestCS();
                    executeCS(request);
                    //Util.mySleep(3000);
                    Util.println(myId, -1, "execution_completed", myId+"#"+request, "............executing critical Section completed.............");
                    Util.println(myId, -1, "cs_released", "", "Releasing CS");
                    lock.releaseCS();
                    request--;
                    /*if(request == 0)
                        System.exit(0);*/
                    Util.mySleep(waitingTime);
                }
               // else
               // {
                    
               // }
            }
        }
        catch (InterruptedException e) {
            Util.println("exception in locktest interrupted exception",e);
            if (comm != null) comm.close();
        }
        catch (Exception e) {
            Util.println("exception in locktest :: "+e.getMessage(),e);
        }
    }
    
    public static void executeCS(int request) throws IOException
    {
        //Util.println("............executing critical Section.............");
        Instant now = Instant.now();
        
        long difference = ChronoUnit.MICROS.between(startTime, now);
        Util.println(myId, -1, "execution_start", "executing CS #"+myId+"#"+request, "............executing critical Section.............#"+myId+"#"+request);
        Socket s = Client.getConnection(fileServerHost, fileServerPort);
        
        String message = " Message send from process "+myId + " at "+ "  " + " " +difference;
        Client.sendMsg(s, message);
        Client.closeSocket(s);
        Instant t = Instant.now();
        Util.println("Time elapsed in CS Execution is "+ChronoUnit.MICROS.between(now, t));
        //Util.println("............executing critical Section completed.............");
        
    }
    
    public static Lock getLock()
    {
        return lock;
    }
}

