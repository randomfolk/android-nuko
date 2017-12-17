package rehanced.com.simplenukowallet.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rehanced.com.simplenukowallet.activities.MainActivity;
import rehanced.com.simplenukowallet.interfaces.StorableWallet;

/**
 * Used for temporary caching of Txinfo. Clears once android garbage collects
 */
public class TxCache {

    private HashMap<String, String> map = new HashMap<String, String>();
    private static TxCache instance;
    private File filepath;
    private int counter;
    public static TxCache getInstance() {
        if (instance == null)
            instance = new TxCache();
        return instance;
    }

    public void setFilepath(File path){
        filepath = path;
    }
    public  void put(String hash, String response) {
        map.put(hash, response);
        Log.d("Txcache","put " + map.size() );
        saveToFile();
    }
    public synchronized void saveToFile(){
        FileOutputStream fout;
        try {
        fout = new FileOutputStream(new File(filepath, "txs.dat"));
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(map);
        oos.close();
        fout.close();
         } catch (Exception e) {    }

    }
    //call only once when open the app
    public synchronized void loadFromFile() {
        try {
            FileInputStream fout = new FileInputStream(new File(filepath, "txs.dat"));
            ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
            map = (HashMap<String, String> ) oos.readObject();
            oos.close();
            fout.close();
            Log.d("Txcache","load from file " + map.toString() );

        } catch (Exception e) {

            Log.d("Txcache","load from file Exception " + e.getMessage() );
        }
    }

    public String get(String hash) {
        return map.get(hash);
    }

    public boolean isEmpty(){
        return map.isEmpty();
    }
    public Set<String> getAllKeys(){
        return map.keySet();
    }
    public Set<String> getKeysToUpdate(){
        Set <String> result=new HashSet<String>();
        for (String key : map.keySet()) {
            String content = map.get(key);
            try{
            JSONObject obj = new JSONObject(content);
            if (obj.getInt("confirmations") < 13){
                Log.d("txcache","getkeystoupdate " + key);
                result.add(key);
            }}
            catch (Exception e){
                result.add(key);
                Log.d("txcache","getkeystoupdate " + e.getMessage());
            }
        }
        return result;
    }

    public boolean contains(String hash) {
        return map.containsKey(hash);
    }

}
