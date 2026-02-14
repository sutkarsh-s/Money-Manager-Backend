package in.utkarshsingh.money.manager.email.port;

import in.utkarshsingh.money.manager.email.domain.EmailMessage;

/**
 * Port for sending an email (DIP + ISP).
 * Implementations: SMTP, SendGrid, etc. Can extend without changing callers.
 */
public interface EmailSender {
    void send(EmailMessage message);
}
