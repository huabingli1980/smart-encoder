// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
//import org.restlet.resource.ClientResource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.sqlite.dao.PassInspectDao;
import com.sqlite.domain.ContextManager;

import boot.ApplicationConfig;

@RestController
public class ReportController
{
    @Autowired
    private PassInspectDao passInspectDao;
    
    @RequestMapping(value = { "/saveConfig" }, produces = { "application/json" })
    @ResponseBody
    public Properties saveConfig(@RequestBody final String prop) throws FileNotFoundException, IOException, URISyntaxException {
        final Gson gson = new Gson();
        final Map<String, String> mp = new HashMap<String, String>();
        final Map<String, String> myMap = (Map<String, String>)gson.fromJson(prop, mp.getClass());
        ApplicationConfig.setAndSaveAll(myMap);
        System.out.println(prop);
        return ApplicationConfig.me(myMap.get("name"));
    }
    
    @RequestMapping(value = { "/getConfig" }, produces = { "application/json" })
    @ResponseBody
    public Properties getConfig(final String name) throws FileNotFoundException, IOException {
        return ApplicationConfig.me(name);
    }
    
    @RequestMapping(value = { "/getConfigNames" }, produces = { "application/json" })
    @ResponseBody
    public String[] getConfigNames() {
    	
        final File file = new File(System.getProperty("user.dir"));
        final String[] names = file.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
		});
        return names;
    }
    
    @RequestMapping(value = { "/save" }, produces = { "application/json" })
    @ResponseBody
    public Map filterAndSaveReport(@RequestParam("badTag[]") final Set<String> fo, final String orderNumber) throws IOException, SQLException {
        this.passInspectDao.updateDamage(ContextManager.dataStr, (Set)fo, orderNumber);
        final List<Object[]> list = (List<Object[]>)this.passInspectDao.getAllByOrderNum(ContextManager.dataStr, orderNumber);
        final File f = new File(orderNumber + ".txt");
        final File fx = new File(orderNumber + "-context.txt");
        final File qcDataFile = new File(orderNumber + ".csv");
        if (!qcDataFile.exists()) {
            qcDataFile.createNewFile();
        }
        Files.write(qcDataFile.toPath(), "EPC,TID,Status,Time,CodeType\n".toString().getBytes(), StandardOpenOption.APPEND);
        for (final Object[] object : list) {
            final Integer seq = (Integer)object[0];
            final String tid = (String)object[1];
            final String epc = (String)object[2];
            final String time = (String)object[3];
            final String status = (String)object[4];
            final String orderNum = (String)object[5];
            System.out.println(seq);
            final String line = String.valueOf(epc) + "," + String.valueOf(tid) + "," + String.valueOf(status) + "," + String.valueOf(time) + "," + "chipType" + "\n";
            Files.write(qcDataFile.toPath(), line.toString().getBytes(), StandardOpenOption.APPEND);
        }
        final Path path = qcDataFile.toPath();
        final String inspectDataFilePath = orderNumber + "-inspect-data.csv";
        final File inspectDataFile = new File(inspectDataFilePath);
        if (!inspectDataFile.exists()) {
            inspectDataFile.createNewFile();
        }
        final Path myinspectDataFilePath = inspectDataFile.toPath();
        final Map m = new HashMap();
        m.put("filepath", path.toAbsolutePath().toString());
        m.put("inspect_data_filepath", myinspectDataFilePath.toAbsolutePath().toString());
        return m;
    }
    
   /* public static void main(final String[] args) {
        new ReportController().getBarcodeByEPC();
    }
    
    private void getBarcodeByEPC() {
        final ClientResource cr = new ClientResource("http://www.rfidcoder.com/api/tag/epc/30F4257BF46DB64000000190?apikey=CiEMpeq6s5YI58B0");
    }*/
    
    @CrossOrigin(origins = { "*" })
    @RequestMapping(value = { "/echo" }, produces = { "application/json" })
    @ResponseBody
    public String echo(final String json) {
        System.out.println("echo...");
        return json;
    }
    
    public String valueAsString(final String value) {
        return "\"=\"\"" + value + "\"\"\"";
    }
}
