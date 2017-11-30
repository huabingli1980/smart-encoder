// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.model;

import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;

@Entity
@Table(name = "order_stat")
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String orderNum;
    private int skuCount;
    private String codeType;
    public int wrongCount;
    public int duplicateCount;
    public int unreadableCount;
    public int readMultipleCount;
    public int unEncodedCount;
    public int piecesCount;
    public int leadingCount;
    public int goodCount;
    
    public int getGoodCount() {
        return this.goodCount;
    }
    
    public void incrGoodCount() {
        ++this.goodCount;
    }
    
    public int getLeadingCount() {
        return this.leadingCount;
    }
    
    public void setLeadingCount(final int leadingCount) {
        this.leadingCount = leadingCount;
    }
    
    public void incrLeadingCount() {
        ++this.leadingCount;
    }
    
    public void incrPiecesCount() {
        ++this.piecesCount;
    }
    
    public void incrWrongCount() {
        ++this.wrongCount;
    }
    
    public void incrUnEncodedCount() {
        ++this.unEncodedCount;
    }
    
    public void incrReadMultipleCount() {
        ++this.readMultipleCount;
    }
    
    public void incrDuplicateCount() {
        ++this.duplicateCount;
    }
    
    public void incrUnreadableCount() {
        ++this.unreadableCount;
    }
    
    public String getCodeType() {
        return this.codeType;
    }
    
    public void setCodeType(final String codeType) {
        this.codeType = codeType;
    }
    
    public String getOrderNum() {
        return this.orderNum;
    }
    
    public void setOrderNum(final String orderNum) {
        this.orderNum = orderNum;
    }
    
    public int getSkuCount() {
        return this.skuCount;
    }
    
    public void setSkuCount(final int skuCount) {
        this.skuCount = skuCount;
    }
    
    @Override
    public String toString() {
        return "Order [orderNum=" + this.orderNum + ", wrongCount=" + this.wrongCount + ", duplicateCount=" + this.duplicateCount + ", unreadableCount=" + this.unreadableCount + ", readMultipleCount=" + this.readMultipleCount + ", unEncodedCount=" + this.unEncodedCount + ", piecesCount=" + this.piecesCount + ", leadingCount=" + this.leadingCount + "]";
    }
}
