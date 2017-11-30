// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.license;

import com.sqlite.license.model.LicenseFailureMessage;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Calendar;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.security.SignatureException;
import java.security.NoSuchProviderException;
import java.security.NoSuchAlgorithmException;
import org.bouncycastle.openpgp.PGPException;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.verhas.licensor.License;
import java.io.File;

public class LicenseManager
{
    String home;
    String publicKeyFilePath;
    String privateKeyFilePath;
    String licenseFilePath;
    File file;
    
    public LicenseManager() {
        this.home = System.getProperty("user.dir");
        this.publicKeyFilePath = this.home + "/license/test.gpg";
        this.privateKeyFilePath = this.home + "/license/secring.gpg";
        this.licenseFilePath = this.home + "/license/demo.license";
        this.file = new File(this.licenseFilePath);
    }
    
    public boolean checkLicense() throws FileNotFoundException, IOException, PGPException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        if (!this.file.exists()) {
            this.createDemoLicense();
        }
        final License license = new License();
        final License licenseLoaded = license.loadKeyRing(new File(this.publicKeyFilePath), (byte[])null).setLicenseEncodedFromFile(this.licenseFilePath);
        final String issueDate = licenseLoaded.getFeature("issue-date");
        final String validDate = licenseLoaded.getFeature("valid-date");
        System.out.println("License will be expired at " + validDate);
        if (!licenseLoaded.isVerified()) {
            throw new RuntimeException("Invalid license config!");
        }
        this.checkDateAndVersionValidity(issueDate, validDate);
        final String licenseMac = license.getFeature("mac");
        final String mac = this.getMacAddress();
        if (licenseMac != null && licenseMac.equals(mac)) {
            System.out.println(mac);
            return false;
        }
        throw new RuntimeException("This pc is not licensed!");
    }
    
    private String getMacAddress() throws UnknownHostException, SocketException {
        final InetAddress ip = InetAddress.getLocalHost();
        final NetworkInterface inetAddr = NetworkInterface.getByInetAddress(ip);
        final byte[] mac = inetAddr.getHardwareAddress();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; ++i) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }
    
    private void checkDateAndVersionValidity(final String issueDate, final String validDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.valueOf(issueDate));
        final Calendar today = Calendar.getInstance();
        if (!calendar.before(today)) {
            throw new IllegalArgumentException("Issue date is too late, probably tampered system time");
        }
        if (validDate != null) {
            final Calendar valid = Calendar.getInstance();
            valid.setTimeInMillis(Long.valueOf(validDate));
            if (today.after(valid)) {
                throw new IllegalArgumentException("License expired.");
            }
        }
    }
    
    private void createDemoLicense() throws NoSuchAlgorithmException, NoSuchProviderException, SignatureException, IOException, PGPException {
        if (!this.file.exists()) {
            this.file.createNewFile();
        }
        final int days = 30;
        final String macAddr = this.getMacAddress();
        final Properties prop = new Properties();
        final long offsetTime = days * 60 * 60 * 1000;
        prop.setProperty("mac", macAddr);
        prop.setProperty("issue-date", String.valueOf(System.currentTimeMillis()));
        prop.setProperty("valid-date", String.valueOf(System.currentTimeMillis() + offsetTime));
        prop.save(new FileOutputStream(this.file), "license");
        final License license = new License();
        final byte[] bytes = license.setLicense(this.file).loadKey(this.privateKeyFilePath, "zdinspector").encodeLicense("test").getBytes();
        final OutputStream os = new FileOutputStream(new File(this.licenseFilePath));
        os.write(bytes);
        os.close();
    }
    
    public LicenseFailureMessage getLicenseFailureMessage() {
        return null;
    }
}
