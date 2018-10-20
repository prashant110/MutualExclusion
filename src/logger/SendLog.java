/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logger;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import msg.Converter;
import util.Util;

/**
 *
 * @author Prashant
 */
public class SendLog {
    
	/*String host;
	int port;
	Socket socket;
	PrintWriter pw ;
	
	public SendLog(String host, int port)
	{
		try
		{
			this.host = host;
			this.port = port;
			socket = new Socket(host,port);
			 pw = new PrintWriter(socket.getOutputStream());
		}
		catch(Exception ex)
		{
			Util.println("Exception ex "+ex.getMessage() ,ex);
		}
	}
	

	public synchronized void sendMessage(int src, int des, String tag, String msg,String other)
	{
		try
        {
			
            LocalDateTime now = LocalDateTime.now();
            HashMap<String,String> map = new HashMap<>();
            map.put("time", now.toString());
            map.put("src",  String.valueOf(src));
            map.put("des", String.valueOf(des));
            map.put("tag", tag);
            map.put("msg", msg);
            map.put("other", other);
            String data = Converter.jsonToString(Converter.toJson(null, map));
            pw.println(data);
            pw.flush();
            
            
        }
        catch(Exception ec)
        {
            Util.println("exception in sendlog sendMessage = " + ec,ec);
            //ec.printStackTrace();
        }
	}
	*/
	String url;
	public SendLog(String url)
	{
		this.url = url;
	}
	public synchronized void sendMessage(int src, int des, String tag, String msg,String other) 
	 {
        try
        {
        	
            LocalDateTime now = LocalDateTime.now();
            HashMap<String,String> map = new HashMap<>();
            map.put("time", now.toString());
            map.put("src", String.valueOf(src));
            map.put("des", String.valueOf(des));
            map.put("tag", tag);
            map.put("msg", msg);
            map.put("other", other);
            String data = Converter.jsonToString(Converter.toJson(null, map));
            HttpClient httpclient = new DefaultHttpClient();
            StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
           

        	HttpPost postMethod = new HttpPost(url);
        	postMethod.setEntity(requestEntity);

        	HttpResponse rawResponse = httpclient.execute(postMethod);
            
        }
        catch(Exception ec)
        {
            Util.println("exception in sendlog sendMessage = " + ec,ec);
            //ec.printStackTrace();
        }
	 }
	/*Socket socket;
	String path;
	BufferedWriter wr;
	PrintWriter pr;	
	String host;
	int  port;
	BufferedReader rd;
	public SendLog(String host,int port)
	{
		path = "/log";
		this.host = host;
		this.port = port;
		try
		{
			socket = new Socket(host, port);
			pr = new PrintWriter(socket.getOutputStream());
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception ex)
		{
			Util.println("Exception "+ex.getMessage(), ex);
		}
	}
	
	 public synchronized void sendMessage(int src, int des, String tag, String msg,String other) 
	 {
	        try
	        {
	        	
	            LocalDateTime now = LocalDateTime.now();
	            HashMap<String,String> map = new HashMap<>();
	            map.put("time", now.toString());
	            map.put("src", String.valueOf(src));
	            map.put("des", String.valueOf(des));
	            map.put("tag", tag);
	            map.put("msg", msg);
	            map.put("other", other);
	            String data = Converter.jsonToString(Converter.toJson(null, map));
	            //wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));	
	            pr.println("POST " + path + " HTTP/1.0");
	            pr.println("Content-Length: " + data.length());
	            pr.println("Content-Type: application/json");
	            pr.println("");
	            pr.println(data);
	            pr.flush();

			   
			    String line;
			    while ((line = rd.readLine()) != null) {
			      System.out.println(line);
			    }
			    //rd.close();
			    //wr.close();
			    //socket.close();
			
	        }
	        catch(Exception ec)
	        {
	            Util.println("exception in sendlog sendMessage = " + ec,ec);
	            //ec.printStackTrace();
	        }
	    }*/
   /* String serverURL;
    OutputStream os;
    HttpURLConnection con; 
    public SendLog(String urlString) 
    {
    	try
    	{
	    	serverURL = urlString;
	    	
	        
    	}
    	catch(Exception ec)
    	{
    		Util.println("exception in sendlog sendMessage = " + ec,ec);
    	}
    }
    
    public void sendMessage(int src, int des, String tag, String msg,String other) 
    {
        try
        {
        	System.setProperty("java.net.preferIPv4Stack" , "true");
	    	
        	URL obj = new URL(serverURL);
        	con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
	        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	        con.setDoOutput(true);
//        	/os = con.getOutputStream();
            //message for browser application send to node.js application
            LocalDateTime now = LocalDateTime.now();
            HashMap<String,String> map = new HashMap<>();
            map.put("time", now.toString());
            map.put("src", String.valueOf(src));
            map.put("des", String.valueOf(des));
            map.put("tag", tag);
            map.put("msg", msg);
            map.put("other", other);
            String message = Converter.jsonToString(Converter.toJson(null, map));
            try (OutputStream os = con.getOutputStream()) 
            {
        		os.write(message.getBytes());
        		os.flush();
            }
            int responseCode = con.getResponseCode();
            //os.close();
            con.disconnect();
		
        }
        catch(Exception ec)
        {
            Util.println("exception in sendlog sendMessage = " + ec,ec);
            //ec.printStackTrace();
        }
    }
    */
    public static void main(String args[])
    {
    	/*SendLog s = new SendLog("127.0.0.1",4587);
    	int i = 0;
    	while(i < 100000)
    	{
    		System.out.println("sending "+i);
    		s.sendMessage(i, 1, "execution_completed", "s", "sdf");
    		i++;
    	}*/
    }
    
}
