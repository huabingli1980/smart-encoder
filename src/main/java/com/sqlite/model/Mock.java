// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.model;

import com.impinj.octane.Tag;
import java.util.UUID;
import java.util.Set;
import boot.PassFactory;
import model.TagInfo;
import java.util.HashSet;
import com.impinj.octane.OctaneSdkException;
import java.util.List;
import java.util.Collection;
import boot.Pass;
import java.util.ArrayList;
import com.sqlite.domain.BasicTagInfoConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import com.sqlite.messaging.MessageManager;
import org.springframework.stereotype.Component;

@Component
public class Mock
{
    @Autowired
    private MessageManager mm;
    @Autowired
    private BasicTagInfoConsumer ctic;
    
    public static void main(final String[] args) {
        final Long d = Long.parseLong("483002BB", 16) - Long.parseLong("483002B9", 16);
        System.out.println(d == 1L);
    }
    
    public void doIt() throws OctaneSdkException {
        final List<Pass> passs = new ArrayList<Pass>();
        passs.addAll(this.createLeadingPass("E342234789ABCDEF00", 1));
        passs.addAll(this.createOnePass("3034329f40229432210006d"));
        passs.addAll(this.createOnePass("3034329f40229432210006f"));
        passs.addAll(this.createOnePass("3034329f402294322100070"));
        passs.addAll(this.createOnePass("3034329f402294322100090"));
        passs.addAll(this.createListSkuPassFrom("123456789ABCDEF0", 4, 9));
        passs.addAll(this.createMultipleReadPass("323456789ABCDEF00", 3));
        passs.addAll(this.createDeadPass("", 1));
        passs.addAll(this.createListSkuPass("223456789ABCDEF00", 3, -2));
        for (int j = 0; j < passs.size(); ++j) {
            final Pass pass = passs.get(j);
            this.ctic.onPass(pass);
        }
    }
    
    private List<Pass> createListRange(final String skuHeader) {
        final List<Pass> passs = new ArrayList<Pass>();
        final Set<TagInfo> tag = new HashSet<TagInfo>();
        final TagInfo tag2 = new TagInfo();
        tag2.setEpc(skuHeader);
        tag2.setTid(System.currentTimeMillis() + "t");
        tag.add(tag2);
        final Pass pass = PassFactory.createPass2(tag, false);
        passs.add(pass);
        return passs;
    }
    
    private List<Pass> createListSkuPassFrom(final String skuHeader, final int pieces, final int n) {
        final List<Pass> passs = new ArrayList<Pass>();
        for (int i = n; i < pieces + n; ++i) {
            final Set<TagInfo> tag = new HashSet<TagInfo>();
            final TagInfo tag2 = new TagInfo();
            tag2.setEpc(skuHeader + (i + 1));
            tag2.setTid(System.currentTimeMillis() + "t");
            tag.add(tag2);
            final Pass pass = PassFactory.createPass2(tag, false);
            passs.add(pass);
        }
        return passs;
    }
    
    private List<Pass> createListSkuPass(final String skuHeader, final int pieces, final int n) {
        final List<Pass> passs = new ArrayList<Pass>();
        for (int i = 0; i < pieces; ++i) {
            final Set<TagInfo> tag = new HashSet<TagInfo>();
            final TagInfo tag2 = new TagInfo();
            tag2.setEpc(skuHeader + (i + 1));
            tag2.setTid(System.currentTimeMillis() + "t");
            if (n >= 0 && i == n) {
                tag2.setEpc(UUID.randomUUID().toString().substring(0, 19));
            }
            tag.add(tag2);
            final Pass pass = PassFactory.createPass2(tag, false);
            passs.add(pass);
        }
        return passs;
    }
    
    private List<Pass> createLeadingPass(final String skuHeader, final int pieces) {
        final List<Pass> passs = new ArrayList<Pass>();
        for (int i = 0; i < pieces; ++i) {
            final Set<TagInfo> tag = new HashSet<TagInfo>();
            final TagInfo tag2 = new TagInfo();
            tag2.setEpc(skuHeader + (i + 1));
            tag2.setTid(System.currentTimeMillis() + "t");
            tag.add(tag2);
            final Pass pass = PassFactory.createPass2(tag, true);
            passs.add(pass);
        }
        return passs;
    }
    
    private List<Pass> createOnePass(final String epc) {
        final List<Pass> passs = new ArrayList<Pass>();
        final TagInfo tag2 = new TagInfo();
        tag2.setEpc(epc);
        tag2.setTid(System.currentTimeMillis() + "t");
        final Set<TagInfo> tag3 = new HashSet<TagInfo>();
        tag3.add(tag2);
        final Pass pass = PassFactory.createPass2(tag3, false);
        passs.add(pass);
        return passs;
    }
    
    private List<Pass> createDeadPass(final String skuHeader, final int pieces) {
        final List<Pass> passs = new ArrayList<Pass>();
        passs.add(PassFactory.createPass(null, false));
        return passs;
    }
    
    private List<Pass> createMultipleReadPass(final String skuHeader, final int pieces) {
        final List<Pass> passs = new ArrayList<Pass>();
        final Set<TagInfo> tag = new HashSet<TagInfo>();
        for (int i = 0; i < pieces; ++i) {
            final TagInfo tag2 = new TagInfo();
            tag2.setEpc(skuHeader + (i + 1));
            tag2.setTid("11234" + i);
            tag.add(tag2);
        }
        final Pass pass = PassFactory.createPass2(tag, false);
        passs.add(pass);
        return passs;
    }
}
