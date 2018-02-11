package com.rsmart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.BitPointers;
import com.impinj.octane.GpiConfigGroup;
import com.impinj.octane.GpoConfig;
import com.impinj.octane.GpoMode;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.LockResultStatus;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.PcBits;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.SequenceState;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagData;
import com.impinj.octane.TagLockOp;
import com.impinj.octane.TagLockOpResult;
import com.impinj.octane.TagLockState;
import com.impinj.octane.TagOp;
import com.impinj.octane.TagOpCompleteListener;
import com.impinj.octane.TagOpReport;
import com.impinj.octane.TagOpResult;
import com.impinj.octane.TagOpSequence;
import com.impinj.octane.TagReadOpResult;
import com.impinj.octane.TagReport;
import com.impinj.octane.TagReportListener;
import com.impinj.octane.TagWriteOp;
import com.impinj.octane.TagWriteOpResult;
import com.impinj.octane.TargetTag;
import com.impinj.octane.WordPointers;
import com.impinj.octane.WriteResultStatus;
import com.sqlite.utils.ReaderManager;

import boot.ApplicationConfig;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SimpleReader2 implements IReader {
	
	private final class OpCompleteListener implements TagOpCompleteListener {
		private final Deferred deferred;
		private int c = 3;
		private OpCompleteListener(Deferred deferred) {
			this.deferred = deferred;
		}

		@Override
		public void onTagOpComplete(ImpinjReader arg0, TagOpReport results) {
			List<TagOpResult> resl = results.getResults();
			Map<String, String> mp = new HashMap<String, String>();
		
			for (TagOpResult tagOpResult : resl) {
				if(tagOpResult instanceof TagWriteOpResult){
					TagWriteOpResult res = (TagWriteOpResult) tagOpResult;
					WriteResultStatus rl = res.getResult();
					String resls = rl.name();
					if(rl == WriteResultStatus.Success){
						c--;
					} else{
						if(res.getOpId() == EPC_OP_ID){
							mp.put("writeEpc", resls);
						} else if (res.getOpId() == ACCESSPWD_OP_ID){
							mp.put("writeAccessPwd", resls);
						} else {
							
						}
					}
					
				} else if(tagOpResult instanceof TagLockOpResult){
					TagLockOpResult res = (TagLockOpResult) tagOpResult;
					LockResultStatus result = res.getResult();
					if(result == LockResultStatus.Success){
						c--;
					} else {
						if(res.getOpId() == LOCK_OP_ID){
							mp.put("writeEpc", result.name());
						} else {
							
						}
					}
					
				} else if(tagOpResult instanceof TagReadOpResult){
					/*deferred.reject("Unknown result");
					return;*/
					System.out.println("Tag reading .....");
				} 
			}
			
			if( c>0 ){
				/*if(!deferred.isPending()){
					System.out.println("Is not pending .....");
					return;
				}*/
				
				deferred.reject(Result.newOpResult(mp));
				
			} else {
				deferred.resolve("ok");
				
			}
		}
	}

	private static final int LOCK_OP_ID = 129;
	private static final short ACCESSPWD_OP_ID = 125;
	public static long start;
	
	ImpinjReader reader;
	Settings readSettings;
	Settings writeSettings;
	
	static short EPC_OP_ID = 123;
    static short PC_BITS_OP_ID = 321;
    static int opSpecID = 1;
    static int outstanding = 0;

	public SimpleReader2() throws Exception {
		init();
	}

	public void init() throws OctaneSdkException {
		reader = ReaderManager.getReader();
		short workingAntPort = 1;
		
		readSettings = reader.queryDefaultSettings();
		readSettings.setSearchMode(SearchMode.DualTarget);
		readSettings.setTagPopulationEstimate(1);
		readSettings.setSession(1);
		
		final AntennaConfigGroup ac = readSettings.getAntennas();
		ac.disableAll();
		AntennaConfig antenna = ac.getAntenna(workingAntPort);
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm(13);
		antenna.setIsMaxRxSensitivity(false);
		antenna.setRxSensitivityinDbm(-30);
		
		final ReportConfig report = readSettings.getReport();
		report.setMode(ReportMode.Individual);
		
		
		writeSettings = reader.queryDefaultSettings();
		AntennaConfigGroup writeA = writeSettings.getAntennas();
		writeA.disableAll();
		AntennaConfig wrantenna = writeA.getAntenna(workingAntPort);
		wrantenna.setEnabled(true);
		wrantenna.setIsMaxRxSensitivity(false);
		wrantenna.setRxSensitivityinDbm(-30);
		
		reader.applySettings(readSettings);
		reader.start();
	}

	@Override
	public Promise writeEpc(final String newEpc) throws Exception {
		final Deferred deferred = new DeferredObject();
		//deferred.waitSafely(400L);
		
		final Promise promise = deferred.promise();
		
		System.out.println();
		System.out.println("#" + SmartEncoder.count + " - About to encode " + newEpc);
		
		/*if(newEpc.length() % 4 != 0){
			deferred.reject(Result.newReasonOnlyResult("Invalid new EPC" + newEpc));
		}*/
		
		readSingleEpc().done(new DoneCallback<Tag>() {

			@Override
			public void onDone(Tag tag) {
				
				//short currentPC = tag.getPcBits();
				String currentEpc = tag.getEpc().toHexString();
				
				//System.out.println("Read EPC(" + currentEpc + ") within " + (System.currentTimeMillis() - readStart) + " ms");
				
				try {
					//writeIt(newEpc, deferred, currentPC, currentEpc);
					reader.setTagOpCompleteListener(new OpCompleteListener(deferred));
					TagOpSequence seq = getSeq(newEpc, currentEpc);
			        reader.addOpSequence(seq);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}).fail(new FailCallback<Result>() {

			@Override
			public void onFail(Result errorCode) {
				deferred.reject(errorCode);
			}
		});
		 
		return promise;
	}
	
	private void writeIt(final String newEpc, final Deferred deferred, short currentPC, String currentEpc) throws Exception {
		
		//reader.applySettings(writeSettings);
		
		/*if ((currentEpc.length() % 4 != 0) || (newEpc.length() % 4 != 0)) {
			if(!deferred.isRejected()){
				deferred.reject("EPCs must be a multiple of 16- bits");
			}
            return;
        }*/

		
        TagOpSequence seq = getSeq(newEpc, currentEpc);
        reader.addOpSequence(seq);

        reader.setTagOpCompleteListener(new OpCompleteListener(deferred));
        //checkOrUpdatePc(newEpc, currentPC, currentEpc, seq);

    	
        /*reader.stop();
		reader.start();*/
	}
	
	

	private TagOpSequence getSeq(final String newEpc, String currentEpc) throws OctaneSdkException {
		TagOpSequence seq = new TagOpSequence();
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
		epcWrite.setData(TagData.fromHexString(newEpc));
		
		TagWriteOp accessPwdWrite = new TagWriteOp();
		accessPwdWrite.Id = ACCESSPWD_OP_ID;
		accessPwdWrite.setMemoryBank(MemoryBank.Reserved);
		accessPwdWrite.setWordPointer(WordPointers.AccessPassword);
		TagData passWord = TagData.fromByteArray("hell".getBytes());
		accessPwdWrite.setData(passWord);
		
		TagLockOp tagLockOp = new TagLockOp();
		tagLockOp.Id = LOCK_OP_ID;
		tagLockOp.setAccessPassword(passWord);
		tagLockOp.setAccessPasswordLockType(TagLockState.Lock);
		
        List<TagOp> ops = seq.getOps();
		ops.add(epcWrite);
		ops.add(accessPwdWrite);
		ops.add(tagLockOp);
		
		return seq;
	}

	private void checkOrUpdatePc(final String newEpc, short currentPC, String currentEpc, TagOpSequence seq) {
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
	public Promise readSingleEpc() throws Exception {
		
		final Deferred deferred = new DeferredObject();
		
		final Promise promise = deferred.promise();
		reader.setTagReportListener(new TagReportListener() {
			
			@Override
			public void onTagReported(ImpinjReader arg0, TagReport tagReport) {
				start = System.currentTimeMillis();
				System.out.println("Read taken - " + (System.currentTimeMillis() - start) + " ms!");
				List<Tag> tags = tagReport.getTags();
				int size = tags.size();
				if (size != 1) {
					
					if(!deferred.isResolved()){
						deferred.reject(Result.newReasonOnlyResult("EXCEPTION_TAG" + size));
					}
					
					return;
				} 
				
				if(!deferred.isResolved()){
					deferred.resolve(tags.get(0));
				}
			}
			
		});
		
		//reader.applySettings(readSettings);
		//reader.stop();
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
		final GpoConfig gpoConfig = this.readSettings.getGpos().get((int) gpoPort);
		gpoConfig.setMode(GpoMode.Pulsed);
		gpoConfig.setGpoPulseDurationMsec((long) pulseDuration);
		try {
			this.reader.applySettings(this.readSettings);
			this.reader.setGpo(port, level);
		} catch (OctaneSdkException e) {
			throw new RuntimeException("Reader Exception", (Throwable) e);
		}
	}

	@Override
	public void addGpiListener(final int port, final GpiListener gpiListener) throws RuntimeException {
		final int portPass = Integer.valueOf(ApplicationConfig.get("port.pass", "1"));
		final int portRevolve = Integer.valueOf(ApplicationConfig.get("port.revolve", new String[0]));
		final GpiConfigGroup gpis = this.readSettings.getGpis();
		gpis.get(portPass).setDebounceInMs(30L);
		gpis.get(portRevolve).setDebounceInMs(30L);
		try {
			this.reader.applySettings(this.readSettings);
		} catch (OctaneSdkException e) {
			throw new RuntimeException("Reader Exception", (Throwable) e);
		}
	}

	@Override
	public void readPeriodically(final long interval, final TagReportListener trl) {
	}
}
