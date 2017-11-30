// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.utils;

import utils.FileUtils;
import model.TagInfo;
import com.sqlite.model.Order;
import com.sqlite.domain.ContextManager;
import boot.Pass;
import com.google.gson.Gson;

public class FileManager
{
    private static Gson gson;
    
    public static void savePass(final Pass pass) {
        final Order currentOrder = ContextManager.currentOrder;
        final String fileNam = currentOrder.getOrderNum() + ".txt";
        if (ContextManager.lastPass != null) {
            ContextManager.lastPass.setState("ABNORMAL_HEADER");
            saveOnePass(ContextManager.lastPass, fileNam);
            ContextManager.lastPass = null;
        }
        saveOnePass(pass, fileNam);
    }
    
    private static void saveOnePass(final Pass pass, final String fileNam) {
        final String tt = FileManager.gson.toJson((Object)pass) + "\n";
        System.out.println(pass.getState() + "\uff0c" + pass.getSku() + "," + pass.getTagInfos().iterator().next().getEpc());
        FileUtils.createWrite(tt, fileNam);
    }
    
    public static void saveCurrentOrder() {
        final Order currentOrder = ContextManager.currentOrder;
        final String fileNam = currentOrder.getOrderNum() + "-order-detail.txt";
        final String tt = FileManager.gson.toJson((Object)currentOrder);
        FileUtils.createWrite(tt, fileNam);
    }
    
    static {
        FileManager.gson = new Gson();
    }
}
