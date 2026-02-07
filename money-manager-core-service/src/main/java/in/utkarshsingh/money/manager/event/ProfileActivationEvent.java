package in.utkarshsingh.money.manager.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileActivationEvent {

    private String eventId;
    private String email;
    private String fullName;
    private String activationToken;
}
