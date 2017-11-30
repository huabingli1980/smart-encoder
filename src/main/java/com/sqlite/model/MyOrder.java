// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.model;

import java.util.HashSet;
import java.util.Set;

public class MyOrder
{
    private Set<SKUProduction> skuProds;
    
    public MyOrder() {
        this.skuProds = new HashSet<SKUProduction>();
    }
    
    public Set<SKUProduction> getSkuProds() {
        return this.skuProds;
    }
    
    public void setSkuProds(final Set<SKUProduction> skuProds) {
        this.skuProds = skuProds;
    }
}
