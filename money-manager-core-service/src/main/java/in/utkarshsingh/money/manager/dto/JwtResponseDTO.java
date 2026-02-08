package in.utkarshsingh.money.manager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponseDTO {

    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserPublicDTO user;
}
