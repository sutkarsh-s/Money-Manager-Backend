package in.utkarshsingh.money.manager.email.domain;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable value object for email content.
 * DIP: High-level code depends on this abstraction, not on SMTP details.
 */
@Value
@Builder
public class EmailMessage {
    String recipient;
    String subject;
    String body;
}
