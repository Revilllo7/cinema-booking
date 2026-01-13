package com.cinema.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host:localhost}")
    private String relayHost;

    @Value("${app.messaging.stomp-port:61613}")
    private int relayPort;

    @Value("${spring.rabbitmq.username:guest}")
    private String brokerUsername;

    @Value("${spring.rabbitmq.password:guest}")
    private String brokerPassword;

    @Value("${app.messaging.use-broker-relay:false}")
    private boolean useBrokerRelay;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        if (useBrokerRelay) {
            registry.enableStompBrokerRelay("/topic")
                .setRelayHost(relayHost)
                .setRelayPort(relayPort)
                .setClientLogin(brokerUsername)
                .setClientPasscode(brokerPassword)
                .setSystemLogin(brokerUsername)
                .setSystemPasscode(brokerPassword);
        } else {
            registry.enableSimpleBroker("/topic");
        }
    }
}
