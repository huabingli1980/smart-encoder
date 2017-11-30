/*// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import org.mozilla.universalchardet.CharsetListener;
import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetectUtil
{
    public static String detect(final byte[] content) {
        final UniversalDetector detector = new UniversalDetector((CharsetListener)null);
        detector.handleData(content, 0, content.length);
        detector.dataEnd();
        return detector.getDetectedCharset();
    }
    
    public static void main(final String[] args) throws IOException {
        final byte[] bytes = Files.readAllBytes(new File("H:\\ushare\\U123\\share to jay\\test.txt").toPath());
        System.out.println(detect(bytes));
    }
}
*/