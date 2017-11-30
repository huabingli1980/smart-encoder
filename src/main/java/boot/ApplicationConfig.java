// 
// Decompiled by Procyon v0.5.30
// 

package boot;

import java.util.Iterator;
import java.util.Map;
import java.net.URISyntaxException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class ApplicationConfig
{
    private static Properties prop;
    static final String filename = "default.properties";
    
    public static void init() throws IOException {
        load("default.properties");
    }
    
    public static void load(final String fileName) throws IOException, FileNotFoundException {
        final File file = new File(fileName);
        if (!file.exists()) {
            final boolean ok = file.createNewFile();
            if (!ok) {
                throw new RuntimeException("Failed to create file - " + fileName);
            }
        }
        final InputStream config = new FileInputStream(file);
        ApplicationConfig.prop.clear();
        ApplicationConfig.prop.load(config);
    }
    
    public static String get(final String key, final String... defaultValue) {
        final String value = ApplicationConfig.prop.getProperty(key);
        if (defaultValue.length == 0) {
            return value;
        }
        return (value == null) ? defaultValue[0] : value;
    }
    
    public static void set(final String key, final String value) {
        ApplicationConfig.prop.setProperty(key, value);
    }
    
    public static void setAndSave(final String key, final String value) throws FileNotFoundException, IOException, URISyntaxException {
        ApplicationConfig.prop.setProperty(key, value);
        final File file = new File("default.properties");
        ApplicationConfig.prop.store(new FileOutputStream(file), null);
    }
    
    public static void setAndSaveAll(Map<String, String> map) throws FileNotFoundException, IOException, URISyntaxException {
        for (String keys : map.keySet()) {
            final String value = String.valueOf(map.get(keys));
            ApplicationConfig.prop.setProperty(keys, value);
        }
        final String name = map.get("name");
        final String fileName = (name == null) ? "default.properties" : (name + ".properties");
        final File file = new File(fileName);
        if (file.exists()) {
            ApplicationConfig.prop.store(new FileOutputStream(file), null);
        }
        else {
            load(fileName);
        }
    }
    
    public static Properties me(final String name) throws FileNotFoundException, IOException {
        final String fileName = (name == null) ? "default.properties" : (name + ".properties");
        final File file = new File(fileName);
        final Properties prop = new Properties();
        if (file.exists()) {
            prop.load(new FileInputStream(file));
        }
        return prop;
    }
    
    static {
        ApplicationConfig.prop = new Properties();
    }
}
