package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.UserNotFoundException;
import in.utkarshsingh.money.manager.repository.ProfileRepository;
import in.utkarshsingh.money.manager.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserResolverService {
    private final ProfileRepository profileRepository;

    public ProfileEntity getCurrentProfile() {
        String email = UserContext.getCurrentEmail();
        if (email == null) {
            throw new UserNotFoundException("unknown");
        }
        return profileRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
