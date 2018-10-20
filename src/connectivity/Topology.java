/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connectivity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.StringTokenizer;

import util.Util;
/**
 *
 * @author Prashant
 */
public class Topology 
{
    public static void readNeighbors(int myId, int N,LinkedList<Integer> neighbors, String topology) {
        Util.println("Reading topology");
        try {
            // if topology is saved in config file
            if(topology != null)
            {
                System.out.println("topology from config");
               StringTokenizer st = new StringTokenizer(topology);
                while (st.hasMoreTokens()) {
                    int neighbor = Integer.parseInt(st.nextToken());
                    neighbors.add(neighbor);
                }   
            }
            else
            {
                // read topology from specific file
                BufferedReader dIn = new BufferedReader(new FileReader("D:/sync1/tp" + myId+".txt"));
                StringTokenizer st = new StringTokenizer(dIn.readLine());
                while (st.hasMoreTokens()) {
                    int neighbor = Integer.parseInt(st.nextToken());
                    neighbors.add(neighbor);
                }  
            }
            
        } catch (FileNotFoundException e) {
            Util.println("No topology found",e);
            for (int j = 0; j < N; j++)
                if (j != myId) neighbors.add(j);
        } catch (IOException e) {
            Util.println(e.getMessage(),e);
        }
        Util.println(neighbors.toString());
    }
    
   public static void createConfigFile(String base, String id, String n, String sign, String log,String wait,boolean req) throws IOException
    {
        Properties p = new Properties();  
        p.setProperty("base",base); 
        p.setProperty("myid",id);  
        p.setProperty("n",n);
        String path = "F:\\workspace\\MutualExclusion_new\\config";
        p.setProperty("log", log);
        p.setProperty("fileserverhost", "127.0.0.1");
        p.setProperty("fileserverport", "12000");
        p.setProperty("logserverurl", "127.0.0.1:4587");
        p.setProperty("nameserver", "127.0.0.1");
        p.setProperty("nameserverport", "12001");
        p.setProperty("wait", wait);
         p.setProperty("printonconsole", "true");
        p.setProperty("canRequest", String.valueOf(req));
        p.setProperty("propogationDelay", "0");
        //p.setProperty("appport", st.nextToken());
        int myId = Integer.parseInt(id);
        int N = Integer.parseInt(n);
        int sqrtN = (int)Math.sqrt(N);
        int top = (N + myId - sqrtN)% N;
        int bottom = (myId + sqrtN) % N;
        int right = (myId + 1 )% sqrtN == 0? (myId - sqrtN + 1) : (myId + 1);
        int left = myId % sqrtN == 0 ? (myId + sqrtN - 1 ): (myId - 1);
        p.setProperty("topology", right+" "+bottom + " "+top+ " "+left);
        System.out.println("NOde "+myId + " topology "+right+" "+bottom + " "+top+ " "+left);
        p.store(new FileWriter(path+"\\"+"config_"+id+".properties"),"Javatpoint Properties Example");  
    }
    
    
    public static void main(String[] args) throws IOException {
        HashSet<Integer> set = new HashSet<>();
        set.add(0);
       // set.add(1);
        /*set.add(2);
        set.add(3);
        set.add(20);
        set.add(4);
        set.add(9);
        set.add(14);
        set.add(19);
        set.add(24);*/
        for (int i = 0; i < 4; i++) {
            createConfigFile("node_", String.valueOf(i), "4", "false", "false","100", true);
        }
    }
    }
