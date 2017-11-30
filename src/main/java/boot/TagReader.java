// 
// Decompiled by Procyon v0.5.30
// 

package boot;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

public class TagReader
{
    Set<TagInfoConsumer> tagInfoConsumers;
    
    public TagReader() {
        this.tagInfoConsumers = new HashSet<TagInfoConsumer>();
    }
    
    public void addTagInfoConsumer(final TagInfoConsumer tagInfoConsumer) {
        this.tagInfoConsumers.add(tagInfoConsumer);
    }
    
    public void onPass(final Pass pass) {
        for (final TagInfoConsumer tagInfoConsumer : this.tagInfoConsumers) {
            tagInfoConsumer.onPass(pass);
        }
    }
}
