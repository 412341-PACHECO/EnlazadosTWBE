package com.example.EnlazadosTW.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio base para estructurar y despachar notificaciones push mediante FCM.
 */
@Service
public class FcmNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(FcmNotificationService.class);

	private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

	public FcmNotificationService(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
		this.firebaseMessagingProvider = firebaseMessagingProvider;
	}

	@Async("notificationsTaskExecutor")
	public void sendToToken(String token, String title, String body, Map<String, String> data) {
		FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
		if (firebaseMessaging == null) {
			logger.warn("Firebase Messaging no esta configurado. Se omite el envio de push");
			return;
		}

		try {
			Message.Builder builder = Message.builder()
				.setToken(token)
				.setNotification(Notification.builder()
					.setTitle(title)
					.setBody(body)
					.build());

			if (data != null && !data.isEmpty()) {
				builder.putAllData(data);
			}

			String messageId = firebaseMessaging.send(builder.build());
			logger.info("Notificacion FCM enviada correctamente. messageId={}", messageId);
		} catch (FirebaseMessagingException ex) {
			logger.error("No se pudo enviar la notificacion FCM al token indicado", ex);
		}
	}

	@Async("notificationsTaskExecutor")
	public void sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
		if (tokens == null || tokens.isEmpty()) {
			logger.debug("No hay tokens FCM para enviar la notificacion");
			return;
		}

		FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
		if (firebaseMessaging == null) {
			logger.warn("Firebase Messaging no esta configurado. Se omite el envio de push");
			return;
		}

		try {
			MulticastMessage.Builder builder = MulticastMessage.builder()
				.addAllTokens(tokens)
				.setNotification(Notification.builder()
					.setTitle(title)
					.setBody(body)
					.build());

			if (data != null && !data.isEmpty()) {
				builder.putAllData(data);
			}

			var response = firebaseMessaging.sendEachForMulticast(builder.build());
			logger.info(
				"Envio FCM completado. successCount={}, failureCount={}",
				response.getSuccessCount(),
				response.getFailureCount()
			);
		} catch (FirebaseMessagingException ex) {
			logger.error("No se pudo enviar la notificacion FCM a multiples tokens", ex);
		}
	}
}
