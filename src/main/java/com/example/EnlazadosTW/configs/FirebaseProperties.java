package com.example.EnlazadosTW.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades para la integracion con Firebase Admin SDK.
 */
@ConfigurationProperties(prefix = "app.firebase")
public class FirebaseProperties {

	private boolean enabled;
	private String projectId;
	private String credentialsPath;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getCredentialsPath() {
		return credentialsPath;
	}

	public void setCredentialsPath(String credentialsPath) {
		this.credentialsPath = credentialsPath;
	}
}
