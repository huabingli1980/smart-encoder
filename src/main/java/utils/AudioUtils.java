// 
// Decompiled by Procyon v0.5.30
// 

package utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.Line;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioSystem;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;

public class AudioUtils
{
    public static void main(final String[] args) {
        final String home = System.getProperty("user.dir");
        playAudio(home + "/sound/alert_duplicate.wav");
    }
    
    public static void playAudio(final String audioFilePath) {
        final String home = System.getProperty("user.dir");
        final String fullPath = home + audioFilePath;
        SourceDataLine soundLine = null;
        final int BUFFER_SIZE = 65536;
        try {
            final FileInputStream fileInputStream = new FileInputStream(new File(fullPath));
            final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(fileInputStream));
            final AudioFormat audioFormat = audioInputStream.getFormat();
            final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            soundLine = (SourceDataLine)AudioSystem.getLine(info);
            soundLine.open(audioFormat);
            soundLine.start();
            int nBytesRead = 0;
            final byte[] sampledData = new byte[BUFFER_SIZE];
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(sampledData, 0, sampledData.length);
                if (nBytesRead >= 0) {
                    soundLine.write(sampledData, 0, nBytesRead);
                }
            }
        }
        catch (UnsupportedAudioFileException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex2) {
            ex2.printStackTrace();
        }
        catch (LineUnavailableException ex3) {
            ex3.printStackTrace();
        }
        finally {
            soundLine.drain();
            soundLine.close();
        }
    }
}
