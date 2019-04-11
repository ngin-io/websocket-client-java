package io.ngin.websocket.sample.client.java;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@EnableConfigurationProperties(WebSocketClientProperties.class)
@ClientEndpoint
public class WebSocketClient {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketClient.class);

    @Autowired
    private WebSocketClientProperties prop;
    private Session session = null;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        
        String subscribeMessage = buildSubscribeRequest(prop.getSecret(), prop.getKey(), 
                prop.getChannels(), prop.getMarketIds());
        sendMessage(subscribeMessage);
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        LOG.info("WebSocket client closed.");
        this.session = null;
    }
    
    @OnMessage
    public void onMessage(String message) {
        LOG.info(message);
    }
    
    public void sendMessage(String message) {
        this.session.getAsyncRemote().sendText(message);
    }
    
    @PostConstruct
    private void init() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, new URI(prop.getBaseUrl()));
        } catch (Exception e) {
            LOG.error("error creating socket client", e);
            return;
        }
    }
        
    private String buildSubscribeRequest(String secret, String key, List<String> channels, List<String> marketIds) {
        JSONObject obj = new JSONObject();
        obj.put("channels", channels);
        obj.put("marketIds", marketIds);
        obj.put("messageType", "subscribe");
        
        if (!StringUtils.isEmpty(key)) {
            long timestamp = System.currentTimeMillis() ;
            String strToSign = buildStringToSign("/users/self/subscribe", timestamp);
            String signature = signMessage(secret, strToSign);
            obj.put("signature", signature);
            obj.put("timestamp", timestamp);
            obj.put("key", key);
        }
        return obj.toString();
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
