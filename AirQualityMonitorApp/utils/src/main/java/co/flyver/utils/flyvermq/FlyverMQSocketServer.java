package co.flyver.utils.flyvermq;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * Created by Petar Petrov on 1/7/15.
 */

/**
 * Socket server handling the external connections to the MessageQueue
 */
public class FlyverMQSocketServer {
    ServerSocket server;
    FlyverMQExternalWorker worker;
    FlyverMQ flyverMQ;

    public FlyverMQSocketServer(FlyverMQ flyverMQ) {
        this.flyverMQ = flyverMQ;
        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(51423));
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    private void start() {
        //noinspection InfiniteLoopStatement
        while(true) {
            try {
                worker = new FlyverMQExternalWorker(flyverMQ, server.accept());
                Thread thread = new Thread(worker);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
