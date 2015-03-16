package co.flyver.utils.settings;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * Created by Petar Petrov on 2/6/15.
 */
public class FlyverPreferences {
    private final static String PATH = Environment.getExternalStorageDirectory()
                                         .toString().concat("/co.flyver/config/");
    private final static String FILENAME = "config.properties";

    private static Properties properties = new Properties();
    private static HashMap<String, String> staged = null;

    // prevent instantiation as all methods are static
    private FlyverPreferences() {
    }


    /**
     * Commits all staged changes to the properties
     * After calling this method all staged changes are cleared
     */
    public static void commit() {
        File file = new File(PATH);

        if(!file.exists()) {
            file.mkdirs();
        }
        if(staged == null) {
            return;
        }

        for(String key : staged.keySet()) {
            properties.setProperty(key, staged.get(key));
        }
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(PATH.concat(FILENAME));
            properties.store(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        staged = null;
    }

    /**
     * Sets the property denoted by the key
     * Also creates new preferences, if they are not present
     * @param key
     * @param value
     */
    public static void setPreference(String key, String value) {
        if(null == staged) {
            staged = new HashMap<>();
        }
        staged.put(key, value);
    }

    /**
     * Returns the value associated with the preference key
     * @param key
     * @return String
     */
    public static String getPreference(String key) {
        File file = new File(PATH.concat(FILENAME));
        if(!file.exists() || file.isDirectory()) {
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties.getProperty(key);
    }
}
