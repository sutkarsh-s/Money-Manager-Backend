package in.utkarshsingh.money.manager.dto;
import lombok.*;
import java.time.LocalDateTime;
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class ContactMessageDTO {
    private Long id; private String name; private String email; private String subject; private String message; private String status; private LocalDateTime createdAt;
}
