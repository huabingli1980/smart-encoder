// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.utils;

import java.io.IOException;
import org.mozilla.universalchardet.UniversalDetector;
import java.io.FileInputStream;

public class TestDetector
{
    public static void main(final String[] args) throws IOException {
        final byte[] buf = new byte[4096];
        final FileInputStream fis = new FileInputStream("H:\\ushare\\U123\\share to jay\\2017_4533629001.txt");
        final UniversalDetector detector = new UniversalDetector();
        int nread;
        while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }
        detector.dataEnd();
        final String encoding = detector.getDetectedCharset();
        if (encoding != null) {
            System.out.println("Detected encoding = " + encoding);
        }
        else {
            System.out.println("No encoding detected.");
        }
        detector.reset();
    }
}
