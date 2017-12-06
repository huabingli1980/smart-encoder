package com.sqlite.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.impinj.octane.OctaneSdkException;
import com.sqlite.dao.PassInspectDao;
import com.sqlite.domain.ContextManager;
import com.sqlite.domain.OIC;
import com.sqlite.utils.ReaderManager;
import com.sqlite.utils.ReaderProxy;

import boot.ApplicationConfig;
import model.InspectContext;

@Controller
public class GreetingController
{
    @Autowired
    private ReaderProxy scannerMain;
    @Autowired
    BuildProperties buildProperties;
    @Autowired
    private PassInspectDao passInspectDao;
    
    @RequestMapping({ "/startProde" })
    @ResponseBody
    public String startProde() throws OctaneSdkException {
        return "ok";
    }
    
    @RequestMapping(value = { "/appendOrder" }, method = { RequestMethod.POST })
    @ResponseBody
    public String appendOrder(@RequestBody final InspectContext inspectContext) {
        ContextManager.addOrders(inspectContext.getOrders());
        final Map<String, String> map = new HashMap<String, String>();
        map.put("resl", "ok");
        return new Gson().toJson((Object)map);
    }
    
    @RequestMapping(value = { "/startInspection" }, method = { RequestMethod.POST })
    @ResponseBody
    public String startInspection(@RequestBody final InspectContext inspectContext) throws OctaneSdkException, FileNotFoundException, IOException {
        OIC.reset();
        ReaderManager.disconnect();
        OIC.hasStarted = true;
        ApplicationConfig.load(inspectContext.getConfigName() + ".properties");
        ContextManager.addOrders(inspectContext.getOrders());
        ContextManager.init();
        this.scannerMain.listenGpiEvent();
        final Map<String, String> map = new HashMap<String, String>();
        map.put("resl", "ok");
        return new Gson().toJson((Object)map);
    }
    
    @RequestMapping({ "/stopInspection" })
    @ResponseBody
    public String stopInspection() throws OctaneSdkException {
        if (ContextManager.currentOrder != null) {
            ContextManager.saveOrder(ContextManager.currentOrder);
        }
        OIC.reset();
        OIC.hasStarted = false;
        final Map<String, String> map = new HashMap<String, String>();
        map.put("resl", "ok");
        return new Gson().toJson((Object)map);
    }
    
    @RequestMapping({ "/startAutomaticReading" })
    @ResponseBody
    public String startAutomaticReading(final String receiveQueueName) {
        String resl = "ok";
        try {
            this.scannerMain.startRead(receiveQueueName);
        }
        catch (OctaneSdkException e) {
            e.printStackTrace();
            resl = "failed";
        }
        return resl;
    }
    
    @RequestMapping({ "/stopAutomaticReading" })
    @ResponseBody
    public String stopAutomaticReading() {
        String resl = "ok";
        try {
            ReaderManager.stop();
        }
        catch (OctaneSdkException e) {
            e.printStackTrace();
            resl = "failed";
        }
        return resl;
    }
    
    @MessageMapping({ "/connect" })
    @SendTo({ "/topic/error" })
    public Map<String, String> connectReader() throws Exception {
        ReaderManager.getReader();
        ReaderManager.stop();
        final HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("result", "ok");
        return hashMap;
    }
    
    @RequestMapping({ "/updateEmptyRead" })
    @ResponseBody
    public String updateEmptyRead(@RequestParam final String epc, @RequestParam final String tid, final int rowId) {
        String resl = "failed";
        final int updatedRowCount = this.passInspectDao.updateEmptyRead(rowId, epc, tid);
        if (updatedRowCount == 1) {
            resl = "ok";
            try {
                ReaderManager.stop();
                this.scannerMain.listenGpiEvent();
            }
            catch (OctaneSdkException e) {
                e.printStackTrace();
            }
        }
        return resl;
    }
    
    @CrossOrigin(origins = { "*" })
    @RequestMapping(value = { "/version" }, method = { RequestMethod.GET })
    @ResponseBody
    public String updateAdd() {
        return new Gson().toJson(this.buildProperties);
    }
}
