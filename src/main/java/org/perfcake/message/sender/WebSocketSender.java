package org.perfcake.message.sender;

import org.apache.log4j.Logger;
import org.perfcake.PerfCakeException;
import org.perfcake.message.Message;
import org.perfcake.reporting.MeasurementUnit;

import javax.websocket.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class WebSocketSender extends AbstractSender {

    private static final Logger logger = Logger.getLogger(WebSocketSender.class);

    WebSocketContainer container;
    Session session;

    enum RemoteEndpointType { BASIC, ASYNC }
    private RemoteEndpointType remoteEndpointType;

    enum PayloadType { TEXT, BINARY, PING }
    private PayloadType payloadType;

    public void setRemoteEndpointType(final String remoteEndpointType) {
        switch (remoteEndpointType) {
            case "basic":
                this.remoteEndpointType = RemoteEndpointType.BASIC;
                break;
            case "async":
                this.remoteEndpointType = RemoteEndpointType.ASYNC;
                break;
            default:
                throw new IllegalStateException("Unknown or undefined web socket remote endpoint type. Use either basic or async.");
        }
    }

    public void setPayloadType(final String payloadType) {
        switch (payloadType) {
            case "text":
                this.payloadType = PayloadType.TEXT;
                break;
            case "binary":
                this.payloadType = PayloadType.BINARY;
                break;
            case "ping":
                this.payloadType = PayloadType.PING;
                break;
            default:
                throw new IllegalStateException("Unknown or undefined web socket payload type. Use text, binary, or ping.");
        }
    }

    @Override
    public void init() throws Exception {
        container = ContainerProvider.getWebSocketContainer();
        try {
            logger.info("Connecting to URI " + target);
            container.connectToServer(new PerfCakeClientEndpoint(), new URI(target));
        } catch (DeploymentException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (session == null)
            throw new PerfCakeException("Web socket session cannot be null before the scenario run.");
    }

    @Override
    public void close() throws PerfCakeException {
        try {
            session.close();
        } catch (IOException e) {
            throw new PerfCakeException("Cannot close web socket session.", e);
        }
    }

    @Override
    public Serializable doSend(Message message, Map<String, String> properties, MeasurementUnit mu) throws Exception {
        if (remoteEndpointType == RemoteEndpointType.BASIC) {
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            switch (payloadType) {
                case TEXT:
                    basic.sendText(message.getPayload().toString());
                    break;
                case BINARY:
                    throw new UnsupportedOperationException("Web socket binary payload is not supported yet.");
                    //basic.sendBinary(null);
                case PING:
                    throw new UnsupportedOperationException("Web socket ping payload is not supported yet.");
                    //basic.sendPing(null);
                default:
                    throw new IllegalStateException("Unknown or undefined web socket payload type. Use text, binary or ping.");
            }
        } else if (remoteEndpointType == RemoteEndpointType.ASYNC) {
            RemoteEndpoint.Async async = session.getAsyncRemote();
            switch (payloadType) {
                case TEXT:
                    async.sendText(message.getPayload().toString());
                    break;
                case BINARY:
                    throw new UnsupportedOperationException("Web socket binary payload is not supported yet.");
                    //async.sendBinary(null);
                case PING:
                    throw new UnsupportedOperationException("Web socket ping payload is not supported yet.");
                    //async.sendPing(null);
                default:
                    throw new IllegalStateException("Unknown or undefined web socket payload type. Use text, binary or ping.");
            }
        } else {
            throw new IllegalStateException("Unknown or undefined web socket remote endpoint type. Use either basic or async.");
        }
        return null;
    }

    @ClientEndpoint
    public class PerfCakeClientEndpoint {

        @OnOpen
        public void onOpen(Session session) {
            logger.info("Connected ... " + session.getId());
            WebSocketSender.this.session = session;
        }

        @OnMessage
        public void onMessage(String message, Session session) {
            logger.debug("Received ... " + message);
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            logger.info(String.format("Session %s close because of %s", session.getId(), closeReason));
        }
    }
}
