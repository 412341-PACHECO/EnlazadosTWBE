package com.example.EnlazadosTW.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler que dispara la generación automática del resumen semanal.
 */
@Component
public class WeeklySummaryScheduler {

	private final WeeklySummaryService weeklySummaryService;
	private final boolean weeklySummaryEnabled;

	public WeeklySummaryScheduler(
		WeeklySummaryService weeklySummaryService,
		@Value("${app.weekly-summary.enabled:true}") boolean weeklySummaryEnabled
	) {
		this.weeklySummaryService = weeklySummaryService;
		this.weeklySummaryEnabled = weeklySummaryEnabled;
	}

	@Scheduled(
		cron = "${app.weekly-summary.cron:0 0 18 ? * FRI}",
		zone = "${app.weekly-summary.zone:America/Argentina/Buenos_Aires}"
	)
	public void generateWeeklySummaries() {
		if (!weeklySummaryEnabled) {
			return;
		}

		weeklySummaryService.generateCurrentWeekSummaries();
	}
}
