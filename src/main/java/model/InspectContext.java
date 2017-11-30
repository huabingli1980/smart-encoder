// 
// Decompiled by Procyon v0.5.30
// 

package model;

import com.sqlite.model.Order;
import java.util.List;

public class InspectContext
{
    private String configName;
    private List<Order> orders;
    
    public String getConfigName() {
        return this.configName;
    }
    
    public void setConfigName(final String configName) {
        this.configName = configName;
    }
    
    public List<Order> getOrders() {
        return (List<Order>)this.orders;
    }
    
    public void setOrders(final List<Order> orders) {
        this.orders = orders;
    }
}
