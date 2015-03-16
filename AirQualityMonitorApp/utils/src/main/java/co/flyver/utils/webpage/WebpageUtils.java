package co.flyver.utils.webpage;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Petar Petrov on 2/5/15.
 */
public class WebpageUtils {
    private final static String webpageLocation = "/co.flyver/webpage/";

    public static String getWebpageLocation() {
        return Environment.getExternalStorageDirectory().toString().concat(webpageLocation);
    }

    /**
     * Checks if the flyver webpage files have been copied to sdcard/co.flyver/webpage/
     * Copies them if not
     * @param assetManager the asset manager associated with the application context
     */
    public static void deployWebpage(AssetManager assetManager) {
        String path = getWebpageLocation();
        File webpageDir = new File(path);
        //TODO:: commented for debugging purposes, uncomment later
        if (!webpageDir.exists()) {
            webpageDir.mkdirs();
            copyWebpage(path, assetManager);
        }
    }

    /**
     * Copies the files for the webpage from the assets dir, to the /co.flyver/webpage/ dir on the sdcard
     *
     * @param destinationPath
     */
    private static void copyWebpage(String destinationPath, AssetManager assetManager) {
        String[] files = null;
        try {
            files = assetManager.list("webpage");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (files != null) {
            for (String file : files) {
                InputStream inputStream;
                OutputStream outputStream;

                try {
                    inputStream = assetManager.open("webpage/" + file);
                    outputStream = new FileOutputStream(destinationPath.concat(file));
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    inputStream.close();
                    outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
