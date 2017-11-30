// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.utils;

import java.nio.file.Files;
import java.nio.charset.Charset;
import org.mozilla.universalchardet.UniversalDetector;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import boot.ApplicationConfig;
import java.io.IOException;

public class OrderFileUtils
{
    public static void main(final String[] args) throws IOException {
        final String orderNumber = "4533640001";
        final int orderQuantity = getOrderQuantityByOrderNum(orderNumber, false);
        System.out.println(orderQuantity);
    }
    
    private static int getOrderQuantityByOrderNum(final String orderNumber, final boolean isMancy) throws IOException {
        final String orderFileHome = ApplicationConfig.get("path.order.data", "C:\\Users\\nsrfid 115\\Desktop\\data find");
        if(orderFileHome == null){
        	return -2;
        }
        	
        final String[]  filesStartWith = getFilesStartWith(orderFileHome, orderNumber);
        if(filesStartWith == null){
        	return -2;
        }
        
        for (final String name : filesStartWith) {
            final File file = new File(orderFileHome + "\\" + name);
            System.out.println("parsing..." + file);
            final int orderQuantity = parseFile(orderNumber, file, isMancy);
            System.out.println(orderQuantity);
            if (orderQuantity != 0) {
                return orderQuantity;
            }
        }
        return -2;
    }
    
    public static String[] getFilesStartWith(final String dirWay, final String orderNum) {
        final File directory = new File(dirWay);
        final String[] listFiles = directory.list();
        return listFiles;
    }
    
    private static int parseFile(final String orderNumber, final File file, final boolean isMancy) throws IOException {
        final Set<String> map = new HashSet<String>();
        final List<String> lines = (List<String>)readLines(file);
        final int totalQuantity = 0;
        for (int i = lines.size() - 1; i >= 0; --i) {
            final String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                System.out.println(line + " vs " + orderNumber);
                final int indexOf = line.indexOf(orderNumber);
                final boolean match = indexOf > -1;
                final boolean isEndingLine = !line.startsWith("..");
                if (match) {
                    if (isEndingLine) {
                        break;
                    }
                    final int start = indexOf + orderNumber.length();
                    final String sku = line.substring(start, start + 3);
                    if (isMancy) {
                        final String[] splits = line.split("\\s+");
                        final String lastSection = splits[splits.length - 1];
                        final String lastTwo = lastSection.substring(0, 2);
                        System.out.println(lastTwo);
                        map.add(lastTwo);
                    }
                    else {
                        System.out.println("adding " + sku);
                        map.add(sku);
                    }
                }
            }
        }
        if (isMancy && map.size() == 1 && map.contains("AA")) {
            return -1;
        }
        return map.size();
    }
    
    private static List<String> readLines(final File file) throws IOException {
        final String[] charsetsToTry = { "UTF-16", "UTF-8" };
        String charsetDetected = UniversalDetector.detectCharset(file);
        if (charsetDetected == null) {
            charsetDetected = "UTF-8";
        }
        return Files.readAllLines(file.toPath(), Charset.forName(charsetDetected));
    }
    
    private static boolean isOrderInFile(final File file, final String orderNumber) throws IOException {
        final String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        return content.indexOf(orderNumber) > -1;
    }
    
    private static List<File> getFilesCreatedDaysAgo(final String orderFileHome, final int daysAgo) {
        return null;
    }
    
    public static int getSkuCountByOrderNum(final String orderNum, final int orderType) {
        final boolean isMancy = orderType == 1;
        int orderQuantity;
        try {
            orderQuantity = getOrderQuantityByOrderNum(orderNum, isMancy);
        }
        catch (IOException e) {
            throw new RuntimeException("Something wrong with IO", e);
        }
        final int skuCount = orderQuantity;
        System.out.println(skuCount);
        return skuCount;
    }
}
