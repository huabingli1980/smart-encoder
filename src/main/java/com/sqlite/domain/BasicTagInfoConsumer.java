// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.domain;

import com.impinj.octane.Tag;
import com.impinj.octane.TagReport;
import boot.AlertManager;
import boot.ApplicationConfig;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import model.TagInfo;
import com.sqlite.entities.PassInspect;
import com.sqlite.utils.ReaderManager;
import com.impinj.octane.OctaneSdkException;
import boot.Pass;
import com.sqlite.dao.PassInspectDao;
import org.springframework.beans.factory.annotation.Autowired;
import com.sqlite.messaging.MessageManager;
import org.springframework.stereotype.Component;

@Component
public class BasicTagInfoConsumer
{
    private static final String LEADING = "\u8fc7\u767d\u7eb8";
    @Autowired
    private MessageManager messenger;
    @Autowired
    private PassInspectDao passInspectDao;
    
    public void onPass(final Pass pass) throws OctaneSdkException {
        if (!pass.isLeading()) {
            OIC.inspect(pass);
            if (pass.getState().equals("GOOD")) {
                OIC.addEpc(pass.getTagInfo());
                ContextManager.incrGoodCount();
            }
        }
        else {
            pass.setState("\u8fc7\u767d\u7eb8");
            ContextManager.incrLeadingCount();
        }
        pass.setOrder(ContextManager.currentOrder);
        final int rowId = this.persist(pass);
        pass.setRowId(rowId);
        this.messenger.sendPass(pass);
        if (!pass.isLeading()) {
            this.actionOnPass(pass);
        }
    }
    
    private int persist(final Pass pass) {
        final String orderNumTo = pass.getOrder().getOrderNum();
        final PassInspect passInspect = this.getPassInspectFromPass(pass, orderNumTo);
        if (pass.isLastPassWrong()) {
            ReaderManager.stopPrinter(800);
            final String orderNumFrom = pass.getOrderNumFrom();
            if (orderNumFrom != null) {
                this.passInspectDao.updateOrderNumOfPrevious(orderNumFrom, orderNumTo);
            }
            else {
                this.passInspectDao.update(passInspect);
            }
        }
        if (pass.getWrongIndex() != 0) {
            this.passInspectDao.updateFirstOfOrder(orderNumTo);
        }
        final int rowId = this.passInspectDao.save(passInspect);
        return rowId;
    }
    
    private PassInspect getPassInspectFromPass(final Pass pass, final String orderNumTo) {
        final PassInspect passInspect = new PassInspect();
        passInspect.setLeading(pass.isLeading());
        passInspect.setSeq(pass.getPassCount());
        passInspect.setTime(pass.getTimeStr());
        final Set<TagInfo> tagInfos = pass.getTagInfos();
        final TagInfo tagInfo = pass.getTagInfo();
        passInspect.setTid(tagInfo.getTid());
        passInspect.setEpc(tagInfo.getEpc());
        if (tagInfos != null) {
            final StringBuilder sb = new StringBuilder();
            int count = 0;
            for (final TagInfo mytagInfo : tagInfos) {
                sb.append(mytagInfo.getEpc());
                if (count++ != tagInfos.size() - 1) {
                    sb.append(",");
                }
            }
            passInspect.setEpcMultiple(sb.toString());
        }
        passInspect.setOrderNum(orderNumTo);
        passInspect.setStatus(pass.getState());
        return passInspect;
    }
    
    public static void main(final String[] args) {
        int count = 0;
        final List<String> asList = Arrays.asList("a");
        final StringBuilder sb = new StringBuilder();
        for (final String tagInfo : asList) {
            sb.append(tagInfo);
            if (count++ != asList.size() - 1) {
                sb.append(",");
            }
        }
        System.out.println(sb);
    }
    
    private void actionOnPass(final Pass pass) {
        final String state2;
        final String state = state2 = pass.getState();
        switch (state2) {
            case "ABNORMAL_HEADER": {
                final Boolean stopOnPresenceOfAbnormalHeader = Boolean.valueOf(ApplicationConfig.get("ctrl.stoponah", new String[0]));
                if (stopOnPresenceOfAbnormalHeader) {
                    AlertManager.alertAndStop(3);
                    break;
                }
                break;
            }
            case "DUPLICATE": {
                final Boolean stopOnPresenceOfDuplicate = Boolean.valueOf(ApplicationConfig.get("ctrl.stoponduplicate", new String[0]));
                if (stopOnPresenceOfDuplicate) {
                    AlertManager.alertAndStop(2);
                    break;
                }
                break;
            }
            case "BLANK": {
                final Boolean stopOnEmptyRead = Boolean.valueOf(ApplicationConfig.get("ctrl.stoponemptyread", new String[0]));
                if (stopOnEmptyRead) {
                    ReaderManager.stopPrinter(800);
                    break;
                }
                break;
            }
            case "multiple-read-exception": {
                final Boolean stopOnMultipleRead = Boolean.valueOf(ApplicationConfig.get("ctrl.stoponmultipleread", new String[0]));
                if (stopOnMultipleRead) {
                    AlertManager.alertAndStop(4);
                    break;
                }
                break;
            }
        }
    }
    
    public void onRead(final TagReport tagReprot) {
        final List<Tag> tags = (List<Tag>)tagReprot.getTags();
        for (final Tag tag : tags) {
            final String epcHexStr = tag.getEpc().toHexString();
            final String tidHexStr = tag.getTid().toHexString();
            this.messenger.sendReadEPC(epcHexStr, tidHexStr);
        }
    }
    
    public void onGpiChange() {
        this.messenger.sendGpiChange();
    }
    
    public void onProdeTrueSignal(final long currentTimeMillis) {
        this.messenger.sendProdeTrueSignal(currentTimeMillis);
    }
    
    public void onProdeFalseSignal(final long currentTimeMillis) {
        this.messenger.sendProdeFalseSignal(currentTimeMillis);
    }
    
    public void onPrinterStop() {
        this.messenger.sendPrinterStopSignal();
    }
    
    public void onReenter(final List<String> tags) {
        this.messenger.sendReenterEPC((List)tags);
    }
    
    public void sendAdd(final List<Tag> mytags) {
        if (mytags != null && mytags.size() == 1) {
            final Tag tag = mytags.iterator().next();
            final String epc = tag.getEpc().toHexString();
            final String tid = tag.getTid().toHexString();
            final Long difference = OIC.getDiffFromPrevious(OIC.missingAfter, epc);
            final boolean isContinuousWithPrevious = difference == 1L;
            String status = "GOOD";
            if (!isContinuousWithPrevious && OIC.orderEpcs.contains(epc)) {
                status = "DUPLICATE";
            }
            this.messenger.sendAdd(epc, tid, status);
            final PassInspect passInspect = new PassInspect();
            passInspect.setEpc(epc);
            passInspect.setTid(tid);
            passInspect.setStatus(status);
            final String orderNum = ContextManager.currentOrder.getOrderNum();
            passInspect.setOrderNum(orderNum);
            this.passInspectDao.save(passInspect);
        }
    }
}
