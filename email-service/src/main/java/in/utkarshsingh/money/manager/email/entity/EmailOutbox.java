package in.utkarshsingh.money.manager.email.entity;

import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_outbox")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String recipient;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
