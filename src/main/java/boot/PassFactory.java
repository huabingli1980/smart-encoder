// 
// Decompiled by Procyon v0.5.30
// 

package boot;

import java.util.Iterator;
import java.util.Set;
import model.TagInfo;
import java.util.HashSet;
import com.sqlite.domain.OIC;
import com.impinj.octane.Tag;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class PassFactory
{
    public static String getCurrentTime() {
        final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
        final String format = sdf.format(new Date());
        return format;
    }
    
    public static Pass createPass(final List<Tag> tags, final boolean isLeading) {
        final Pass pass = new Pass();
        pass.setTimeStr(getCurrentTime());
        pass.setPassCount(OIC.getPassId());
        pass.setLeading(isLeading);
        if (tags == null || tags.isEmpty()) {
            return pass;
        }
        final Set<TagInfo> mytags = new HashSet<TagInfo>();
        for (final Tag tag : tags) {
            final String tid = tag.getTid().toHexString();
            final String epc = tag.getEpc().toHexString();
            final TagInfo tagInfo = new TagInfo();
            tagInfo.setTid(tid);
            tagInfo.setEpc(epc);
            final String chipType = getChipTypeByTID(tid);
            tagInfo.setChipType(chipType);
            mytags.add(tagInfo);
        }
        pass.setTagInfos(mytags);
        return pass;
    }
    
    public static Pass createPass2(final Set<TagInfo> tags, final boolean isLeading) {
        final Pass pass = new Pass();
        pass.setTimeStr(getCurrentTime());
        pass.setPassCount(OIC.getPassId());
        pass.setLeading(isLeading);
        if (tags == null || tags.isEmpty()) {
            return pass;
        }
        pass.setTagInfos(tags);
        return pass;
    }
    
    private static String getChipTypeByTID(final String tid) {
        String chipType = "UNKNOWN";
        if (tid.startsWith("E2801160")) {
            chipType = "R6";
        }
        else if (tid.startsWith("E2806810")) {
            chipType = "U7";
        }
        return chipType;
    }
}
