package com.wzj.wordCount;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DealFile {

    // 处理的文件
    private File file = null;
    //线程数量
    private Integer threadNum;
    // 线程表
    private List<CountWordsThread> listCountWordsThreads = null;
    //private List<Thread> listThread = null;
    // 文件分割大小
    private long splitSize;
    // 当前处理的文件位置
    private long currentPos;

    public DealFile(File file) {
        this.file = file;
        this.threadNum = 3;
        this.listCountWordsThreads = new ArrayList<>();
        //this.listThread = new ArrayList<>();
        this.splitSize = 1024;
        this.currentPos = 0;
    }

    public void doFile() {

        long length = file.length();

        ExecutorService executorService = Executors.newCachedThreadPool();

        while (currentPos < length) {

            for (int i = 0; i < threadNum; i++) {

                if (currentPos < length) {
                    CountWordsThread countWordsThread = null;
                    if (currentPos + splitSize < length) {
                        RandomAccessFile randomAccessFile = null;
                        try {
                            randomAccessFile = new RandomAccessFile(this.file, "r");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        try {
                            randomAccessFile.seek(currentPos + splitSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int offset = 0;
                        while (true) {
                            char ch = 0;
                            try {
                                ch = (char) randomAccessFile.read();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            //是否到文件末尾，到了跳出
                            if (-1 == ch)
                                break;
                            //是否是字母和'，都不是跳出（防止单词被截断）
                            if (!Character.isLetter(ch) && '\'' != ch)
                                break;

                            offset++;
                        }
                        countWordsThread = new CountWordsThread(this.file, currentPos, splitSize + offset);
                        currentPos += splitSize + offset;
                        try {
                            randomAccessFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        countWordsThread = new CountWordsThread(this.file, currentPos, length - currentPos);
                        currentPos += length - currentPos;
                    }

                    //Thread thread = new Thread(countWordsThread);
                    //System.out.println("thread do file:" + thread.getName());
                    //thread.start();
                    // this.listThread.add(thread);
                    this.listCountWordsThreads.add(countWordsThread);
                    executorService.execute(countWordsThread);
                }
            }

            while (true) {
                boolean flag = true;
                if (!executorService.isTerminated()) {
                    flag = false;
                    break;
                }
                if (flag) {
                    break;
                }
            }
            // 判断Map线程是否全部结束
//            while (true) {
//                boolean flag = true;
//                for (int i = 0; i < listThread.size(); i++) {
//                    if (listThread.get(i).getState() != Thread.State.TERMINATED) {
//                        flag = false;
//                        break;
//                    }
//                }
//                if (flag) {
//                    break;
//                }
//            }
        }

        // 开始Reduce
        new Thread(() -> {
            TreeMap<String, Integer> treeMap = new TreeMap<>();
            for (int i = 0; i < this.listCountWordsThreads.size(); i++) {
                Map<String, Integer> hashMap = listCountWordsThreads.get(i).getHashMap();
                for (String key :
                        hashMap.keySet()) {
                    if (key.equals("")) {
                        continue;
                    }
                    if (treeMap.get(key) == null) {
                        treeMap.put(key, hashMap.get(key));
                    } else {
                        treeMap.put(key, treeMap.get(key) + hashMap.get(key));
                    }
                }
            }
            //关闭线程池
            executorService.shutdown();

            for (String key :
                    treeMap.keySet()) {
                System.out.println("Word:" + key + "  Count:" + treeMap.get(key));
            }
        }).start();
    }
}
