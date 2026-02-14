package in.utkarshsingh.money.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.utkarshsingh.money.manager.dto.AuthDTO;
import in.utkarshsingh.money.manager.dto.JwtResponseDTO;
import in.utkarshsingh.money.manager.dto.ProfileDTO;
import in.utkarshsingh.money.manager.dto.UserPublicDTO;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.exceptions.*;
import in.utkarshsingh.money.manager.domain.OutboxEventFactory;
import in.utkarshsingh.money.manager.port.OutboxEventStore;
import in.utkarshsingh.money.manager.repository.ProfileRepository;
import in.utkarshsingh.money.manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OutboxEventStore outboxEventStore;
    private final OutboxEventFactory outboxEventFactory;
    private final ObjectMapper objectMapper;


//    @Transactional
//    public ProfileDTO registerProfile(ProfileDTO profileDTO) {
//
//        log.info("Attempting to register profile with email: {}", profileDTO.getEmail());
//
//        try {
//            // 1️⃣ Check if email already exists
//            if (profileRepository.existsByEmail(profileDTO.getEmail())) {
//                log.warn("Registration failed. Email already exists: {}", profileDTO.getEmail());
//                throw new EmailAlreadyExistsException(profileDTO.getEmail());
//            }
//
//            // 2️⃣ Convert DTO → Entity
//            ProfileEntity newProfile = toEntity(profileDTO);
//
//            // 3️⃣ Encrypt password
//            newProfile.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
//
//            // 4️⃣ Generate activation token
//            newProfile.setActivationToken(UUID.randomUUID().toString());
//            newProfile.setActivationExpiry(LocalDateTime.now().plusHours(24));
//            newProfile.setIsActive(false);
//
//            // 5️⃣ Save to DB
//            newProfile = profileRepository.save(newProfile);
//
//            log.info("Profile registered successfully with id: {}", newProfile.getId());
//
//            // 6️⃣ Publish activation event to RabbitMQ (async email sending)
//            publishActivationEvent(newProfile);
//
//            return toDTO(newProfile);
//
//        } catch (EmailAlreadyExistsException | ProfileRegistrationException ex) {
//            throw ex;
//        } catch (DataIntegrityViolationException ex) {
//            log.error("Database constraint violation while registering email: {}", profileDTO.getEmail(), ex);
//            throw new ProfileRegistrationException("Email already registered. Please use a different email or try logging in.", ex);
//        } catch (Exception ex) {
//            log.error("Unexpected error occurred during profile registration", ex);
//            throw new ProfileRegistrationException("Something went wrong during registration. Please try again later.", ex);
//        }
//    }

    @Transactional
    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        log.info("Attempting to register profile with email: {}", profileDTO.getEmail());

        if (profileRepository.existsByEmail(profileDTO.getEmail())) {
            throw new EmailAlreadyExistsException(profileDTO.getEmail());
        }

        ProfileEntity profile = toEntity(profileDTO);

        profile.setPassword(passwordEncoder.encode(profileDTO.getPassword()));
        profile.setActivationToken(UUID.randomUUID().toString());
        profile.setActivationExpiry(LocalDateTime.now().plusHours(24));
        profile.setIsActive(false);

        profile = profileRepository.save(profile);

        createOutboxEvent(profile);

        log.info("Profile registered successfully with id: {}", profile.getId());

        return toDTO(profile);
    }

    private void createOutboxEvent(ProfileEntity profile) {
        ProfileActivationEvent event = ProfileActivationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .activationToken(profile.getActivationToken())
                .build();
        String payloadJson = toJson(event);
        outboxEventStore.save(outboxEventFactory.createProfileActivationOutboxEvent(event, payloadJson));
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }



    public ProfileEntity toEntity(ProfileDTO profileDTO) {
        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createdAt(profileDTO.getCreatedAt())
                .updatedAt(profileDTO.getUpdatedAt())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity) {
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }

    public boolean activateProfile(String activationToken) throws Exception {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile -> {
                    profile.setIsActive(true);
                    profile.setActivationToken(null);
                    profile.setActivationExpiry(null);
                    profileRepository.save(profile);
                    return true;
                })
                .orElseThrow(() -> new BadRequestException("Invalid activation token"));
    }

//    public boolean isAccountActive(String email) {
//        return profileRepository.findByEmail(email)
//                .map(ProfileEntity::getIsActive)
//                .orElse(false);
//    }

    @Transactional(readOnly = true)
    public JwtResponseDTO login(AuthDTO authDTO) {

        String email = authDTO.getEmail();
        log.info("Login attempt for email: {}", email);

        //Check if user exist
        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed - email not registered: {}", email);
                    return new UserNotFoundException(email);
                });


        //Check if he has activated his account
        if (!Boolean.TRUE.equals(profile.getIsActive())) {
            log.warn("Login failed - account not active: {}", email);
            throw new AccountNotActiveException(email);
        }

        // Step 1: Authenticate using Spring Security
        authenticateUser(email, authDTO.getPassword());

        // Step 2: Load profile after successful authentication
//        ProfileEntity profile = profileRepository.findByEmail(email)
//                .orElseThrow(InvalidCredentialsException::new);

        log.info("Login successful for email: {}", email);

        // Step 3: Generate JWT response
        return generateJwtResponse(profile);
    }

    /**
     * Handles Spring Security authentication and maps exceptions
     */
    private void authenticateUser(String email, String password) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

        } catch (BadCredentialsException ex) {

            log.warn("Authentication failed - invalid credentials for: {}", email);
            throw new InvalidCredentialsException();

        } catch (DisabledException ex) {

            log.warn("Authentication failed - account disabled for: {}", email);
            throw new AccountNotActiveException(email);

        } catch (LockedException ex) {

            log.warn("Authentication failed - account locked for: {}", email);
            throw new AccountLockedException();

        } catch (AuthenticationException ex) {

            log.warn("Authentication failed for: {}", email, ex);
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Generates JWT and response DTO
     */
    private JwtResponseDTO generateJwtResponse(ProfileEntity profile) {

        String accessToken = jwtUtil.generateToken(profile.getEmail());

        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(
                        UserPublicDTO.builder()
                                .id(profile.getId())
                                .fullName(profile.getFullName())
                                .email(profile.getEmail())
                                .profileImageUrl(profile.getProfileImageUrl())
                                .build()
                )
                .build();
    }



    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity currentUser = null;
        if (email == null) {
            currentUser = getCurrentProfile();
        }else {
            currentUser = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + email));
        }

        return ProfileDTO.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createdAt(currentUser.getCreatedAt())
                .updatedAt(currentUser.getUpdatedAt())
                .build();
    }

//    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
//        try {
//            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(), authDTO.getPassword()));
//            //Generate JWT token
//            String token = jwtUtil.generateToken(authDTO.getEmail());
//            return Map.of(
//                    "token", token,
//                    "user", getPublicProfile(authDTO.getEmail())
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Invalid email or password");
//        }
//    }



}
