package com.github.mostroverkhov.firebase_rsocket;

import com.github.mostroverkhov.firebase_rsocket.auth.Authenticator;
import com.github.mostroverkhov.firebase_rsocket.server.handler.HandlerManager;
import com.github.mostroverkhov.firebase_rsocket.server.mapper.DefaultRequestMapper;
import com.github.mostroverkhov.firebase_rsocket.server.mapper.RequestMapper;
import com.github.mostroverkhov.firebase_rsocket_data.common.model.Operation;
import com.google.gson.Gson;
import io.reactivesocket.AbstractReactiveSocket;
import io.reactivesocket.ConnectionSetupPayload;
import io.reactivesocket.Payload;
import io.reactivesocket.ReactiveSocket;
import io.reactivesocket.lease.DisabledLeaseAcceptingSocket;
import io.reactivesocket.lease.LeaseEnforcingSocket;
import io.reactivesocket.server.ReactiveSocketServer;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * Author: mostroverkhov
 */
public class ServerSocketAcceptor implements ReactiveSocketServer.SocketAcceptor {

    private Authenticator authenticator;
    private HandlerManager handlerManager;
    private final Gson gson;

    ServerSocketAcceptor(Authenticator authenticator,
                         HandlerManager handlerManager,
                         Gson gson) {
        this.authenticator = authenticator;
        this.handlerManager = handlerManager;
        this.gson = gson;
    }

    @Override
    public LeaseEnforcingSocket accept(ConnectionSetupPayload setupPayload,
                                       ReactiveSocket reactiveSocket) {
        return new DisabledLeaseAcceptingSocket(
                new FirebaseReactiveSocket(
                        new SocketContext(authenticator, gson),
                        handlerManager,
                        new DefaultRequestMapper(gson)));
    }

    private static class FirebaseReactiveSocket extends AbstractReactiveSocket {

        private final SocketContext context;
        private final HandlerManager handlerManager;
        private final RequestMapper<?> requestHandlerAdapter;

        public FirebaseReactiveSocket(SocketContext context,
                                      HandlerManager handlerManager,
                                      RequestMapper<?> requestHandlerAdapter) {
            this.context = context;
            this.handlerManager = handlerManager;
            this.requestHandlerAdapter = requestHandlerAdapter;
        }

        @Override
        public Publisher<Payload> requestStream(Payload payload) {

            Flowable<byte[]> requestFlow = Flowable.fromCallable(() -> bytes(payload))
                    .observeOn(Schedulers.io())
                    .cache();

            Flowable<Optional<Publisher<Payload>>> responseFlow = requestFlow
                    .map(request ->
                            requestHandlerAdapter
                                    .map(request)
                                    .map(this::handleRequest));
            Flowable<Publisher<Payload>> succFlow = responseFlow
                    .filter(Optional::isPresent)
                    .map(Optional::get);
            Flowable<Publisher<Payload>> succOrErrorFlow = succFlow
                    .switchIfEmpty(requestFlow
                            .flatMap(r -> Flowable.error(requestMissingHandlerAdapter(bytesToString(r)))));

            return succOrErrorFlow.flatMap(pub -> pub);
        }

        private Publisher<Payload> handleRequest(Operation adaptedRequest) {
            return context.authenticator().authenticate()
                    .andThen(Flowable.defer(
                            () -> handlerManager
                                    .handler(adaptedRequest)
                                    .handle(context, adaptedRequest)));
        }

        private static byte[] bytes(Payload payload) {
            ByteBuffer bb = payload.getData();
            byte[] b = new byte[bb.remaining()];
            bb.get(b);
            return b;
        }

        private static String bytesToString(byte[] bytes) {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("String encoding error", e);
            }
        }
    }

    private static FirebaseRsocketMessageFormatException
    requestMissingHandlerAdapter(String request) {
        return new FirebaseRsocketMessageFormatException(
                "No handler adapter for request: " + request);
    }

    public static class SocketContext {
        private Authenticator authenticator;
        private final Gson gson;

        public SocketContext(Authenticator authenticator, Gson gson) {
            this.authenticator = authenticator;
            this.gson = gson;
        }

        public Authenticator authenticator() {
            return authenticator;
        }

        public Gson gson() {
            return gson;
        }
    }

}
