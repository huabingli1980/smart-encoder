// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.messaging;

import com.sqlite.license.model.LicenseFailureMessage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import boot.Pass;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageManager
{
    @Autowired
    private SimpMessagingTemplate template;
    private Gson gson;
    
    public MessageManager() {
        this.gson = new Gson();
    }
    
    public void sendPass(final Pass pass) {
        System.out.println("pass...");
        this.template.convertAndSend("/topic/greetings", this.gson.toJson((Object)pass));
    }
    
    public void sendAdd(final String epc, final String tid, final String status) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("epc", epc);
        map.put("tid", tid);
        map.put("status", status);
        this.template.convertAndSend("/topic/add", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendReadEPC(final String epc, final String tid) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("epc", epc);
        map.put("tid", tid);
        this.template.convertAndSend("/topic/input", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendGpiChange() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("time", String.valueOf(System.currentTimeMillis()));
        this.template.convertAndSend("/topic/detected", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendProdeTrueSignal(final long currentTimeMillis) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("signal", "true");
        map.put("time", String.valueOf(currentTimeMillis));
        this.template.convertAndSend("/topic/prode", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendProdeFalseSignal(final long currentTimeMillis) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("signal", "false");
        map.put("time", String.valueOf(currentTimeMillis));
        this.template.convertAndSend("/topic/prode", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendPrinterStopSignal() {
        final Map<String, String> map = new HashMap<String, String>();
        this.template.convertAndSend("/topic/printStop", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendReenterEPC(final List<String> tagInfos) {
        final Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("epcs", tagInfos);
        this.template.convertAndSend("/topic/reenter", (Object)this.gson.toJson((Object)map));
    }
    
    public void sendLicenseFailureMessage(final LicenseFailureMessage licenseFailureMessage) {
    }
}
