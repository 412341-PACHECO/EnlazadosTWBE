package com.example.EnlazadosTW.services;

import com.example.EnlazadosTW.entities.AttendanceBilling;
import com.example.EnlazadosTW.entities.AttendanceRecord;
import com.example.EnlazadosTW.entities.Patient;
import com.example.EnlazadosTW.entities.ProfessionalProfile;
import com.example.EnlazadosTW.repositories.AttendanceBillingRepository;
import com.example.EnlazadosTW.repositories.AttendanceRecordRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para generar reportes PDF de liquidaciones administrativas.
 */
@Service
@Transactional(readOnly = true)
public class PdfBillingReportService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final AttendanceBillingRepository attendanceBillingRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;

	public PdfBillingReportService(
		AttendanceBillingRepository attendanceBillingRepository,
		AttendanceRecordRepository attendanceRecordRepository
	) {
		this.attendanceBillingRepository = attendanceBillingRepository;
		this.attendanceRecordRepository = attendanceRecordRepository;
	}

	public byte[] generateAttendanceBillingPdf(UUID billingId) {
		AttendanceBilling billing = attendanceBillingRepository.findById(billingId)
			.orElseThrow(() -> new IllegalArgumentException("Liquidacion de asistencias no encontrada con ID: " + billingId));

		List<AttendanceRecord> attendanceRecords = attendanceRecordRepository.findByAttendanceBillingIdOrderBySessionDateAsc(billingId);
		if (attendanceRecords.isEmpty()) {
			throw new IllegalArgumentException("La liquidacion no tiene asistencias asociadas para generar el PDF");
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Document document = new Document(PageSize.A4, 36, 36, 54, 54);

		try {
			PdfWriter.getInstance(document, outputStream);
			document.open();

			addHeader(document);
			addProfessionalSection(document, billing.getProfessionalProfile());
			addPatientSection(document, billing.getPatient(), billing.getHealthInsuranceName());
			addBillingSummarySection(document, billing);
			addAttendanceTable(document, attendanceRecords);
			addFooter(document, billing);

		} catch (DocumentException ex) {
			throw new IllegalStateException("No se pudo generar el documento PDF de liquidacion", ex);
		} finally {
			document.close();
		}

		return outputStream.toByteArray();
	}

	private void addHeader(Document document) throws DocumentException {
		Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
		Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

		Paragraph title = new Paragraph("EnlazadosTW - Planilla de Facturacion", titleFont);
		title.setAlignment(Element.ALIGN_CENTER);
		document.add(title);

		Paragraph subtitle = new Paragraph(
			"Documento consolidado de asistencias para presentacion administrativa",
			subtitleFont
		);
		subtitle.setSpacingAfter(20);
		subtitle.setAlignment(Element.ALIGN_CENTER);
		document.add(subtitle);
	}

	private void addProfessionalSection(Document document, ProfessionalProfile professionalProfile) throws DocumentException {
		document.add(buildSectionTitle("Datos del Profesional"));

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setSpacingAfter(14);
		table.setWidths(new float[] { 2f, 5f });

		addLabelValueRow(table, "Nombre", buildFullName(
			professionalProfile.getUser().getFirstName(),
			professionalProfile.getUser().getLastName()
		));
		addLabelValueRow(table, "Email", professionalProfile.getUser().getEmail());
		addLabelValueRow(table, "Especialidad", professionalProfile.getSpecialty());
		addLabelValueRow(table, "Matricula", professionalProfile.getLicenseNumber());
		addLabelValueRow(table, "Honorario nominal", "$ " + professionalProfile.getSessionFee());

		document.add(table);
	}

	private void addPatientSection(Document document, Patient patient, String healthInsuranceName) throws DocumentException {
		document.add(buildSectionTitle("Datos del Paciente"));

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setSpacingAfter(14);
		table.setWidths(new float[] { 2f, 5f });

		addLabelValueRow(table, "Paciente", patient != null
			? buildFullName(patient.getFirstName(), patient.getLastName())
			: "Liquidacion multipaciente");
		addLabelValueRow(table, "Obra social", healthInsuranceName != null ? healthInsuranceName : "No especificada");

		document.add(table);
	}

	private void addBillingSummarySection(Document document, AttendanceBilling billing) throws DocumentException {
		document.add(buildSectionTitle("Resumen de Liquidacion"));

		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		table.setSpacingAfter(16);
		table.setWidths(new float[] { 2f, 5f });

		addLabelValueRow(table, "Periodo", billing.getBillingPeriod());
		addLabelValueRow(table, "Sesiones", String.valueOf(billing.getTotalSessions()));
		addLabelValueRow(table, "Monto total", "$ " + billing.getTotalAmount());
		addLabelValueRow(table, "Estado", billing.getPaymentStatus().name());
		addLabelValueRow(table, "Generado", DATE_TIME_FORMATTER.format(LocalDateTime.now()));

		document.add(table);
	}

	private void addAttendanceTable(Document document, List<AttendanceRecord> attendanceRecords) throws DocumentException {
		document.add(buildSectionTitle("Listado de Sesiones"));

		PdfPTable table = new PdfPTable(6);
		table.setWidthPercentage(100);
		table.setSpacingAfter(18);
		table.setWidths(new float[] { 1.5f, 2.6f, 2.4f, 1.8f, 1.6f, 1.6f });

		addTableHeader(table, "Fecha");
		addTableHeader(table, "Paciente");
		addTableHeader(table, "Obra social");
		addTableHeader(table, "Honorario");
		addTableHeader(table, "Cobertura");
		addTableHeader(table, "Copago");

		for (AttendanceRecord attendanceRecord : attendanceRecords) {
			addTableCell(table, DATE_FORMATTER.format(attendanceRecord.getSessionDate()));
			addTableCell(table, buildFullName(
				attendanceRecord.getPatient().getFirstName(),
				attendanceRecord.getPatient().getLastName()
			));
			addTableCell(table, attendanceRecord.getHealthInsuranceName());
			addTableCell(table, "$ " + attendanceRecord.getSessionFeeSnapshot());
			addTableCell(table, formatMoney(attendanceRecord.getHealthInsuranceCoverageAmount()));
			addTableCell(table, formatMoney(attendanceRecord.getCopaymentAmount()));
		}

		document.add(table);
	}

	private void addFooter(Document document, AttendanceBilling billing) throws DocumentException {
		Paragraph certification = new Paragraph(
			"Certificacion digital: " + billing.getDigitalHash(),
			FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)
		);
		certification.setAlignment(Element.ALIGN_RIGHT);
		certification.setSpacingBefore(8);
		document.add(certification);
	}

	private Paragraph buildSectionTitle(String title) {
		Paragraph sectionTitle = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
		sectionTitle.setSpacingBefore(8);
		sectionTitle.setSpacingAfter(8);
		return sectionTitle;
	}

	private void addLabelValueRow(PdfPTable table, String label, String value) {
		PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
		labelCell.setBorder(Rectangle.BOX);
		labelCell.setPadding(6);

		PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 10)));
		valueCell.setBorder(Rectangle.BOX);
		valueCell.setPadding(6);

		table.addCell(labelCell);
		table.addCell(valueCell);
	}

	private void addTableHeader(PdfPTable table, String value) {
		PdfPCell headerCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerCell.setPadding(6);
		table.addCell(headerCell);
	}

	private void addTableCell(PdfPTable table, String value) {
		PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "-", FontFactory.getFont(FontFactory.HELVETICA, 9)));
		cell.setPadding(5);
		table.addCell(cell);
	}

	private String buildFullName(String firstName, String lastName) {
		String safeFirstName = firstName != null ? firstName.trim() : "";
		String safeLastName = lastName != null ? lastName.trim() : "";
		String fullName = (safeFirstName + " " + safeLastName).trim();
		return fullName.isBlank() ? "-" : fullName;
	}

	private String formatMoney(BigDecimal amount) {
		return amount != null ? "$ " + amount : "-";
	}
}
