package io.ngin.websocket.sample.client.java;

import java.net.URISyntaxException;

import javax.annotation.PostConstruct;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@Component
public class WebSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClient.class);

    private static final String BASE_URL = "https://socket.btcmarkets.net";
    private static final String PATH = "/v2";
    private static final String SUBSCRIBE_EVENT = "subscribe";
    private static final String MESSAGE_EVENT = "message";

    private Socket socket;
    
    @PostConstruct
    private void init() {
        IO.Options options = buildConnectionOptions();

        try {
            socket = IO.socket(BASE_URL, options);
        } catch (URISyntaxException e) {
            LOG.error("error creating socket client", e);
            return;
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit(SUBSCRIBE_EVENT, buildSubscribeRequest());
            }

        }).on(MESSAGE_EVENT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
                LOG.info(obj.toString());
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOG.info("disconnected from websocket");
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                LOG.error("error occured with websocket ", args[0]);
            }
        });
        socket.connect();
    }

    private IO.Options buildConnectionOptions() {
        IO.Options opts = new IO.Options();
        opts.path = PATH;
        opts.secure = true;
        opts.transports = new String[] {"websocket"};
        opts.upgrade = false;
        return opts;
    }

    private JSONObject buildSubscribeRequest() {
        String[] channels = new String[] {"heartbeat", "tick"};
        String[] marketIds = new String[] {"BTC-AUD"};
        JSONObject obj = new JSONObject();
        obj.put("channels", channels);
        obj.put("marketIds", marketIds);
        return obj;
    }

}
