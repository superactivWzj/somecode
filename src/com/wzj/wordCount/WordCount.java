package com.wzj.wordCount;

import java.io.File;

public class WordCount {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        File file = new File("/Users/weizijian/Downloads/word.txt");
        DealFile dealFile = new DealFile(file);
        dealFile.doFile();
        long end = System.currentTimeMillis();

        System.out.println("处理时间：" + (end - start) + "ms");
    }
}
