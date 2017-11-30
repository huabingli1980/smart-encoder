// 
// Decompiled by Procyon v0.5.30
// 

package com.sqlite.config;

import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@CrossOrigin({ "*" })
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer
{
    public void configureMessageBroker(final MessageBrokerRegistry config) {
        config.enableSimpleBroker(new String[] { "/topic" });
        config.setApplicationDestinationPrefixes(new String[] { "/app" });
    }
    
    public void registerStompEndpoints(final StompEndpointRegistry registry) {
        registry.addEndpoint(new String[] { "/gs-guide-websocket" }).setAllowedOrigins(new String[] { "*" }).withSockJS();
    }
}
