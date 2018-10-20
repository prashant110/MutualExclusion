/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package msg;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import util.Util;

/**
 *
 * @author Prashant
 */
public class Converter {
    // convert json to string

    /**
     * convert json object into json string
     * @param obj
     * @return string
     */
    public static String jsonToString(JSONObject obj)
    {
        return obj.toJSONString();
    }
    
    /**
     * convert key value pair into json object
     * @param obj jsonObject
     * @param args key value pair for json
     * @return jsonObjects
     */
    public static JSONObject toJson(JSONObject obj, HashMap<String,String> args)
    {
        JSONObject json = obj == null ? new JSONObject() : obj;
        json.putAll(args);
        return json;
    }
    
    public static JSONObject toJson(JSONObject obj, String key, String val)
    {
        JSONObject json = obj == null ? new JSONObject() : obj;
        json.put(key,val);
        return json;
    }
    
    /**
     * Add array into json object
     * @param obj JSON object
     * @param key key for json
     * @param val array of value
     * @return json object
     */
    public static JSONObject addArrayTOJson(JSONObject obj, String key,int[] val)
    {
        JSONObject json = obj == null ? new JSONObject() : obj;
        JSONArray arr = new JSONArray();
        for(int v: val)
            arr.add(v);
        json.put(key, arr);
        return json;
    }
    public static JSONObject addArrayTOJson(JSONObject obj, String key,ArrayList<Integer> val)
    {
        JSONObject json = obj == null ? new JSONObject() : obj;
        JSONArray arr = new JSONArray();
        for(Integer v: val)
            arr.add(v);
        json.put(key, arr);
        return json;
    }
    
    public static JSONObject addSetTOJson(JSONObject obj, String key,HashSet<Integer> val)
    {
        JSONObject json = obj == null ? new JSONObject() : obj;
        JSONArray arr = new JSONArray();
        for(Integer v: val)
            arr.add(v);
        json.put(key, arr);
        return json;
    }
    /**
     * convert the given json String into json Object
     * @param str jsonString
     * @return jsonObject
     */
    public static JSONObject stringToJson(String str)
    {
        JSONParser parser = new JSONParser();
        JSONObject obj = null;
        try {
                obj = (JSONObject) parser.parse(str);
        } catch (ParseException e) {
                Util.println(e.getMessage(),e);
        }
        return obj;
    }
    
   
    
    public static void getArrayList(JSONObject obj , String key, ArrayList<Integer> list)
    {
        JSONArray arr = (JSONArray)obj.get(key);
        for(int i = 0; i < arr.size(); i++)
            list.add((int)(long)arr.get(i));
}
    
    public static void getSet(JSONObject obj , String key, HashSet<Integer> list)
    {
        JSONArray arr = (JSONArray)obj.get(key);
        for(int i = 0; i < arr.size(); i++)
            list.add((int)(long)arr.get(i));
    }
    
    public static void getArray(JSONObject obj , String key, int[] arrResult)
    {
        JSONArray arr = (JSONArray)obj.get(key);
        for(int i = 0; i < arr.size(); i++)
            arrResult[i] = ((int)(long)arr.get(i));
    }
    
    public static void main(String[] args) {
        HashMap<String,String> map = new HashMap<>();
        map.put("a","b");
        ArrayList<Integer> l = new ArrayList<>();
        l.add(1);
        l.add(2);
        l.add(3);
        JSONObject obj = toJson(null, map);
        obj = addArrayTOJson(obj, "run", l);
        System.out.println("obj = " + jsonToString(obj));
        JSONObject k = stringToJson(jsonToString(obj));
        System.out.println("k = " + k.get("a"));
        System.out.println("k = " + k.get("run"));
        JSONArray arrd = (JSONArray)k.get("run");
        int[] as = new int[arrd.size()];
        for(int i = 0; i < arrd.size(); i++)
            as[i] = (int)(long)arrd.get(i);
        
        for (int i = 0; i < as.length; i++) {
            System.out.print(as[i] + " ");
        }
    }
}
