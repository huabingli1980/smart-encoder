// 
// Decompiled by Procyon v0.5.30
// 

package service;

import utils.AudioUtils;

public class SoundAlertManager
{
    public static final String HOME;
    public static final String PATH_EMPTY_READ = "/sound/alert_empty_read_exception.wav";
    public static final String PATH_MULTIPLE_READ = "/sound/alert_multiple_read_exception.wav";
    public static final String PATH_ABNORMAL_HEADER = "/sound/alert_abnormal_header.wav";
    public static final String PATH_UNREADABLE = "/sound/alert_unreadable.wav";
    public static final String PATH_DUPLICATE = "/sound/alert_duplicate.wav";
    public static final String PATH_READ_SUCCESSFUL = "/sound/inform_read_successful.wav";
    
    public static void main(final String[] args) {
        new SoundAlertManager().alertDeadChip();
    }
    
    public void alertDeadChip() {
        AudioUtils.playAudio("/sound/alert_unreadable.wav");
    }
    
    public void alertDuplicateEPC() {
        AudioUtils.playAudio("/sound/alert_duplicate.wav");
    }
    
    public void alertAbnormalHeader() {
        AudioUtils.playAudio("/sound/alert_abnormal_header.wav");
    }
    
    public void alertReadMultiple() {
        AudioUtils.playAudio("/sound/alert_multiple_read_exception.wav");
    }
    
    public void alertReadEmpty() {
        AudioUtils.playAudio("/sound/alert_empty_read_exception.wav");
    }
    
    public void informReadSuccessful() {
        AudioUtils.playAudio("/sound/inform_read_successful.wav");
    }
    
    static {
        HOME = System.getProperty("user.dir");
    }
}
