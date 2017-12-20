package com.rsmart;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.AutoStartConfig;
import com.impinj.octane.AutoStartMode;
import com.impinj.octane.BitPointers;
import com.impinj.octane.GpiConfigGroup;
import com.impinj.octane.GpoConfig;
import com.impinj.octane.GpoMode;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.PcBits;
import com.impinj.octane.ReaderStopEvent;
import com.impinj.octane.ReaderStopListener;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.SequenceState;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagData;
import com.impinj.octane.TagOp;
import com.impinj.octane.TagOpCompleteListener;
import com.impinj.octane.TagOpReport;
import com.impinj.octane.TagOpResult;
import com.impinj.octane.TagOpSequence;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.TagWriteOp;
import com.impinj.octane.TagWriteOpResult;
import com.impinj.octane.TargetTag;
import com.impinj.octane.WordPointers;
import com.sqlite.utils.ReaderManager;

import boot.ApplicationConfig;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SimpleReader implements IReader {
	private static final int WRITE_DURATION = 220;
	ImpinjReader reader;
	Settings settings;
	
	static short EPC_OP_ID = 123;
    static short PC_BITS_OP_ID = 321;
    static int opSpecID = 1;
    static int outstanding = 0;
    static Random r = new Random();

	public SimpleReader() {
		this.reader = ReaderManager.getReader();
		this.settings = this.reader.queryDefaultSettings();
	}

	@Override
	public Promise writeEpc(final String newEpc) {
		final Deferred deferred = new DeferredObject();
		final Promise promise = deferred.promise();
		
		readEpc().done(new DoneCallback<List<Tag>>() {

			@Override
			public void onDone(List<Tag> tags) {
				int size = tags.size();
				if(size != 1){
					if(!deferred.isRejected()){
						deferred.reject("tag target count does not equals one!");
					}
					
					return;
				}
				
				try {
					Settings settings = reader.queryDefaultSettings();
					reader.setReaderStopListener(null);
					
					final AntennaConfigGroup ac = settings.getAntennas();
					ac.disableAll();

					final Short antennaPort = Short.valueOf(ApplicationConfig.get("detect.ant", "1"));
					AntennaConfig antenna;
					try {
						antenna = ac.getAntenna(antennaPort);
					} catch (OctaneSdkException e) {
						throw new RuntimeException("Reader Exception", e);
					}
					
					antenna.setEnabled(true);
					antenna.setIsMaxTxPower(false);
					antenna.setTxPowerinDbm((double) Short.valueOf(ApplicationConfig.get("detect.power", "60")));
					antenna.setIsMaxRxSensitivity(false);
					antenna.setRxSensitivityinDbm((double) Short.valueOf(ApplicationConfig.get("receive.sensitivity", "-35")));
					
					reader.applySettings(settings);
				} catch (OctaneSdkException e1) {
					throw new RuntimeException("Someting went wrong when setting up reading!", e1);
				}
				
				Tag tag = tags.get(0);
				short currentPC = tag.getPcBits();
				String currentEpc = tag.getEpc().toHexString();
			
				if ((currentEpc.length() % 4 != 0) || (newEpc.length() % 4 != 0)) {
					if(!deferred.isRejected()){
						deferred.reject("EPCs must be a multiple of 16- bits");
					}
		            return;
		        }

		        System.out.println("Programming Tag from EPC " + currentEpc + " to " + newEpc);

		        TagOpSequence seq = new TagOpSequence();
		        seq.setOps(new ArrayList<TagOp>());
		        seq.setExecutionCount((short) 1);
		        seq.setState(SequenceState.Active);
		        seq.setId(opSpecID++);

		        seq.setTargetTag(new TargetTag());
		        seq.getTargetTag().setBitPointer(BitPointers.Epc);
		        seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
		        seq.getTargetTag().setData(currentEpc);

		        TagWriteOp epcWrite = new TagWriteOp();
		        epcWrite.Id = EPC_OP_ID;
		        epcWrite.setMemoryBank(MemoryBank.Epc);
		        epcWrite.setWordPointer(WordPointers.Epc);
		        try {
					epcWrite.setData(TagData.fromHexString(newEpc));
				} catch (OctaneSdkException e) {
					throw new RuntimeException("Invalid format of new EPC!" + newEpc, e);
				}

		        seq.getOps().add(epcWrite);

		        // have to program the PC bits if these are not the same
		        if (currentEpc.length() != newEpc.length()) {
		            String currentPCString = PcBits.toHexString(currentPC);

		            short newPC = PcBits.AdjustPcBits(currentPC, (short) (newEpc.length() / 4));
		            String newPCString = PcBits.toHexString(newPC);

		            System.out.println("PC bits to establish new length: " + newPCString + " " + currentPCString);

		            TagWriteOp pcWrite = new TagWriteOp();
		            pcWrite.Id = PC_BITS_OP_ID;
		            pcWrite.setMemoryBank(MemoryBank.Epc);
		            pcWrite.setWordPointer(WordPointers.PcBits);

		            try {
						pcWrite.setData(TagData.fromHexString(newPCString));
					} catch (OctaneSdkException e) {
						throw new RuntimeException("Invalid format of new EPC!" + newPCString, e);
					}
		            
		            seq.getOps().add(pcWrite);
		        }

		        try {
		        	reader.setTagOpCompleteListener(new TagOpCompleteListener() {
						
						@Override
						public void onTagOpComplete(ImpinjReader arg0, TagOpReport results) {
							 	System.out.println("TagOpComplete: ");
							 	
						        for (TagOpResult t : results.getResults()) {
						            String hexString = t.getTag().getEpc().toHexString();
									System.out.print("EPC: " + hexString);
									
						            if (t instanceof TagWriteOpResult) {
						                TagWriteOpResult tr = (TagWriteOpResult) t;
						                	
						                if (tr.getOpId() == EPC_OP_ID) {
						                    System.out.print("  Write to EPC Complete: ");
						                } else if (tr.getOpId() == PC_BITS_OP_ID) {
						                    System.out.print("  Write to PC Complete: ");
						                }
						                
						                deferred.resolve(hexString);
						                System.out.println(" result: " + tr.getResult().toString() + " words_written: " + tr.getNumWordsWritten());
						                outstanding--;
						            }
						       }
						}
					});
		        	
					reader.addOpSequence(seq);
					
					if(reader.queryStatus().getIsSingulating()){
						reader.stop();
					}
					
					reader.start();
					
					Thread.sleep(WRITE_DURATION);
				} catch (OctaneSdkException e) {
					throw new RuntimeException("Someting went wrong when starting the reader!", e);
				} catch (InterruptedException e) {
					throw new RuntimeException("Sleep thread is interrupted!", e);
				}
			}
			
		}).fail(new FailCallback<String>() {

			@Override
			public void onFail(String errorCode) {
				deferred.reject(errorCode);
			}
		});
		 
		return promise;
	}

	@Override
	public Promise readMargin(final String epc) {
		final Deferred deferred = new DeferredObject();
		final Promise promise = deferred.promise();
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		deferred.resolve((Object) 0);
		return promise;
	}

	public Promise read() {
		return null;
	}

	
	@Override
	public Promise readEpc() throws RuntimeException {
		final Deferred deferred = new DeferredObject();
		final Promise promise = deferred.promise();

		this.settings.setSearchMode(SearchMode.SingleTarget);
		this.settings.setTagPopulationEstimate(1);
		this.settings.setSession(1);
		final AntennaConfigGroup ac = this.settings.getAntennas();
		ac.disableAll();

		final Short antennaPort = Short.valueOf(ApplicationConfig.get("detect.ant", "1"));
		AntennaConfig antenna;
		try {
			antenna = ac.getAntenna(antennaPort);
		} catch (OctaneSdkException e) {
			throw new RuntimeException("Reader Exception", e);
		}
		
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm((double) Short.valueOf(ApplicationConfig.get("detect.power", "12")));
		antenna.setIsMaxRxSensitivity(false);
		antenna.setRxSensitivityinDbm((double) Short.valueOf(ApplicationConfig.get("receive.sensitivity", "-35")));
		
		final ReportConfig report = this.settings.getReport();
		report.setIncludeFastId(true);
		report.setIncludePcBits(true);
		report.setIncludeAntennaPortNumber(true);
		report.setMode(ReportMode.BatchAfterStop);
		
		final List<Tag> tags = new ArrayList<Tag>();
		this.reader.setReaderStopListener(new ReaderStopListener() {
			
			@Override
			public void onReaderStop(ImpinjReader arg0, ReaderStopEvent arg1) {
				
				System.out.println("stopping ....");
				if (tags.size() == 0) {
					
					if(!deferred.isResolved()){
						deferred.reject("EXCEPTION_NO_TAG");
					}
					
				} else if (tags.size() > 1){
					
					if(!deferred.isResolved()){
						deferred.reject("EXCEPTION_MULTIPLE_TAGS");
					}
					
				} else {
					
					if(!deferred.isResolved()){
						deferred.resolve(tags);
					}
				}
			}
		});
		
		this.reader.setTagReportListener(new TagReportListener() {
			
			@Override
			public void onTagReported(ImpinjReader arg0, TagReport tagReport) {
				tags.clear();
				tags.addAll(tagReport.getTags());
			}
			
		});
		
		final AutoStartConfig autoStartConfig = new AutoStartConfig();
		autoStartConfig.setMode(AutoStartMode.Immediate);
		this.settings.setAutoStart(autoStartConfig);
		
		try {
			this.reader.applySettings(this.settings);
		} catch (OctaneSdkException e2) {
			throw new RuntimeException("Reader Exception", e2);
		}
		
		return promise;
	}

	@Override
	public Promise readBlock() {
		return null;
	}

	@Override
	public Promise writeUserMemory(final String content) {
		return null;
	}

	@Override
	public Promise writeBlock(final String content) {
		return null;
	}

	@Override
	public Promise kill(final String password) {
		return null;
	}

	@Override
	public Promise lockMemory(final int bitPointer, final String password) {
		return null;
	}

	@Override
	public void gpo(final int port, final boolean level) throws RuntimeException {
		final Short gpoPort = Short.valueOf(ApplicationConfig.get("gpo.port", "4"));
		final Long pulseDuration = Long.valueOf(ApplicationConfig.get("pulse.duration", "500"));
		final GpoConfig gpoConfig = this.settings.getGpos().get((int) gpoPort);
		gpoConfig.setMode(GpoMode.Pulsed);
		gpoConfig.setGpoPulseDurationMsec((long) pulseDuration);
		try {
			this.reader.applySettings(this.settings);
			this.reader.setGpo(port, level);
		} catch (OctaneSdkException e) {
			throw new RuntimeException("Reader Exception", (Throwable) e);
		}
	}

	@Override
	public void addGpiListener(final int port, final GpiListener gpiListener) throws RuntimeException {
		final int portPass = Integer.valueOf(ApplicationConfig.get("port.pass", "1"));
		final int portRevolve = Integer.valueOf(ApplicationConfig.get("port.revolve", new String[0]));
		final GpiConfigGroup gpis = this.settings.getGpis();
		gpis.get(portPass).setDebounceInMs(30L);
		gpis.get(portRevolve).setDebounceInMs(30L);
		try {
			this.reader.applySettings(this.settings);
		} catch (OctaneSdkException e) {
			throw new RuntimeException("Reader Exception", (Throwable) e);
		}
	}

	@Override
	public void readPeriodically(final long interval, final TagReportListener trl) {
	}
}
