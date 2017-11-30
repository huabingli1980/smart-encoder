// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.entities;

import javax.persistence.GenerationType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "pass_inspects")
public class PassInspect
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int seq;
    private String tid;
    private String epc;
    private String epcMultiple;
    private boolean isLeading;
    private String status;
    private String time;
    private String orderNum;
    
    public PassInspect() {
        this.epcMultiple = "";
    }
    
    public boolean isLeading() {
        return this.isLeading;
    }
    
    public void setLeading(final boolean isLeading) {
        this.isLeading = isLeading;
    }
    
    public String getEpcMultiple() {
        return this.epcMultiple;
    }
    
    public void setEpcMultiple(final String epcMultiple) {
        this.epcMultiple = epcMultiple;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(final int id) {
        this.id = id;
    }
    
    public int getSeq() {
        return this.seq;
    }
    
    public void setSeq(final int seq) {
        this.seq = seq;
    }
    
    public String getTid() {
        return this.tid;
    }
    
    public void setTid(final String tid) {
        this.tid = tid;
    }
    
    public String getEpc() {
        return this.epc;
    }
    
    public void setEpc(final String epc) {
        this.epc = epc;
    }
    
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(final String status) {
        this.status = status;
    }
    
    public String getTime() {
        return this.time;
    }
    
    public void setTime(final String time) {
        this.time = time;
    }
    
    public String getOrderNum() {
        return this.orderNum;
    }
    
    public void setOrderNum(final String orderNum) {
        this.orderNum = orderNum;
    }
}
