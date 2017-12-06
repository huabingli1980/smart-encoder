// 
// Decompiled by Procyon v0.5.30
// 

package com.rsmart;

import com.impinj.octane.TagReportListener;
import org.jdeferred.Promise;

public interface IReader
{
    Promise readEpc() throws RuntimeException;
    
    void readPeriodically(final long p0, final TagReportListener p1);
    
    Promise readBlock();
    
    Promise writeEpc(final String p0);
    
    Promise writeUserMemory(final String p0);
    
    Promise writeBlock(final String p0);
    
    Promise readMargin(final String p0);
    
    Promise kill(final String p0);
    
    Promise lockMemory(final int p0, final String p1);
    
    void gpo(final int p0, final boolean p1) throws RuntimeException;
    
    void addGpiListener(final int p0, final GpiListener p1) throws RuntimeException;
}
