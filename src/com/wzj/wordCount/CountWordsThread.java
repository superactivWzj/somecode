package com.wzj.wordCount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class CountWordsThread implements Runnable{

    private FileChannel fileChannel = null;
    private FileLock lock = null;
    private MappedByteBuffer mbBuf = null;
    private Map<String, Integer> hashMap = null;

    public CountWordsThread(File file,long start,long size) {

        try {
            fileChannel = new RandomAccessFile(file,"rw").getChannel();
            lock = fileChannel.lock(start,size,false);
            mbBuf = fileChannel.map(FileChannel.MapMode.READ_ONLY,start,size);
            hashMap = new HashMap<>();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String str = Charset.forName("utf-8").decode(mbBuf).toString();
        str = str.toLowerCase();
        String[] strings = str.split("[^a-zA-Z']+");
        for (String e :
                strings) {
            if (e.equals("")) continue;
            if (hashMap.get(e)==null){
                hashMap.put(e,1);
            }else {
                hashMap.put(e,hashMap.get(e)+1);
            }

        }

        try {
            lock.release();
            fileChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Map<String, Integer> getHashMap() {
        return hashMap;
    }
}
