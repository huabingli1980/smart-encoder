package com.example.sdksamples;

import java.util.ArrayList;
import java.util.Scanner;

import com.impinj.octane.BitPointers;
import com.impinj.octane.ImpinjReader;
import com.impinj.octane.MemoryBank;
import com.impinj.octane.OctaneSdkException;
import com.impinj.octane.SequenceState;
import com.impinj.octane.Settings;
import com.impinj.octane.TagData;
import com.impinj.octane.TagOp;
import com.impinj.octane.TagOpSequence;
import com.impinj.octane.TagWriteOp;
import com.impinj.octane.TargetTag;

public class WriteUserMemory {
    String defaultWrite = "abcd0123";

    public static void main(String[] args) {
        try {
            String hostname = SampleProperties.hostname;

            if (hostname == null) {
                throw new Exception("Must specify the '"
                        + SampleProperties.hostname + "' property");
            }

            ImpinjReader reader = new ImpinjReader();

            // Connect
            System.out.println("Connecting to " + hostname);
            reader.connect(hostname);

            // Get the default settings
            Settings settings = reader.queryDefaultSettings();

            settings.getReport().setIncludeAntennaPortNumber(true);

            // Apply the new settings
            reader.applySettings(settings);

            // create the reader op sequence
            TagOpSequence seq = new TagOpSequence();
            seq.setOps(new ArrayList<TagOp>());
            seq.setExecutionCount((short) 1); // forever
            seq.setState(SequenceState.Active);
            seq.setId(1);

            TagWriteOp writeOp = new TagWriteOp();
            writeOp.setMemoryBank(MemoryBank.User);
            writeOp.setWordPointer((short) 0);
            writeOp.setData(TagData.fromHexString("abcd"));

            // add to the list
            seq.getOps().add(writeOp);

            // Use target tag to only apply to some EPCs
            String targetEpc = System.getProperty(SampleProperties.targetTag);

           
                // or just send NULL to apply to all tags
                seq.setTargetTag(null);
           
            // add to the reader. The reader supports multiple sequences
            reader.addOpSequence(seq);

            // set up listeners to hear stuff back from SDK. Normally the 
            // application would enable this but we dont want too many 
            // reports in our example
            // reader.setTagReportListener(new TagReportListenerImplementation());

            reader.setTagOpCompleteListener(
                    new TagOpCompleteListenerImplementation());

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
}
