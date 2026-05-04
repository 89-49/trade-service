package org.pgsg.trade.global.config;

import org.pgsg.config.security.SecurityConfigImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SecurityConfigImpl.class)
public class SecurityImportConfig {
}
