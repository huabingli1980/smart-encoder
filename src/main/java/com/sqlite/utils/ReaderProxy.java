package com.sqlite.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.GpiChangeListener;
import com.impinj.octane.GpiConfig;
import com.impinj.octane.GpiConfigGroup;
import com.impinj.octane.GpiEvent;
import com.impinj.octane.GpoConfig;
import com.impinj.octane.GpoMode;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReaderStartEvent;
import com.impinj.octane.ReaderStartListener;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.rsmart.Rx;
import com.sqlite.domain.BasicTagInfoConsumer;
import com.sqlite.domain.OIC;

import boot.ApplicationConfig;
import boot.Pass;
import boot.PassFactory;

@Component
public class ReaderProxy {
	private List<Tag> tags;

	@Autowired
	private BasicTagInfoConsumer tagReader;

	public void listenGpiEvent() throws OctaneSdkException {
		final ImpinjReader reader = ReaderManager.getReader();
		final Settings settings = reader.queryDefaultSettings();
		settings.setSearchMode(SearchMode.SingleTarget);
		settings.setTagPopulationEstimate(1);
		settings.setSession(2);
		final AntennaConfigGroup ac = settings.getAntennas();
		ac.disableAll();
		final Short valueOf = Short.valueOf(ApplicationConfig.get("detect.ant", new String[0]));
		final AntennaConfig antenna = ac.getAntenna((Number) valueOf);
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm(Short.valueOf(ApplicationConfig.get("detect.power", new String[0])));
		antenna.setIsMaxRxSensitivity(false);
		antenna.setRxSensitivityinDbm(Short.valueOf(ApplicationConfig.get("receive.sensitivity", new String[0])));
		final ReportConfig report = settings.getReport();
		report.setIncludeFastId(true);
		report.setIncludePcBits(true);
		report.setIncludeAntennaPortNumber(true);
		report.setMode(ReportMode.BatchAfterStop);
		final int portPass = Integer.valueOf(ApplicationConfig.get("port.pass", new String[0]));
		final int portRevolve = Integer.valueOf(ApplicationConfig.get("port.revolve", new String[0]));
		
		final GpiConfigGroup gpis = settings.getGpis();
		final GpiConfig gpiConfig = gpis.get(portPass);
		gpiConfig.setDebounceInMs(30L);
		gpis.get(portRevolve).setDebounceInMs(30L);
		final Short gpoPort = Short.valueOf(ApplicationConfig.get("gpo.port", new String[0]));
		final Long pulseDuration = Long.valueOf(ApplicationConfig.get("pulse.duration", new String[0]));
		final GpoConfig gpoConfig = settings.getGpos().get(gpoPort);
		gpoConfig.setMode(GpoMode.Pulsed);
		gpoConfig.setGpoPulseDurationMsec(pulseDuration);
		reader.applySettings(settings);
		
		reader.setGpiChangeListener(new GpiChangeListener() {
			
			@Override
			public void onGpiChanged(ImpinjReader arg0, GpiEvent arg1) {

				if(arg1.isState()){
					new Rx().start();
				}
			}
		});
		
		
		reader.setReaderStartListener(new ReaderStartListener() {
			
			@Override
			public void onReaderStart(ImpinjReader arg0, ReaderStartEvent arg1) {
				tags = null;
			}
		});
		
		reader.setReaderStopListener(new ReaderStopListener() {
			
			@Override
			public void onReaderStop(ImpinjReader arg0, ReaderStopEvent arg1) {
				Pass pass = PassFactory.createPass(tags, OIC.isLeading);
				try {
					tagReader.onPass(pass);
				} catch (OctaneSdkException e) {
					e.printStackTrace();
				}
			}
		});
		
		reader.setTagReportListener(new TagReportListener() {
			
			@Override
			public void onTagReported(ImpinjReader arg0, TagReport arg1) {
				tags = arg1.getTags();
			}
		});
	}

	public void startRead(final String receiveQueueName) throws OctaneSdkException {
		final ImpinjReader reader = ReaderManager.getReader();
		final Settings settings = reader.queryDefaultSettings();
		final Short readAntPort = Short.valueOf(ApplicationConfig.get("read.ant", new String[0]));
		final Short readPower = Short.valueOf(ApplicationConfig.get("read.power", new String[0]));
		settings.setSearchMode(SearchMode.TagFocus);
		settings.setSession(1);
		final AntennaConfigGroup ac = settings.getAntennas();
		ac.disableAll();
		final AntennaConfig antenna = ac.getAntenna((Number) readAntPort);
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm((double) readPower);
		final ReportConfig report = settings.getReport();
		report.setIncludeFastId(true);
		report.setIncludePcBits(true);
		report.setMode(ReportMode.Individual);
		reader.applySettings(settings);

		reader.setTagReportListener(new TagReportListener() {

			@Override
			public void onTagReported(ImpinjReader arg0, TagReport tagReport) {
				List<Tag> tagsRead = tagReport.getTags();
				if ("reenter".equals(receiveQueueName)) {
					List<String> epcTid = new ArrayList<>();
					for (Tag tagRead : tagsRead) {
						String value = 
								tagRead.getEpc().toHexString() + ","
						            + tagRead.getTid().toHexString();
						epcTid.add(value);
					}
					tagReader.onReenter(epcTid);
				}

				if ("add".equals(receiveQueueName)) {
					if(!tagsRead.isEmpty()) {
						tagReader.sendAdd(tagsRead);
					}
				}
			}
		});

		reader.start();
	}

}
