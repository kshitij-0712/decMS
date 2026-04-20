package com.decms.admin.service;

import com.decms.admin.dto.AuditReportResponse;

/**
 * ReportFormatter — Interface (DIP principle, Khizer)
 *
 * HIGH-LEVEL module ReportService depends on THIS interface.
 * LOW-LEVEL modules HtmlReportFormatter and PdfReportFormatter
 * implement this interface.
 *
 * ReportService never imports the concrete classes directly.
 * Spring injects the correct one at runtime.
 */
public interface ReportFormatter {

    /**
     * Formats the compiled audit report into a string.
     * HTML formatter returns HTML markup.
     * PDF formatter returns Base64-encoded PDF bytes.
     */
    String format(AuditReportResponse report);

    /** Returns "HTML" or "PDF" */
    String getFormatName();

    /** Returns content type for HTTP response */
    String getContentType();
}
