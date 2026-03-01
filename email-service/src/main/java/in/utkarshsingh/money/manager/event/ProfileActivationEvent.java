package in.utkarshsingh.money.manager.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileActivationEvent {
    private String email;
    private String fullName;
    private String activationToken;
}
