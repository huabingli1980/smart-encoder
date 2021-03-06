package com.example.sdksamples;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.LoggerFactory;

import com.impinj.octane.AutoStartMode;
import com.impinj.octane.AutoStopMode;
import com.impinj.octane.BitPointers;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.PcBits;
import com.impinj.octane.ReaderMode;
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class WriteEpc implements TagReportListener, TagOpCompleteListener {

    static short EPC_OP_ID = 123;
    static short PC_BITS_OP_ID = 321;
    static int opSpecID = 1;
    static int outstanding = 0;
    static Random r = new Random();
	private static long start;
    private ImpinjReader reader;

    /*static String getRandomEpc() {
        String epc = "";

        // get the length of the EPC from 1 to 8 words
        int numwords = r.nextInt((6 - 1) + 1) + 1;
        // int numwords = 1;

        for (int i = 0; i < numwords; i++) {
            Short s = (short) r.nextInt(Short.MAX_VALUE + 1);
            epc += String.format("%04X", s);
        }
        return epc;
    }*/

    public static void main(String[] args) {
    	Logger minaLogger = (Logger) LoggerFactory.getLogger("org.apache.mina");
		if (minaLogger != null) {
			minaLogger.setLevel(Level.WARN);
		}
		
        WriteEpc epcWriter = new WriteEpc();
     
        epcWriter.run();
    }

    void programEpc(String currentEpc, short currentPC, String newEpc)
            throws Exception {
        if ((currentEpc.length() % 4 != 0) || (newEpc.length() % 4 != 0)) {
            throw new Exception("EPCs must be a multiple of 16- bits: "
                    + currentEpc + "  " + newEpc);
        }

        if (outstanding > 0) {
            return;
        }

        System.out.println("Programming Tag ");
        System.out.println("   EPC " + currentEpc + " to " + newEpc);

        TagOpSequence seq = new TagOpSequence();
        seq.setOps(new ArrayList<TagOp>());
        seq.setExecutionCount((short) 1); // delete after one time
        seq.setState(SequenceState.Active);
        seq.setId(opSpecID++);
        //seq.setBlockWriteEnabled(true);
        seq.setTargetTag(new TargetTag());
        seq.getTargetTag().setBitPointer(BitPointers.Epc);
        seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
        seq.getTargetTag().setData(currentEpc);

        TagWriteOp epcWrite = new TagWriteOp();
        epcWrite.Id = EPC_OP_ID;
        epcWrite.setMemoryBank(MemoryBank.Epc);
        epcWrite.setWordPointer(WordPointers.Epc);
        epcWrite.setData(TagData.fromHexString(newEpc));

        // add to the list
        seq.getOps().add(epcWrite);

        // have to program the PC bits if these are not the same
        if (currentEpc.length() != newEpc.length()) {
            // keep other PC bits the same.
            String currentPCString = PcBits.toHexString(currentPC);

            short newPC = PcBits.AdjustPcBits(currentPC,
                    (short) (newEpc.length() / 4));
            String newPCString = PcBits.toHexString(newPC);

            System.out.println("   PC bits to establish new length: "
                    + newPCString + " " + currentPCString);

            TagWriteOp pcWrite = new TagWriteOp();
            pcWrite.Id = PC_BITS_OP_ID;
            pcWrite.setMemoryBank(MemoryBank.Epc);
            pcWrite.setWordPointer(WordPointers.PcBits);

            pcWrite.setData(TagData.fromHexString(newPCString));
            seq.getOps().add(pcWrite);
        }

        outstanding++;
        start = System.currentTimeMillis();
        reader.addOpSequence(seq);
    }

    void run() {

        try {
            String hostname = SampleProperties.hostname;

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            reader = new ImpinjReader();

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            // just use a single antenna here
            settings.getAntennas().disableAll();
            settings.getAntennas().getAntenna((short) 1).setEnabled(true);

            // set session one so we see the tag only once every few seconds
            settings.getReport().setIncludeAntennaPortNumber(true);
            settings.setReaderMode(ReaderMode.AutoSetDenseReader);
            settings.setSearchMode(SearchMode.DualTarget);
            settings.setSession(1);
            // turn these on so we have them always
            settings.getReport().setIncludePcBits(true);

            // Set periodic mode so we reset the tag and it shows up with its
            // new EPC
            //settings.getAutoStart().setMode(AutoStartMode.Immediate);
           /* settings.getAutoStart().setPeriodInMs(2000);
            settings.getAutoStop().setMode(AutoStopMode.Duration);
            settings.getAutoStop().setDurationInMs(1000);*/

            // Apply the new settings
            reader.applySettings(settings);
            
            TagOpSequence seq = new TagOpSequence();
            seq.setExecutionCount((short) 1);
            seq.setState(SequenceState.Active);
            seq.setId(opSpecID++);
            
           /* seq.setTargetTag(new TargetTag());
            //seq.getTargetTag().setBitPointer(BitPointers.);
            seq.getTargetTag().setMemoryBank(MemoryBank.Tid);
            seq.getTargetTag().setData("E28068942000500177E258CA");*/
            
            
            TagWriteOp pcWrite = new TagWriteOp();
            pcWrite.Id = PC_BITS_OP_ID;
            pcWrite.setMemoryBank(MemoryBank.Epc);
            pcWrite.setWordPointer(WordPointers.PcBits);

          /*  try {
				pcWrite.setData(TagData.fromHexString(newPCString));
			} catch (OctaneSdkException e) {
				throw new RuntimeException("Invalid format of new EPC!" + newPCString, e);
			}*/
            
            
            
            // set up listeners to hear stuff back from SDK
            //reader.setTagReportListener(this);
            reader.setTagOpCompleteListener(this);

            // Start the reader
            reader.start();

            System.out.println("Press Enter to exit.");
            Scanner s = new Scanner(System.in);
            s.nextLine();

            System.out.println("Stopping  " + hostname);
            reader.stop();

            System.out.println("Disconnecting from " + hostname);
            reader.disconnect();

            System.out.println("Done");
        } catch (OctaneSdkException ex) {
            System.out.println(ex.getMessage());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace(System.out);
        }
    }

    public void onTagReported(ImpinjReader reader, TagReport report) {
        List<Tag> tags = report.getTags();

        for (Tag t : tags) {
            String newEpc = "00B07A135403A98892124423";//getRandomEpc();

            if (t.isPcBitsPresent()) {
                short pc = t.getPcBits();
                String currentEpc = t.getEpc().toHexString();
                System.out.println("current time - " + System.currentTimeMillis());
                try {
                    programEpc(currentEpc, pc, newEpc);
                } catch (Exception e) {
                    System.out.println("Failed To program EPC: " + e.toString());
                }
            }
        }
    }

    public void onTagOpComplete(ImpinjReader reader, TagOpReport results) {
        System.out.println("TagOpComplete: ");
        for (TagOpResult t : results.getResults()) {
            System.out.print("  EPC: " + t.getTag().getEpc().toHexString());
            if (t instanceof TagWriteOpResult) {
                TagWriteOpResult tr = (TagWriteOpResult) t;

                if (tr.getOpId() == EPC_OP_ID) {
                    System.out.print("  Write to EPC Complete: " + (System.currentTimeMillis() - start));
                    System.out.println("write current time - " + System.currentTimeMillis());
                } else if (tr.getOpId() == PC_BITS_OP_ID) {
                    System.out.print("  Write to PC Complete: ");
                }
                System.out.println(" result: " + tr.getResult().toString()
                        + " words_written: " + tr.getNumWordsWritten());
                outstanding--;
            }
        }
        
        System.out.println("took " + (System.currentTimeMillis() - start) + " ms");
        System.exit(0);
    }
}
