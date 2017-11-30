// 
// Decompiled by Procyon v0.5.30
// 

package boot;

import java.util.HashSet;
import com.sqlite.model.Order;
import model.TagInfo;
import java.util.Set;

public class Pass
{
    private String state;
    private Set<TagInfo> tagInfos;
    private int passCount;
    private long diff;
    private boolean isLeading;
    private boolean isLastPassWrong;
    private boolean isMissPass;
    private boolean isSkuChange;
    private boolean isStrikeout;
    private boolean needConfirm;
    private String timeStr;
    private long rowId;
    private int correctiveSku;
    private int wrongIndex;
    private TagInfo tagInfo;
    private boolean isEndOfOrder;
    private boolean lastPassEndOfOrder;
    private boolean isEmpty;
    private String orderNum;
    private String orderNumFrom;
    private Order order;
    private int sku;
    
    public Pass() {
        this.tagInfos = new HashSet<TagInfo>();
        this.isLeading = false;
        this.isLastPassWrong = false;
        this.isMissPass = false;
        this.isSkuChange = false;
        this.isStrikeout = false;
        this.needConfirm = false;
        this.tagInfo = new TagInfo();
    }
    
    public long getDiff() {
        return this.diff;
    }
    
    public void setDiff(final long diff) {
        this.diff = diff;
    }
    
    public boolean isMissPass() {
        return this.isMissPass;
    }
    
    public void setMissPass(final boolean isMissPass) {
        this.isMissPass = isMissPass;
    }
    
    public String getTimeStr() {
        return this.timeStr;
    }
    
    public void setTimeStr(final String timeStr) {
        this.timeStr = timeStr;
    }
    
    public long getRowId() {
        return this.rowId;
    }
    
    public void setRowId(final long rowId) {
        this.rowId = rowId;
    }
    
    public boolean isNeedConfirm() {
        return this.needConfirm;
    }
    
    public void setNeedConfirm(final boolean needConfirm) {
        this.needConfirm = needConfirm;
    }
    
    public boolean isStrikeout() {
        return this.isStrikeout;
    }
    
    public void setLastPassStrikedout(final boolean isStrikeout) {
        this.isStrikeout = isStrikeout;
    }
    
    public TagInfo getTagInfo() {
        return this.tagInfo;
    }
    
    public void setTagInfo(final TagInfo tagInfo) {
        this.tagInfo = tagInfo;
    }
    
    public int getWrongIndex() {
        return this.wrongIndex;
    }
    
    public void setWrongIndex(final int wrongIndex) {
        this.wrongIndex = wrongIndex;
    }
    
    public int getCorrectiveSku() {
        return this.correctiveSku;
    }
    
    public void setCorrectiveSku(final int correctiveSku) {
        this.correctiveSku = correctiveSku;
    }
    
    public boolean isSkuChange() {
        return this.isSkuChange;
    }
    
    public void setSkuChange(final boolean isSkuChange) {
        this.isSkuChange = isSkuChange;
    }
    
    public boolean isLastPassWrong() {
        return this.isLastPassWrong;
    }
    
    public void setLastPassWrong(final boolean isLastPassWrong) {
        this.isLastPassWrong = isLastPassWrong;
    }
    
    public boolean isLastPassEndOfOrder() {
        return this.lastPassEndOfOrder;
    }
    
    public void setLastPassEndOfOrder(final boolean lastPassEndOfOrder) {
        this.lastPassEndOfOrder = lastPassEndOfOrder;
    }
    
    public boolean isEmpty() {
        return this.isEmpty;
    }
    
    public void setEmpty(final boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
    
    public String getOrderNumFrom() {
        return this.orderNumFrom;
    }
    
    public void setOrderNumFrom(final String orderNumFrom) {
        this.orderNumFrom = orderNumFrom;
    }
    
    public Order getOrder() {
        return this.order;
    }
    
    public void setOrder(final Order order) {
        this.order = order;
    }
    
    public String getOrderNum() {
        return this.orderNum;
    }
    
    public void setOrderNum(final String orderNum) {
        this.orderNum = orderNum;
    }
    
    public boolean isEndOfOrder() {
        return this.isEndOfOrder;
    }
    
    public void setIsOrderStarted(final boolean isEndOfOrder) {
        this.isEndOfOrder = isEndOfOrder;
    }
    
    public int getSku() {
        return this.sku;
    }
    
    public void setSku(final int sku) {
        this.sku = sku;
    }
    
    public boolean isLeading() {
        return this.isLeading;
    }
    
    public void setLeading(final boolean isLeading) {
        this.isLeading = isLeading;
    }
    
    public String getState() {
        return this.state;
    }
    
    public void setState(final String state) {
        this.state = state;
    }
    
    public Set<TagInfo> getTagInfos() {
        return this.tagInfos;
    }
    
    public void setTagInfos(final Set<TagInfo> tagInfos) {
        this.tagInfos = tagInfos;
    }
    
    public int getPassCount() {
        return this.passCount;
    }
    
    public void setPassCount(final int passCount) {
        this.passCount = passCount;
    }
}
