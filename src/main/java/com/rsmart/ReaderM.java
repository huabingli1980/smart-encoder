package com.rsmart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.impinj.octane.AntennaConfig;
import com.impinj.octane.AntennaConfigGroup;
import com.impinj.octane.GpiChangeListener;
import com.impinj.octane.GpiEvent;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.JReader;
import com.impinj.octane.KillResultStatus;
import com.impinj.octane.LockResultStatus;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.ReportConfig;
import com.impinj.octane.ReportMode;
import com.impinj.octane.SearchMode;
import com.impinj.octane.SequenceState;
import com.impinj.octane.SequenceTriggerType;
import com.impinj.octane.Settings;
import com.impinj.octane.Tag;
import com.impinj.octane.TagData;
import com.impinj.octane.TagKillOpResult;
import com.impinj.octane.TagLockOp;
import com.impinj.octane.TagLockOpResult;
import com.impinj.octane.TagLockState;
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
import com.impinj.octane.WriteResultStatus;
import com.rsmart.utils.DebugUtils;
import com.rsmart.utils.IDGenerator;
import com.sqlite.utils.ReaderManager;

public class ReaderM implements TagReportListener, TagOpCompleteListener, GpiChangeListener {

	private static short ID_WRITEOP = 1232;
	private static short ID_LOCKOP = 1233;
	private static short ID_WRITEACCESSPWDOP = 1234;
	public static String currentEpc;

	Set<String> tids = new HashSet<String>();

	static Map<String, String> data = new HashMap<String, String>();
	static int count;
	private JReader reader;

	public static void main(String[] args) throws OctaneSdkException {

		data.put("0", "00B07A135403A98805192200");
		/*data.put("1", "00B07A135403A98805192201");
		data.put("2", "00B07A135403A98805192202");
		for (int i = 3; i < 30; i++) {
			data.put("" + i, "00B07A135403A988051922A" + (i - 2));
		}*/

		DebugUtils.disableLog();
		ReaderM rh = new ReaderM();
		rh.simulate();
	}

	private void simulate() {
		for (int i = 0; i < 1; i++) {
			prepareEncode("barcode" + i);
			reader.startRospect();
		}
	}

	public ReaderM() throws OctaneSdkException {
		init();
	}

	private void init() throws OctaneSdkException {
		reader = (JReader) ReaderManager.getReader();
		short workingAntPort = 1;

		Settings readSettings = reader.queryDefaultSettings();
		readSettings.setSearchMode(SearchMode.DualTarget);
		readSettings.setTagPopulationEstimate(1);
		readSettings.setSession(1);

		/*
		 * GpiConfigGroup gpis = readSettings.getGpis(); gpis.disableAll();
		 * gpis.get(0).setIsEnabled(true);
		 */

		/*
		 * GpoConfigGroup gpos = readSettings.getGpos(); GpoConfig gpo =
		 * gpos.get(3); gpo.setMode(GpoMode.Normal);
		 * gpo.setGpoPulseDurationMsec(20);
		 */

		// readSettings.getAutoStart().setMode(AutoStartMode.Immediate);

		final AntennaConfigGroup ac = readSettings.getAntennas();
		ac.disableAll();
		AntennaConfig antenna = ac.getAntenna(workingAntPort);
		antenna.setEnabled(true);
		antenna.setIsMaxTxPower(false);
		antenna.setTxPowerinDbm(20);
		antenna.setIsMaxRxSensitivity(false);
		antenna.setRxSensitivityinDbm(-35);

		final ReportConfig report = readSettings.getReport();
		report.setMode(ReportMode.Individual);
		report.setIncludeFastId(true);

		/*AntennaConfig antenna2 = ac.getAntenna(2);
		antenna2.setEnabled(true);
		antenna2.setIsMaxTxPower(false);
		antenna2.setTxPowerinDbm(20);*/

		reader.setTagReportListener(this);
		reader.setTagOpCompleteListener(this);
		reader.setGpiChangeListener(this);
		
		reader.applySettings(readSettings);
		
	}

	@Override
	public void onTagOpComplete(ImpinjReader arg0, TagOpReport arg1) {
		DebugUtils.record("On Tag Op Complete");
		List<TagOpResult> results = arg1.getResults();
		for (TagOpResult tagOpResult : results) {
			if (tagOpResult instanceof TagWriteOpResult) {
				TagWriteOpResult res = (TagWriteOpResult) tagOpResult;
				WriteResultStatus rl = res.getResult();
				if (rl == WriteResultStatus.Success) {
					Tag tag = res.getTag();
					System.out.println("Write Success - " + res.getSequenceId() + " - " + res.getNumWordsWritten()
							+ " tid: " + tag.getTid().toHexString() + ", epc: " + tag.getEpc().toHexString());
				} else {
					System.out.println("Write Failed" + rl + " - " + res.getOpId());
				}

			} else if (tagOpResult instanceof TagLockOpResult) {
				TagLockOpResult res = (TagLockOpResult) tagOpResult;
				LockResultStatus result = res.getResult();
				if (result == LockResultStatus.Success) {
					System.out.println("Lock Success");
				} else {
					System.out.println("Lock Failed" + result);
				}

			} else if (tagOpResult instanceof TagKillOpResult) {
				TagKillOpResult res = (TagKillOpResult) tagOpResult;
				KillResultStatus result = res.getResult();
				if (result == KillResultStatus.Success) {
					System.out.println("Kill Success");
				} else {
					System.out.println("Kill Failed" + result);
				}

			}
		}

		System.out.println();

	}

	@Override
	public void onTagReported(ImpinjReader reader, TagReport tagReport) {
		System.out.println("On tag report ...");
		List<Tag> tags = tagReport.getTags();
		Tag tag = tags.get(0);
		String tid = tag.getTid().toHexString();
		String epc = tag.getEpc().toHexString();

		currentEpc = tid;

		prepareEncode(tid);
		
		System.out.println("your epc - " + epc + " tid - " + tid);

	}

	public void writeEpcAndLock(int id, String epc, String target) {

		TagOpSequence seq;
		try {
			seq = getSeq(id, epc, target);
			reader.addOpSequence(seq);
			System.out.println("added");
		} catch (OctaneSdkException e) {
			e.printStackTrace();
		}
	}

	private TagOpSequence getSeq(int seqId, final String newEpc, String tid) throws OctaneSdkException {
		TagOpSequence seq = new TagOpSequence();
		seq.setExecutionCount((short) 1);
		seq.setState(SequenceState.Active);
		seq.setId(seqId);
		/*seq.setBlockWriteEnabled(true);
		seq.setBlockWriteWordCount((short) 2);*/
		seq.setSequenceStopTrigger(SequenceTriggerType.ExecutionCount);
		// seq.setAntennaId(antennaId);

		// Use TID as target to search
		
		/* TargetTag targetTag = new TargetTag();
		  targetTag.setMemoryBank(MemoryBank.Tid);
		  targetTag.setData(tid);*/
		 
		seq.setTargetTag(null);

		List<TagOp> ops = seq.getOps();

		/*
		 * TagData passWord = TagData.fromHexString("00000001"); TagWriteOp
		 * setKillPwdOp = new TagWriteOp(); setKillPwdOp.Id =
		 * ++ID_WRITEACCESSPWDOP; setKillPwdOp.setAccessPassword(null);
		 * setKillPwdOp.setMemoryBank(MemoryBank.Reserved);
		 * setKillPwdOp.setWordPointer(WordPointers.KillPassword);
		 * setKillPwdOp.setData(passWord); ops.add(setKillPwdOp);
		 * 
		 * 
		 * TagKillOp tagKillOp = new TagKillOp(); tagKillOp.Id = ++ID_LOCKOP;
		 * tagKillOp.setKillPassword(passWord); ops.add(tagKillOp);
		 */

		/*
		 * TagData unpassWord = TagData.fromHexString("00000009"); TagLockOp
		 * unlockEpcOp = new TagLockOp(); unlockEpcOp.Id = ++ID_LOCKOP;
		 * unlockEpcOp.setAccessPassword(unpassWord);
		 * //unlockEpcOp.setAccessPasswordLockType(TagLockState.Unlock);
		 * unlockEpcOp.setEpcLockType(TagLockState.Unlock);
		 * ops.add(unlockEpcOp);
		 */

		// 1. Write EPC
		TagWriteOp writeEpcOp = getEpcWriteOp(newEpc, null);
		ops.add(writeEpcOp);

		// Access password(initial one is "00000000")

		// 2. Write Access password
		/*
		 * TagWriteOp accessPwdWrite = getAccessPasswordWriteOp(passWord);
		 * ops.add(accessPwdWrite);
		 */

		// 3. Lock access password and permanently lock EPC
		/*
		 * TagLockOp lockEpcOp = new TagLockOp(); lockEpcOp.Id = ++ID_LOCKOP;
		 * lockEpcOp.setAccessPassword(null);
		 * lockEpcOp.setEpcLockType(TagLockState.Permalock); ops.add(lockEpcOp);
		 */
		// 3. Lock access password and permanently lock EPC
		/*TagLockOp lockEpcOp = new TagLockOp();
		lockEpcOp.Id = ++ID_LOCKOP;
		lockEpcOp.setAccessPassword(null);
		lockEpcOp.setEpcLockType(TagLockState.Permalock);
		ops.add(lockEpcOp);*/

		return seq;
	}

	private TagWriteOp getAccessPasswordWriteOp(TagData passWord) {
		TagWriteOp accessPwdWrite = new TagWriteOp();
		accessPwdWrite.Id = ++ID_WRITEACCESSPWDOP;
		accessPwdWrite.setAccessPassword(null);
		accessPwdWrite.setMemoryBank(MemoryBank.Reserved);
		accessPwdWrite.setWordPointer(WordPointers.AccessPassword);
		accessPwdWrite.setData(passWord);
		return accessPwdWrite;
	}

	private TagWriteOp getEpcWriteOp(final String newEpc, TagData pwd) throws OctaneSdkException {
		TagWriteOp epcWrite = new TagWriteOp();
		epcWrite.Id = ++ID_WRITEOP;
		epcWrite.setMemoryBank(MemoryBank.Epc);
		epcWrite.setWordPointer(WordPointers.Epc);
		epcWrite.setData(TagData.fromHexString(newEpc));
		epcWrite.setAccessPassword(pwd);
		return epcWrite;
	}

	@Override
	public void onGpiChanged(ImpinjReader reader, GpiEvent gpiEvent) {

		if (!gpiEvent.isState()) {
			// signalToReadBarcode();
			prepareEncode("");
		}
	}

	private void signalToReadBarcode() {
		try {
			Thread.sleep(50);
			reader.setGpo(3, true);
			Thread.sleep(50);
			reader.setGpo(3, false);
		} catch (OctaneSdkException e) {
			System.out.println("gpo timeout!!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void listenEventFromSerialCom() {

		String port = "com3";
		final SerialPort comPort = SerialPort.getCommPort(port);
		comPort.setBaudRate(115200);
		comPort.setParity(1);
		comPort.setNumDataBits(8);
		comPort.setNumStopBits(1);

		if (!comPort.openPort()) {
			throw new RuntimeException("Failed to open serial port - " + port);
		}

		comPort.addDataListener(new SerialPortDataListener() {

			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
			}

			public void serialEvent(final SerialPortEvent event) {

				if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
					return;
				}

				final byte[] newData = new byte[comPort.bytesAvailable()];
				final int byteCount = comPort.readBytes(newData, (long) newData.length);
				final String barcode = new String(newData);

				// System.out.println("barcode ------- " + dataStr);
				// System.out.println(dataStr);
				/*
				 * if (byteCount <= 6) { System.out.println("Failed"); } else {
				 * try {
				 * 
				 * writeIt(); } catch (Exception e) { // TODO Auto-generated
				 * catch block e.printStackTrace(); } }
				 */

				prepareEncode(barcode);
			}
		});
	}

	private void prepareEncode(String barcode) {
		String epc = "00B07A135403A98805192200"; // getEpc(barcode);
		String target = this.currentEpc;
		int id = IDGenerator.genSeqId();

		System.out.println("about to encode (" + id + ", " + epc + "," + target + ")");
		writeEpcAndLock(id, epc, null);
	}

	private String getEpc(String barcode) {
		/*
		 * String valueOf = String.valueOf(System.currentTimeMillis() + 54321);
		 * String epcToWrite = "00B07A135403A988" +
		 * valueOf.substring(valueOf.length() - 8);
		 */

		return data.get("" + count++);
	}
}
