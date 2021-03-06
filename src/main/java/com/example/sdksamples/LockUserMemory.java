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
import com.impinj.octane.TagLockOp;
import com.impinj.octane.TagLockState;
import com.impinj.octane.TagOp;
import com.impinj.octane.TagOpSequence;
import com.impinj.octane.TagWriteOp;
import com.impinj.octane.TargetTag;
import com.impinj.octane.WordPointers;


public class LockUserMemory {

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
            // only execute this one time since it will fail the second time due
            // to password lock
            seq.setExecutionCount((short) 1);
            seq.setState(SequenceState.Active);
            seq.setId(1);

            // write a new access password
            TagWriteOp writeOp = new TagWriteOp();
            writeOp.setMemoryBank(MemoryBank.Reserved);
            // access password starts at an offset into reserved memory
            writeOp.setWordPointer(WordPointers.AccessPassword);
            writeOp.setData(TagData.fromHexString("abcd0123"));
            // no access password at this point

            TagLockOp lockOp = new TagLockOp();
            // lock the access password so it can't be changed
            // since we have a password set, we have to use it
            lockOp.setAccessPassword(TagData.fromHexString("abcd0123"));
            lockOp.setAccessPasswordLockType(TagLockState.Lock);

            // uncomment to lock user memory so it can't be changed
            // lockOp.setUserLockType(TagLockState.Lock);

            // add to the list
            seq.getOps().add(writeOp);
            seq.getOps().add(lockOp);

            // Use target tag to only apply to some EPCs
            String targetEpc = "00B07A135403A9880806C38B";

            if (targetEpc != null) {
                seq.setTargetTag(new TargetTag());
                seq.getTargetTag().setBitPointer(BitPointers.Epc);
                seq.getTargetTag().setMemoryBank(MemoryBank.Epc);
                seq.getTargetTag().setData(targetEpc);
            } else {
                // or just send NULL to apply to all tags
                seq.setTargetTag(null);
            }

            // add to the reader. The reader supports multiple sequences
            reader.addOpSequence(seq);

            // set up listeners to hear stuff back from SDK
            // reader.setTagReportListener(new
            // TagReportListenerImplementation());
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
