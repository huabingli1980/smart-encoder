/*package com.impinj.octane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeoutException;

import org.llrp.ltk.generated.custom.enumerations.ImpinjQTAccessRange;
import org.llrp.ltk.generated.custom.enumerations.ImpinjQTDataProfile;
import org.llrp.ltk.generated.custom.enumerations.ImpinjQTPersistence;
import org.llrp.ltk.generated.custom.parameters.ImpinjAccessSpecConfiguration;
import org.llrp.ltk.generated.custom.parameters.ImpinjBlockPermalock;
import org.llrp.ltk.generated.custom.parameters.ImpinjBlockWriteWordCount;
import org.llrp.ltk.generated.custom.parameters.ImpinjGetQTConfig;
import org.llrp.ltk.generated.custom.parameters.ImpinjMarginRead;
import org.llrp.ltk.generated.custom.parameters.ImpinjOpSpecRetryCount;
import org.llrp.ltk.generated.custom.parameters.ImpinjSetQTConfig;
import org.llrp.ltk.generated.enumerations.AccessReportTriggerType;
import org.llrp.ltk.generated.enumerations.AccessSpecState;
import org.llrp.ltk.generated.enumerations.AccessSpecStopTriggerType;
import org.llrp.ltk.generated.enumerations.AirProtocols;
import org.llrp.ltk.generated.enumerations.C1G2LockDataField;
import org.llrp.ltk.generated.enumerations.C1G2LockPrivilege;
import org.llrp.ltk.generated.messages.ADD_ACCESSSPEC_RESPONSE;
import org.llrp.ltk.generated.messages.KEEPALIVE;
import org.llrp.ltk.generated.messages.MyADD_ACCESSSPEC;
import org.llrp.ltk.generated.messages.READER_EVENT_NOTIFICATION;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.parameters.AccessCommand;
import org.llrp.ltk.generated.parameters.AccessReportSpec;
import org.llrp.ltk.generated.parameters.AccessSpec;
import org.llrp.ltk.generated.parameters.AccessSpecStopTrigger;
import org.llrp.ltk.generated.parameters.C1G2BlockWrite;
import org.llrp.ltk.generated.parameters.C1G2Kill;
import org.llrp.ltk.generated.parameters.C1G2Lock;
import org.llrp.ltk.generated.parameters.C1G2LockPayload;
import org.llrp.ltk.generated.parameters.C1G2Read;
import org.llrp.ltk.generated.parameters.C1G2TagSpec;
import org.llrp.ltk.generated.parameters.C1G2TargetTag;
import org.llrp.ltk.generated.parameters.C1G2Write;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedShort;
import org.llrp.ltk.types.UnsignedShortArray_HEX;

import com.rsmart.ReaderHandler;
import com.rsmart.utils.DebugUtils;

public class MyReader extends ImpinjReader {

	
	 * @Override public void addOpSequence(TagOpSequence sequence) throws
	 * OctaneSdkException {
	 * 
	 * System.out.println("Adding Op Sequence");
	 * //super.addOpSequence(sequence); }
	 

	@Override
	void addAccessSpec(TagOpSequence sequence) throws OctaneSdkException {
		DebugUtils.record("start");
		MyADD_ACCESSSPEC msg = new MyADD_ACCESSSPEC();
		msg.setMessageID(this.getUniqueMessageID());
		Object msgErr = null;
		msg.setAccessSpec(new AccessSpec());
		AccessSpec accessSpec = msg.getAccessSpec();
		accessSpec.setAntennaID(new UnsignedShort(sequence.getAntennaId()));
		accessSpec.setROSpecID(new UnsignedInteger(0));
		accessSpec.setProtocolID(new AirProtocols(1));
		accessSpec.setCurrentState(new AccessSpecState(0));
		accessSpec.setAccessSpecStopTrigger(new AccessSpecStopTrigger());
		accessSpec.getAccessSpecStopTrigger().setAccessSpecStopTrigger(new AccessSpecStopTriggerType(1));
		accessSpec.getAccessSpecStopTrigger().setOperationCountValue(new UnsignedShort(sequence.getExecutionCount()));

		accessSpec.setAccessSpecID(new UnsignedInteger(sequence.getId()));
		accessSpec.setAccessCommand(new AccessCommand());
		C1G2TagSpec tagspec = new C1G2TagSpec();
		ArrayList tlist = new ArrayList();
		tagspec.setC1G2TargetTagList(tlist);
		C1G2TargetTag tag = new C1G2TargetTag();
		if (sequence.getTargetTag() == null) {
			sequence.setTargetTag(new TargetTag());
			sequence.getTargetTag().setBitPointer((short) 0);
			sequence.getTargetTag().setData("");
			sequence.getTargetTag().setMemoryBank(MemoryBank.Epc);
		}

		
		 * if (sequence.getTargetTag().getMask() == null) { String olist = "";
		 * 
		 * for (int rsp = 0; rsp < sequence.getTargetTag().getData().length();
		 * ++rsp) { olist = olist + "F"; }
		 * 
		 * sequence.getTargetTag().setMask(olist); }
		 

		tag.setMatch(new Bit(true));
		tag.setMB(this.twoBitFieldFromInt(sequence.getTargetTag().getMemoryBank().getValue()));
		tag.setPointer(new UnsignedShort(sequence.getTargetTag().getBitPointer()));
		tag.setTagData(new BitArray_HEX(sequence.getTargetTag().getData()));
		tag.setTagMask(new BitArray_HEX(sequence.getTargetTag().getMask()));
		tagspec.getC1G2TargetTagList().add(tag);
		accessSpec.getAccessCommand().setAirProtocolTagSpec(tagspec);
		ArrayList arg14 = new ArrayList();
		accessSpec.getAccessCommand().setAccessCommandOpSpecList(arg14);
		Iterator arg15 = sequence.getOps().iterator();

		while (arg15.hasNext()) {
			TagOp msgType = (TagOp) arg15.next();
			if (msgType instanceof TagReadOp) {
				TagReadOp status = (TagReadOp) msgType;
				C1G2Read err = new C1G2Read();
				if (err.getAccessPassword() != null) {
					err.setAccessPassword(new UnsignedInteger(status.getAccessPassword().toDoubleWord()));
				} else {
					err.setAccessPassword(new UnsignedInteger(0));
				}

				err.setMB(this.twoBitFieldFromInt(status.getMemoryBank().getValue()));
				err.setOpSpecID(new UnsignedShort(status.Id));
				err.setWordCount(new UnsignedShort(status.getWordCount()));
				err.setWordPointer(new UnsignedShort(status.getWordPointer()));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(err);
			} else if (msgType instanceof TagWriteOp) {
				TagWriteOp arg19 = (TagWriteOp) msgType;
				if (sequence.isBlockWriteEnabled()) {
					C1G2BlockWrite arg22 = new C1G2BlockWrite();
					if (arg22.getAccessPassword() != null) {
						arg22.setAccessPassword(new UnsignedInteger(arg19.getAccessPassword().toDoubleWord()));
					} else {
						arg22.setAccessPassword(new UnsignedInteger(0));
					}

					arg22.setMB(this.twoBitFieldFromInt(arg19.getMemoryBank().getValue()));
					arg22.setOpSpecID(new UnsignedShort(arg19.Id));
					arg22.setWordPointer(new UnsignedShort(arg19.getWordPointer()));
					arg22.setWriteData(new UnsignedShortArray_HEX(arg19.getData().toHexWordString()));
					accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg22);
				} else {
					C1G2Write arg24 = new C1G2Write();
					if (arg24.getAccessPassword() != null) {
						arg24.setAccessPassword(new UnsignedInteger(arg19.getAccessPassword().toDoubleWord()));
					} else {
						arg24.setAccessPassword(new UnsignedInteger(0));
					}

					arg24.setMB(this.twoBitFieldFromInt(arg19.getMemoryBank().getValue()));
					arg24.setOpSpecID(new UnsignedShort(arg19.Id));
					arg24.setWordPointer(new UnsignedShort(arg19.getWordPointer()));
					arg24.setWriteData(new UnsignedShortArray_HEX(arg19.getData().toHexWordString()));
					accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg24);
				}
			} else if (msgType instanceof TagLockOp) {
				TagLockOp arg20 = (TagLockOp) msgType;
				if (arg20.getLockCount() == 0) {
					throw new OctaneSdkException("The TagLogckOp does not specify any lock operations");
				}

				C1G2Lock arg26 = new C1G2Lock();
				if (arg26.getAccessPassword() != null) {
					arg26.setAccessPassword(new UnsignedInteger(arg20.getAccessPassword().toDoubleWord()));
				} else {
					arg26.setAccessPassword(new UnsignedInteger(0));
				}

				arg26.setOpSpecID(new UnsignedShort(arg20.Id));
				arg26.setC1G2LockPayloadList(new ArrayList());
				C1G2LockPayload lp;
				if (arg20.getKillPasswordLockType() != TagLockState.None) {
					lp = new C1G2LockPayload();
					lp.setPrivilege(new C1G2LockPrivilege(arg20.getKillPasswordLockType().getValue()));
					lp.setDataField(new C1G2LockDataField(LockMemoryBank.KillPassword.getValue()));
					arg26.addToC1G2LockPayloadList(lp);
				}

				if (arg20.getAccessPasswordLockType() != TagLockState.None) {
					lp = new C1G2LockPayload();
					lp.setPrivilege(new C1G2LockPrivilege(arg20.getAccessPasswordLockType().getValue()));
					lp.setDataField(new C1G2LockDataField(LockMemoryBank.AccessPassword.getValue()));
					arg26.addToC1G2LockPayloadList(lp);
				}

				if (arg20.getEpcLockType() != TagLockState.None) {
					lp = new C1G2LockPayload();
					lp.setPrivilege(new C1G2LockPrivilege(arg20.getEpcLockType().getValue()));
					lp.setDataField(new C1G2LockDataField(LockMemoryBank.Epc.getValue()));
					arg26.addToC1G2LockPayloadList(lp);
				}

				if (arg20.getTidLockType() != TagLockState.None) {
					lp = new C1G2LockPayload();
					lp.setPrivilege(new C1G2LockPrivilege(arg20.getTidLockType().getValue()));
					lp.setDataField(new C1G2LockDataField(LockMemoryBank.Tid.getValue()));
					arg26.addToC1G2LockPayloadList(lp);
				}

				if (arg20.getUserLockType() != TagLockState.None) {
					lp = new C1G2LockPayload();
					lp.setPrivilege(new C1G2LockPrivilege(arg20.getUserLockType().getValue()));
					lp.setDataField(new C1G2LockDataField(LockMemoryBank.User.getValue()));
					arg26.addToC1G2LockPayloadList(lp);
				}

				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg26);
			} else if (msgType instanceof TagKillOp) {
				TagKillOp arg21 = (TagKillOp) msgType;
				C1G2Kill arg28 = new C1G2Kill();
				arg28.setOpSpecID(new UnsignedShort(arg21.Id));
				arg28.setKillPassword(new UnsignedInteger(arg21.getKillPassword().toDoubleWord()));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg28);
			} else if (msgType instanceof TagBlockPermalockOp) {
				TagBlockPermalockOp arg23 = (TagBlockPermalockOp) msgType;
				ImpinjBlockPermalock arg31 = new ImpinjBlockPermalock();
				if (arg31.getAccessPassword() != null) {
					arg31.setAccessPassword(new UnsignedInteger(arg23.getAccessPassword().toDoubleWord()));
				} else {
					arg31.setAccessPassword(new UnsignedInteger(0));
				}

				arg31.setMB(this.twoBitFieldFromInt(MemoryBank.User.getValue()));
				arg31.setOpSpecID(new UnsignedShort(arg23.Id));
				arg31.setBlockPointer(new UnsignedShort(0));
				arg31.setBlockMask(new UnsignedShortArray_HEX(arg23.getBlockMask().toHexWordString()));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg31);
			} else if (msgType instanceof TagMarginReadOp) {
				TagMarginReadOp arg25 = (TagMarginReadOp) msgType;
				ImpinjMarginRead arg32 = new ImpinjMarginRead();
				if (arg25.getAccessPassword() != null) {
					arg32.setAccessPassword(new UnsignedInteger(arg25.getAccessPassword().toDoubleWord()));
				} else {
					arg32.setAccessPassword(new UnsignedInteger(0));
				}

				arg32.setMB(this.twoBitFieldFromInt(arg25.getMemoryBank().getValue()));
				arg32.setOpSpecID(new UnsignedShort(arg25.Id));
				arg32.setBitPointer(new UnsignedShort(arg25.getBitPointer()));
				arg32.setBitLength(new UnsignedByte(arg25.getMarginMask().getBitLength()));
				arg32.setMask(new UnsignedShortArray_HEX(arg25.getMarginMask().toHexWordString()));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg32);
			} else if (msgType instanceof TagQtSetOp) {
				TagQtSetOp arg27 = (TagQtSetOp) msgType;
				ImpinjSetQTConfig arg33 = new ImpinjSetQTConfig();
				if (arg27.getAccessPassword() != null) {
					arg33.setAccessPassword(new UnsignedInteger(arg27.getAccessPassword().toDoubleWord()));
				} else {
					arg33.setAccessPassword(new UnsignedInteger(0));
				}

				arg33.setAccessRange(new ImpinjQTAccessRange(arg27.getAccessRange().getValue()));
				arg33.setDataProfile(new ImpinjQTDataProfile(arg27.getDataProfile().getValue()));
				arg33.setPersistence(new ImpinjQTPersistence(arg27.getPersistence().getValue()));
				arg33.setOpSpecID(new UnsignedShort(arg27.Id));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg33);
			} else if (msgType instanceof TagQtGetOp) {
				TagQtGetOp arg29 = (TagQtGetOp) msgType;
				ImpinjGetQTConfig arg34 = new ImpinjGetQTConfig();
				if (arg34.getAccessPassword() != null) {
					arg34.setAccessPassword(new UnsignedInteger(arg29.getAccessPassword().toDoubleWord()));
				} else {
					arg34.setAccessPassword(new UnsignedInteger(0));
				}

				arg34.setOpSpecID(new UnsignedShort(arg29.Id));
				accessSpec.getAccessCommand().addToAccessCommandOpSpecList(arg34);
			}
		}

		accessSpec.setAccessReportSpec(new AccessReportSpec());
		accessSpec.getAccessReportSpec().setAccessReportTrigger(new AccessReportTriggerType(0));
		if (sequence.isBlockWriteEnabled()) {
			ImpinjAccessSpecConfiguration arg16 = new ImpinjAccessSpecConfiguration();
			arg16.setImpinjBlockWriteWordCount(new ImpinjBlockWriteWordCount());
			arg16.getImpinjBlockWriteWordCount().setWordCount(new UnsignedShort(sequence.getBlockWriteWordCount()));
			arg16.setImpinjOpSpecRetryCount(new ImpinjOpSpecRetryCount());
			arg16.getImpinjOpSpecRetryCount().setRetryCount(new UnsignedShort(sequence.getBlockWriteRetryCount()));
			accessSpec.addToCustomList(arg16);
		}

		ADD_ACCESSSPEC_RESPONSE arg17 = null;
		String arg18 = "ADD_ACCESSSPEC";
		DebugUtils.record("start1");
		try {
			arg17 = (ADD_ACCESSSPEC_RESPONSE) this.readerConnection.transact(msg, (long) this.messageTimeout);
		} catch (TimeoutException arg13) {
			this.throwTimeoutException(arg18);
		}
		DebugUtils.record("start3");

		
		 * this.checkForNullReply(arg18, arg17, (ERROR_MESSAGE) msgErr);
		 * StatusCode arg30 = arg17.getLLRPStatus().getStatusCode(); if
		 * (!arg30.equals(new StatusCode("M_Success"))) { String arg35 =
		 * "failure";
		 * 
		 * try { arg35 = arg17.toXMLString(); } catch
		 * (InvalidLLRPMessageException arg12) { ; }
		 * 
		 * throw new OctaneSdkException("Error adding access specs: " + arg35);
		 * }
		 
	}

	@Override
	public void messageReceived(LLRPMessage message) {
		DebugUtils.record("342kk");
		super.messageReceived(message);
	}

	@Override
	void doMessageReceived(LLRPMessage message) {
		// ReaderHandler.record("do mesg:");
		if (message.getTypeNum() == RO_ACCESS_REPORT.TYPENUM) {
			RO_ACCESS_REPORT keepalive = (RO_ACCESS_REPORT) message;
			try {
				this.onTagReportAvailableInternal(keepalive);
			} catch (OctaneSdkException arg4) {
				arg4.printStackTrace();
			}
			
		} else if (message.getTypeNum() == READER_EVENT_NOTIFICATION.TYPENUM) {
			READER_EVENT_NOTIFICATION keepalive1 = (READER_EVENT_NOTIFICATION) message;

			try {
				this.onReaderEventInternal(keepalive1);
			} catch (Exception arg3) {

				arg3.printStackTrace();
			}
		} else if (message.getTypeNum() == KEEPALIVE.TYPENUM) {
			KEEPALIVE keepalive2 = (KEEPALIVE) message;
			this.onKeepaliveMessage(keepalive2);
		}
	}

}
*/