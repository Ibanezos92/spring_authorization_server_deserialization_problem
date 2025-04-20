package com.example

import com.example.mixinfix.DurationDeserializer
import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import groovy.util.logging.Slf4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer

import java.time.Duration

@Configuration
@Slf4j
class AuthServerConfig_CustomFix {

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {

        JdbcRegisteredClientRepository r = new JdbcRegisteredClientRepository(jdbcTemplate)
        JdbcRegisteredClientRepository.RegisteredClientRowMapper rc = new JdbcRegisteredClientRepository.RegisteredClientRowMapper()

        ObjectMapper mapper = new ObjectMapper()

        ClassLoader classLoader = JdbcRegisteredClientRepository.class.getClassLoader()
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader)
        mapper.registerModules(securityModules)
        mapper.registerModule(new OAuth2AuthorizationServerJackson2Module())

        SimpleModule module = new SimpleModule()
        module.addDeserializer(Duration.class,new DurationDeserializer())
        mapper.registerModule(module)

        rc.setObjectMapper(mapper)
        r.setRegisteredClientRowMapper(rc)

        return r

    }

    @Bean
    OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository jdbcClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, jdbcClientRepository)
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, clients)
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return (context) -> {
            if ("access_token".equals(context.getTokenType().getValue())) {
                String clientId = context.getRegisteredClient().getClientId()

                context.getClaims().claim("clientCode", '182')
            }
        }
    }
}
