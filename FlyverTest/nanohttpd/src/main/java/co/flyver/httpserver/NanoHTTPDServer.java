package co.flyver.httpserver;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import co.flyver.utils.JSONUtils;
import co.flyver.utils.flyvermq.FlyverMQ;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.exceptions.FlyverMQException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebSocketResponseHandler;

/**
 * Created by Petar Petrov on 1/6/15.
 */

/**
 * Subclass of NanoHTTPD, used to serve webpages from the smartphone
 */
public class NanoHTTPDServer extends NanoHTTPD implements FlyverMQConsumer {
    WebSocketResponseHandler responseHandler;
    public static final String MIME_JS = "application/javascript";
    public static final String MIME_CSS = "text/css";
    public static final String MIME_PNG = "image/png";
    public NanoHTTPDServer() {
        super(8080);
        responseHandler = new WebSocketResponseHandler(WebSocketsWrapper.getWebSocketFactory());
        try {
            FlyverMQ.getInstance().registerConsumer(this, "airquality.mapped");
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        Response ws = responseHandler.serve(session);
        if (ws == null) {
            final String sourcePath = "/co.flyver/webpage";
            String mime_type = NanoHTTPD.MIME_HTML;
            Method method = session.getMethod();
            String uri = session.getUri();
            Map<String, String> files = new HashMap<>();
            BufferedReader bufferedReader;
            try {
                session.parseBody(files);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ResponseException e) {
                e.printStackTrace();
            }
            if (method.toString().equalsIgnoreCase("GET")) {
                File sdcard = Environment.getExternalStorageDirectory();
                File resource = new File(sdcard, sourcePath.concat("/index.html"));
                InputStream stream = null;
                if (uri.endsWith(".html")) {
                    mime_type = NanoHTTPD.MIME_HTML;

                } else if (uri.endsWith(".js")) {
                    mime_type = MIME_JS;
                    resource = new File(sdcard, sourcePath.concat(uri));

                } else if (uri.endsWith(".css")) {
                    resource = new File(sdcard, sourcePath.concat(uri));
                    mime_type = MIME_CSS;
                } else if(uri.endsWith(".png")) {
                    resource = new File(sdcard, sourcePath.concat(uri));
                    mime_type = MIME_PNG;
                }

                try {
                    stream = new FileInputStream(resource);
//                    String line;
//                    while ((line = bufferedReader.readLine()) != null) {
//                        html.append(line);
//                        html.append("\n");
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new Response(Response.Status.OK, mime_type, stream);
            }
            if (method.toString().equalsIgnoreCase("POST")) {
                switch (files.get("postData")) {
                    case "file": {
                        File file = new File(Environment.getExternalStorageDirectory().getPath().concat("/co.flyver/airQualityData.csv"));
                        StringBuilder sb = new StringBuilder();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line.concat("|"));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, sb.toString());
                    }
                    case "reset": {
                        File file = new File(Environment.getExternalStorageDirectory().getPath().concat("/co.flyver/airQualityData.csv"));
                        file.delete();
                    }
                    default: {
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, "DEFAULT!");
                    }
                }
            }
            return new Response(Response.Status.NOT_FOUND, MIME_HTML, "UNAVAILABLE!!!");
        } else {
            return ws;
        }
    }

    @Override
    public void dataReceived(FlyverMQMessage message) {
        try {
            if(!WebSocketsWrapper.openSockets.isEmpty()) {
                WebSocketsWrapper.openSockets.getFirst().send(JSONUtils.serialize(message.data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unregistered() {

    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }
}
