// 
// Decompiled by Procyon v0.5.30
// 

package model;

public class TagInfo
{
    private String tid;
    private String epc;
    private String chipType;
    
    public TagInfo() {
        this.tid = "";
        this.epc = "";
    }
    
    public String getChipType() {
        return this.chipType;
    }
    
    public void setChipType(final String chipType) {
        this.chipType = chipType;
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.epc == null) ? 0 : this.epc.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final TagInfo other = (TagInfo)obj;
        if (this.epc == null) {
            if (other.epc != null) {
                return false;
            }
        }
        else if (!this.epc.equals(other.epc)) {
            return false;
        }
        return true;
    }
}
