// 
// Decompiled by Procyon v0.5.30
// 

package com.rsmart;

public class CountTryer
{
    Executor executor;
    int tryCount;
    boolean exitFlag;
    
    public CountTryer(final Executor executor) {
        this.exitFlag = false;
        this.executor = executor;
    }
    
    public CountTryer time(final int i) {
        this.tryCount = i;
        return this;
    }
    
    public void onSuccess() {
        this.exitFlag = true;
    }
    
    public void start() {
        System.out.println("starting....");
        for (int i = 0; i < this.tryCount; ++i) {
            System.out.println("executing...");
            this.executor.execute(this);
            System.out.println("executing called");
            if (this.exitFlag) {
                System.out.println("Trying " + (i + 1) + " and succeed!");
                break;
            }
        }
    }
}
