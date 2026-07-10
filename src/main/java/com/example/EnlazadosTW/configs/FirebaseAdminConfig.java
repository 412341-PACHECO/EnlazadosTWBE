package com.example.EnlazadosTW.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuracion base del SDK de Firebase Admin.
 */
@Configuration
@EnableConfigurationProperties(FirebaseProperties.class)
@ConditionalOnProperty(prefix = "app.firebase", name = "enabled", havingValue = "true")
public class FirebaseAdminConfig {

	private static final Logger logger = LoggerFactory.getLogger(FirebaseAdminConfig.class);

	@Bean
	public FirebaseApp firebaseApp(FirebaseProperties firebaseProperties) throws IOException {
		if (!FirebaseApp.getApps().isEmpty()) {
			return FirebaseApp.getInstance();
		}

		GoogleCredentials credentials = buildCredentials(firebaseProperties);

		FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
			.setCredentials(credentials);

		if (StringUtils.hasText(firebaseProperties.getProjectId())) {
			optionsBuilder.setProjectId(firebaseProperties.getProjectId());
		}

		FirebaseApp firebaseApp = FirebaseApp.initializeApp(optionsBuilder.build());
		logger.info("Firebase Admin SDK inicializado correctamente");
		return firebaseApp;
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
		return FirebaseMessaging.getInstance(firebaseApp);
	}

	private GoogleCredentials buildCredentials(FirebaseProperties firebaseProperties) throws IOException {
		if (StringUtils.hasText(firebaseProperties.getCredentialsPath())) {
			try (InputStream inputStream = new FileInputStream(firebaseProperties.getCredentialsPath())) {
				return GoogleCredentials.fromStream(inputStream);
			}
		}

		return GoogleCredentials.getApplicationDefault();
	}
}
