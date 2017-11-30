// 
// Decompiled by Procyon v0.5.30
// 

package utils;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import java.io.IOException;
import java.io.File;

public class FileUtils
{
    public static void createWrite(final String tt, final String fileNam) {
        final File f = new File(fileNam);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    throw new RuntimeException("Cant create a file");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Sth wrong when setting gpo");
            }
        }
        try {
            Files.write(f.toPath(), tt.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    public static void updateLastLine(final String fileNam) {
        final File f = new File(fileNam);
        if (!f.exists()) {
            try {
                if (!f.createNewFile()) {
                    throw new RuntimeException("Cant create a file");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("Sth wrong when setting gpo");
            }
        }
        try {
            final List<String> lines = Files.readAllLines(f.toPath());
            for (int size = lines.size(), i = 0; i < size; ++i) {
                String lineToWrite = lines.get(i);
                if (i == size - 1) {
                    lineToWrite = lineToWrite.replaceFirst("GOOD", "ABNORMAL_HEADER");
                }
                Files.write(f.toPath(), lineToWrite.getBytes(), StandardOpenOption.WRITE);
            }
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
}
