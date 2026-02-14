package in.utkarshsingh.money.manager.email.domain;

import in.utkarshsingh.money.manager.email.event.ProfileActivationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SRP: Single responsibility — build activation email content only.
 * OCP: New email types can be added via new builder classes without changing this one.
 */
@Component
public class ActivationEmailContentBuilder {

    @Value("${app.activation.url}")
    private String activationUrl;

    private static final String SUBJECT = "Activate your Money Manager account";

    public EmailMessage build(ProfileActivationEvent event) {
        String activationLink = activationUrl + "/api/v1.0/activate?token=" + event.getActivationToken();
        String body = "Hi " + event.getFullName() + ",\n\n"
                + "Click the link below to activate your account:\n"
                + activationLink + "\n\n"
                + "If you did not register, please ignore this email.";
        return EmailMessage.builder()
                .recipient(event.getEmail())
                .subject(SUBJECT)
                .body(body)
                .build();
    }
}
