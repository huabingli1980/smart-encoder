// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.domain;

import java.util.HashSet;
import com.sqlite.model.Order;
import boot.ApplicationConfig;
import com.sqlite.utils.ReaderManager;
import com.impinj.octane.OctaneSdkException;
import java.util.Iterator;
import model.TagInfo;
import boot.Pass;
import java.util.Set;

public class OIC
{
    public static final String PASS_STATE_GOOD = "GOOD";
    public static final String PASS_STATE_UNENCODED = "UNENCODED";
    public static final String PASS_STATE_DUPLICATE = "DUPLICATE";
    public static final String PASS_STATE_ABNORMAL_HEADER = "ABNORMAL_HEADER";
    public static final String PASS_STATE_MULTIPLE_READ = "multiple-read-exception";
    public static final String PASS_STATE_EMPTY_READ = "BLANK";
    public static final String PATTERN_UNENCODED = "E280";
    public static final String PASS_STATE_UNKNOWN = "UN_KNOWN";
    public static final String PASS_STATE_LEADING = "\u8fc7\u767d\u7eb8";
    public static String curHeader;
    public static String ppHeader;
    public static String prevHeader;
    public static Set<String> orderEpcs;
    public static String codeType;
    public static String jobId;
    public static int pass;
    public static int passCounterForCurrentSection;
    public static int currentSku;
    public static boolean isLeading;
    public static boolean hasStarted;
    public static boolean currentPassState;
    public static int revolveCountAfterPassLow;
    private static boolean isLastPassChange;
    private static boolean isLastPassEmpty;
    private static String firstPassHeader;
    private static String secondPassHeader;
    private static String previousEPC;
    private static int currentSkuPieceCount;
    public static String missingAfter;
    private static boolean isLassPassEmpty2;
    public static long diff;
    
    public static Pass inspect(final Pass pass) throws OctaneSdkException {
        ++OIC.passCounterForCurrentSection;
        ContextManager.incrPiecesCount();
        final Set<TagInfo> tagInfos = pass.getTagInfos();
        final int size = tagInfos.size();
        pass.setState("GOOD");
        TagInfo targetTag = null;
        if (size > 1) {
            pass.setState("multiple-read-exception");
            ContextManager.incrReadMultipleCount();
            for (final TagInfo tagInfo : tagInfos) {
                final String epc = tagInfo.getEpc();
                if (!OIC.orderEpcs.contains(epc)) {
                    pass.setTagInfo(tagInfo);
                    targetTag = tagInfo;
                    break;
                }
            }
        }
        else {
            pass.setTagInfos(null);
            if (size == 0) {
                OIC.isLastPassEmpty = true;
                OIC.isLassPassEmpty2 = true;
                pass.setState("BLANK");
                ContextManager.incrUnreadableCount();
                return pass;
            }
        }
        targetTag = tagInfos.iterator().next();
        pass.setTagInfo(targetTag);
        return dealWithOneTag(pass);
    }
    
    private static Pass dealWithOneTag(final Pass pass) {
        final TagInfo targetTag = pass.getTagInfo();
        final String currentEPC = targetTag.getEpc();
        final String currentHeader = getHeader(currentEPC);
        final boolean isCurrentEPCHeaderChanged = isHeaderChanged(currentHeader);
        if (OIC.previousEPC != null && OIC.isLastPassEmpty) {
            OIC.isLastPassEmpty = false;
            if (isContinousWithPreviousGood(currentEPC)) {
                pass.setLastPassStrikedout(true);
            }
            else {
                pass.setNeedConfirm(true);
                System.out.println("Stopping Printer for confirming ...");
                ReaderManager.stopPrinter(8000);
            }
        }
        if (isDuplicate(targetTag)) {
            pass.setState("DUPLICATE");
            ContextManager.incrDuplicateCount();
            return pass;
        }
        if (isCurrentEPCHeaderChanged) {
            onHeaderChange();
            OIC.currentSkuPieceCount = 1;
            ++OIC.currentSku;
            inInspectLastPass2(pass);
            final int skuCount = ContextManager.currentOrder.getSkuCount();
            if (OIC.currentSku > skuCount) {
                switchOrder();
                pass.setIsOrderStarted(true);
            }
        }
        else {
            ++OIC.currentSkuPieceCount;
            OIC.isLastPassChange = false;
            ContextManager.isLastPassEndOrder = false;
            final Long diff = getDiffFromPrevious(currentEPC, OIC.previousEPC);
            if (!OIC.isLassPassEmpty2 && OIC.previousEPC != null && diff > 1L) {
                System.out.println("Stopping for alerting of disjoin epcs with sku - " + currentEPC + " vs " + OIC.previousEPC);
                pass.setMissPass(true);
                pass.setDiff(diff);
                OIC.missingAfter = currentEPC;
                final Long pulseDuration = Long.valueOf(ApplicationConfig.get("stop.duration", "8000"));
                ReaderManager.stopPrinter(pulseDuration);
            }
        }
        OIC.isLassPassEmpty2 = false;
        OIC.previousEPC = currentEPC;
        OIC.curHeader = currentHeader;
        pass.setSku(OIC.currentSku);
        return pass;
    }
    
    private static void inInspectLastPass2(final Pass pass) {
        if (OIC.isLastPassChange) {
            OIC.isLastPassChange = false;
            if (ContextManager.isLastPassEndOrder) {
                ContextManager.isLastPassEndOrder = false;
                int skuCount = --OIC.currentSku;
                OIC.currentSku = ContextManager.currentOrder.getSkuCount();
                skuCount = OIC.currentSku;
                pass.setOrderNumFrom(ContextManager.currentOrder.getOrderNum());
                pass.setOrder(ContextManager.currentOrder);
                pass.setCorrectiveSku(skuCount);
                pass.setLastPassWrong(true);
                if (OIC.prevHeader.equals(OIC.curHeader)) {
                    ContextManager.switchBack();
                }
                else {
                    pass.setLastPassEndOfOrder(true);
                }
            }
            else {
                pass.setLastPassWrong(true);
                pass.setSku(--OIC.currentSku);
            }
            System.out.println(OIC.currentSku);
            ContextManager.incrWrongCount();
        }
        else {
            OIC.isLastPassChange = true;
        }
    }
    
    private static void switchOrder() {
        ContextManager.loadNextOrder();
        ContextManager.isLastPassEndOrder = true;
        OIC.currentSku = 1;
    }
    
    private static boolean isContinousWithPreviousGood(final String currentEPC) {
        final Long difference = getDiffFromPrevious(currentEPC, OIC.previousEPC);
        final boolean isContinuousWithPrevious = difference == 1L;
        return isContinuousWithPrevious;
    }
    
    public static Long getDiffFromPrevious(final String currentEPC, final String previousEPC) {
        final String lastTenCharOfCurrentEPC = getLastNCharactor(currentEPC, 10);
        final String lastTenCharactorOfPrevEPC = getLastNCharactor(previousEPC, 10);
        final Long difference = Long.parseLong(lastTenCharOfCurrentEPC, 16) - Long.parseLong(lastTenCharactorOfPrevEPC, 16);
        return difference;
    }
    
    private static String getLastNCharactor(final String currentHeader, final int n) {
        return currentHeader.substring(currentHeader.length() - n);
    }
    
    private static String getHeader(final String epcStr) {
        final String headerLen = ApplicationConfig.get("compare.header.len", new String[0]);
        final int len = (headerLen == null) ? 15 : Integer.valueOf(headerLen);
        final String currentHeader = epcStr.substring(0, len);
        return currentHeader;
    }
    
    private static void onHeaderChange() {
        final Boolean stopOnHeaderChange = Boolean.valueOf(ApplicationConfig.get("ctrl.stoponhc", new String[0]));
        if (stopOnHeaderChange) {
            ReaderManager.stopPrinter(8000);
        }
    }
    
    public static void addEpc(final TagInfo epc) {
        OIC.orderEpcs.add(epc.getEpc());
    }
    
    public static int getOrderGoodCount() {
        final int goodCount = OIC.orderEpcs.size();
        OIC.orderEpcs.clear();
        return goodCount;
    }
    
    public static boolean isDuplicate(final TagInfo epc) {
        return OIC.orderEpcs.contains(epc.getEpc());
    }
    
    public static boolean isHeaderChanged(final String nowHeader) {
        return OIC.curHeader == null || !nowHeader.equals(OIC.curHeader);
    }
    
    public static int getPassId() {
        return ++OIC.pass;
    }
    
    public static void reset() {
        OIC.pass = 0;
        OIC.passCounterForCurrentSection = 0;
        OIC.currentSku = 0;
        OIC.orderEpcs.clear();
        ContextManager.orders.clear();
        ContextManager.switchAndKeepFirstPiece = false;
        ContextManager.isLastPassEndOrder = false;
        ContextManager.currentOrder = null;
        OIC.codeType = null;
        OIC.jobId = null;
        OIC.curHeader = null;
        OIC.isLeading = false;
        OIC.revolveCountAfterPassLow = 0;
        OIC.isLastPassChange = false;
        OIC.isLastPassEmpty = false;
        OIC.firstPassHeader = null;
        OIC.secondPassHeader = null;
        OIC.previousEPC = null;
        OIC.ppHeader = "";
        OIC.prevHeader = "";
        OIC.currentSkuPieceCount = 0;
    }
    
    public static void loadOrder(final Order nextOrder) {
        OIC.jobId = nextOrder.getOrderNum();
        OIC.codeType = nextOrder.getCodeType();
    }
    
    static {
        OIC.curHeader = null;
        OIC.ppHeader = "";
        OIC.prevHeader = "";
        OIC.orderEpcs = new HashSet(1500);
    }
}
