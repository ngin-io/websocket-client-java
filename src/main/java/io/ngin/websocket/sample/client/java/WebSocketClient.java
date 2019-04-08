package io.ngin.websocket.sample.client.java;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@Component
@EnableConfigurationProperties(WebSocketClientProperties.class)
public class WebSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClient.class);
    private static final String MESSAGE_EVENT = "message";

    private Socket socket;

    @Autowired
    private WebSocketClientProperties prop;
    
    @PostConstruct
    private void init() {
        IO.Options options = buildConnectionOptions(prop.getPath());

        try {
            socket = IO.socket(prop.getBaseUrl(), options);
        } catch (URISyntaxException e) {
            LOG.error("error creating socket client", e);
            return;
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit("subscribe", buildSubscribeRequest(prop.getSecret(), prop.getKey(), prop.getChannels(), prop.getMarketIds()));
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

    private IO.Options buildConnectionOptions(String path) {
        IO.Options opts = new IO.Options();
        opts.path = path;
        opts.secure = true;
        opts.transports = new String[] {"websocket"};
        opts.upgrade = false;
        return opts;
    }

    private JSONObject buildSubscribeRequest(String secret, String key, List<String> channels, List<String> marketIds) {
        JSONObject obj = new JSONObject();
        obj.put("channels", channels);
        obj.put("marketIds", marketIds);
        
        if (!StringUtils.isEmpty(key)) {
            long timestamp = System.currentTimeMillis() ;
            String strToSign = buildStringToSign("/users/self/subscribe", timestamp);
            String signature = signMessage(secret, strToSign);
            obj.put("signature", signature);
            obj.put("timestamp", timestamp);
            obj.put("key", key);
        }
        return obj;
    }
    
    private static String buildStringToSign(String path, long timestamp) {
        return path + "\n" + timestamp;
    }

    private static String signMessage(String secret, String data) {
        String signature = "";
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            byte[] key = Base64.getDecoder().decode(secret);
            SecretKeySpec keSpec = new SecretKeySpec(key, "HmacSHA512");
            mac.init(keSpec);
            signature = Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes()));
        } catch (Exception e) {
            LOG.error("unable to sign the request", e);
        }
        return signature;
    }

}
