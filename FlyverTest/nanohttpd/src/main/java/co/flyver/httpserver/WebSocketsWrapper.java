package co.flyver.httpserver;

import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;

import fi.iki.elonen.IWebSocketFactory;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebSocket;
import fi.iki.elonen.WebSocketFrame;

/**
 * Created by Petar Petrov on 1/27/15.
 */
public class WebSocketsWrapper extends WebSocket {
    private final String TAG = "WebSocket";
    public static LinkedList<WebSocketsWrapper> openSockets = new LinkedList<>();


    public WebSocketsWrapper(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
        openSockets.add(this);
    }

    public static IWebSocketFactory getWebSocketFactory() {
        return new IWebSocketFactory() {
            @Override
            public WebSocket openWebSocket(NanoHTTPD.IHTTPSession handshake) {
                return new WebSocketsWrapper(handshake);
            }
        };
    }

    @Override
    protected void onPong(WebSocketFrame pongFrame) {
        Log.e(TAG, "Pong");

    }

    @Override
    protected void onMessage(WebSocketFrame messageFrame) {
        Log.e(TAG, messageFrame.getTextPayload());

    }

    @Override
    protected void onClose(WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
        openSockets.removeFirst();
        Log.e(TAG, "Closed");
    }

    @Override
    protected void onException(IOException e) {

    }
}
