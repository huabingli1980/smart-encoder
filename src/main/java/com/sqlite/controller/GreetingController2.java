package com.sqlite.controller;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageMapping;
import com.sqlite.utils.ReaderManager;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileNotFoundException;
import boot.ApplicationConfig;
import com.sqlite.domain.OIC;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.Map;
import com.google.gson.Gson;
import java.util.HashMap;
import com.sqlite.domain.ContextManager;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import model.InspectContext;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.impinj.octane.OctaneSdkException;
import com.sqlite.dao.PassInspectDao;
import com.sqlite.model.Mock;
import com.sqlite.messaging.MessageManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;

import com.sqlite.utils.ReaderProxy;

@Controller
public class GreetingController2 {
	@Autowired
	private ReaderProxy scannerMain;
	@Autowired
	private MessageManager messageManager;
	@Autowired
	private Mock mock;
	@Autowired
	private PassInspectDao passInspectDao;
	
	@Autowired
    BuildProperties buildProperties;
	
	@RequestMapping("/startProde")
	@ResponseBody
	public String startProde() throws OctaneSdkException {

		return "ok";
	}

	@RequestMapping(value = "/appendOrder", method = RequestMethod.POST)
	@ResponseBody
	public String appendOrder(@RequestBody final InspectContext inspectContext) {
		ContextManager.addOrders(inspectContext.getOrders());
		final Map<String, String> map = new HashMap<String, String>();
		map.put("resl", "ok");
		return new Gson().toJson((Object) map);
	}

	@RequestMapping(value = "/startInspection", method = RequestMethod.POST)
	@ResponseBody
	public String startInspection(@RequestBody final InspectContext inspectContext)
			throws OctaneSdkException, FileNotFoundException, IOException {
		OIC.reset();
		OIC.hasStarted = true;
		ApplicationConfig.load(inspectContext.getConfigName() + ".properties");
		ContextManager.addOrders(inspectContext.getOrders());
		ContextManager.init();
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mock.doIt();
				} catch (OctaneSdkException e) {
					e.printStackTrace();
				}
			}
		}).start();

		final Map<String, String> map = new HashMap<String, String>();
		map.put("resl", "ok");
		return new Gson().toJson((Object) map);
	}

	@RequestMapping("/stopInspection")
	@ResponseBody
	public String stopInspection() throws OctaneSdkException {
		if (ContextManager.currentOrder != null) {
			ContextManager.saveOrder(ContextManager.currentOrder);
		}
		OIC.reset();
		OIC.hasStarted = false;
		final Map<String, String> map = new HashMap<String, String>();
		map.put("resl", "ok");
		return new Gson().toJson((Object) map);
	}

	@RequestMapping("/startAutomaticReading")
	@ResponseBody
	public String startAutomaticReading(final String receiveQueueName) {
		if ("input".equals(receiveQueueName)) {
			for (int i = 0; i < 3; ++i) {
				this.messageManager.sendReadEPC("epc" + i, "tid" + i);
			}
		}
		if ("reenter".equals(receiveQueueName)) {
			final List<String> tagInfos = new ArrayList<String>();
			for (int j = 0; j < 3; ++j) {
				tagInfos.add("epc" + j + ",tid" + j);
			}
			this.messageManager.sendReenterEPC((List) tagInfos);
		}
		if ("add".equals(receiveQueueName)) {
			this.messageManager.sendAdd("testadd", "testaddtid", "GOOD");
		}
		String resl = "ok";
		try {
			this.scannerMain.startRead(receiveQueueName);
		} catch (OctaneSdkException e) {
			e.printStackTrace();
			resl = "failed";
		}
		return resl;
	}

	@RequestMapping("/stopAutomaticReading")
	@ResponseBody
	public String stopAutomaticReading() {
		final String resl = "ok";
		return resl;
	}

	@MessageMapping("/connect")
	@SendTo("/topic/error")
	public Map<String, String> connectReader() throws Exception {
		ReaderManager.getReader();
		final HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("result", "ok");
		return hashMap;
	}

	@RequestMapping(value = "/updateEmptyRead", method = RequestMethod.POST)
	@ResponseBody
	public String updateEmptyRead(@RequestParam final String epc, @RequestParam final String tid, final int rowId) {
		String resl = "failed";
		final int updatedRowCount = this.passInspectDao.updateEmptyRead(rowId, epc, tid);
		if (updatedRowCount == 1) {
			resl = "ok";
			try {
				ReaderManager.stop();
				this.scannerMain.listenGpiEvent();
			} catch (OctaneSdkException e) {
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
