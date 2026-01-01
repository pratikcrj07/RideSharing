//package com.ridesharing.Config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.*;
//
//@Configuration
//@EnableWebSocketMessageBroker
//public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//
//    // client connect to /ws endpnt
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/ws") // primary websocket endp
//                .setAllowedOriginPatterns("*")
//                .withSockJS();
//    }
//
//    // message broker for routing message (simple in-memory broker)
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/topic", "/queue"); // topics for broadcasts
//        registry.setApplicationDestinationPrefixes("/app"); // client sends to /app/*
//    }
//}
