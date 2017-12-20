package com.rsmart;



import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

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
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.Settings;
import com.sqlite.utils.ReaderManager;

import boot.ApplicationConfig;

public class Main {
	

	
	public static void main(String[] args) throws OctaneSdkException {
		
		 Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		    if(minaLogger!=null)
		    {	
		    	minaLogger.setLevel(Level.WARN);
		    }
		    
		final ImpinjReader reader = ReaderManager.getReader();
		final Settings settings = reader.queryDefaultSettings();
		settings.setSearchMode(SearchMode.SingleTarget);
		settings.setTagPopulationEstimate(1);
		settings.setSession(2);
		final AntennaConfigGroup ac = settings.getAntennas();
		ac.disableAll();
		final Short valueOf = Short.valueOf(ApplicationConfig.get("detect.ant", "1"));
		final AntennaConfig antenna = ac.getAntenna((Number) valueOf);
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm(Short.valueOf(ApplicationConfig.get("detect.power", "12")));
		antenna.setIsMaxRxSensitivity(false);
		antenna.setRxSensitivityinDbm(Short.valueOf(ApplicationConfig.get("receive.sensitivity", "-38")));
		final ReportConfig report = settings.getReport();
		report.setIncludeFastId(true);
		report.setIncludePcBits(true);
		report.setIncludeAntennaPortNumber(true);
		report.setMode(ReportMode.BatchAfterStop);
		final int portPass = Integer.valueOf(ApplicationConfig.get("port.pass", "1"));
		final int portRevolve = Integer.valueOf(ApplicationConfig.get("port.revolve", "1"));
		
		final GpiConfigGroup gpis = settings.getGpis();
		final GpiConfig gpiConfig = gpis.get(portPass);
		gpiConfig.setDebounceInMs(30L);
		final Short gpoPort = Short.valueOf(ApplicationConfig.get("gpo.port", "1"));
		final Long pulseDuration = Long.valueOf(ApplicationConfig.get("pulse.duration", "800"));
		final GpoConfig gpoConfig = settings.getGpos().get(gpoPort);
		gpoConfig.setMode(GpoMode.Pulsed);
		gpoConfig.setGpoPulseDurationMsec(pulseDuration);
		reader.applySettings(settings);
		
		reader.setGpiChangeListener(new GpiChangeListener() {
			
			@Override
			public void onGpiChanged(ImpinjReader arg0, GpiEvent arg1) {
				System.out.println("received signal - " + arg1.isState());
				if(arg1.isState()){
					long startL = System.currentTimeMillis();
					new Rx().start();
					
					System.out.println("time taken to complete the write process: " + (System.currentTimeMillis() - startL));
					/*try {
						arg0.setGpo(1, true);
					} catch (OctaneSdkException e) {
						e.printStackTrace();
					}*/
				}
			}
		});
		
	}

}
