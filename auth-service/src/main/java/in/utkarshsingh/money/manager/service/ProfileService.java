package in.utkarshsingh.money.manager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.utkarshsingh.money.manager.dto.JwtResponseDTO;
import in.utkarshsingh.money.manager.dto.ProfileDTO;
import in.utkarshsingh.money.manager.dto.request.ChangePasswordRequest;
import in.utkarshsingh.money.manager.dto.request.LoginRequest;
import in.utkarshsingh.money.manager.dto.request.RegisterRequest;
import in.utkarshsingh.money.manager.dto.request.UpdateProfileRequest;
import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.exceptions.*;
import in.utkarshsingh.money.manager.mapper.ProfileMapper;
import in.utkarshsingh.money.manager.repository.OutboxRepository;
import in.utkarshsingh.money.manager.repository.ProfileRepository;
import in.utkarshsingh.money.manager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final ProfileMapper profileMapper;

    @Transactional
    public ProfileDTO registerProfile(RegisterRequest request) {
        log.info("Attempting to register profile with email: {}", request.getEmail());
        if (profileRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        ProfileEntity profile = profileMapper.toEntity(request);
        profile.setPassword(passwordEncoder.encode(request.getPassword()));
        profile.setActivationToken(UUID.randomUUID().toString());
        profile.setActivationExpiry(LocalDateTime.now().plusHours(24));
        profile.setIsActive(false);
        profile = profileRepository.save(profile);
        createOutboxEvent(profile);
        log.info("Profile registered successfully with id: {}", profile.getId());
        return profileMapper.toDTO(profile);
    }

    @Transactional
    public boolean activateProfile(String activationToken) {
        ProfileEntity profile = profileRepository.findByActivationToken(activationToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired activation token"));
        if (profile.getActivationExpiry() != null && profile.getActivationExpiry().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Activation token has expired");
        }
        profile.setIsActive(true);
        profile.setActivationToken(null);
        profile.setActivationExpiry(null);
        profileRepository.save(profile);
        return true;
    }

    @Transactional(readOnly = true)
    public JwtResponseDTO login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Login attempt for email: {}", email);
        ProfileEntity profile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        if (!Boolean.TRUE.equals(profile.getIsActive())) {
            throw new AccountNotActiveException(email);
        }
        authenticateUser(email, request.getPassword());
        log.info("Login successful for email: {}", email);
        return generateJwtResponse(profile);
    }

    public ProfileEntity getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return profileRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found with email: " + authentication.getName()));
    }

    @Transactional(readOnly = true)
    public ProfileDTO getPublicProfile(String email) {
        ProfileEntity profile;
        if (email == null) {
            profile = getCurrentProfile();
        } else {
            profile = profileRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException(email));
        }
        return profileMapper.toDTO(profile);
    }

    @Transactional
    public ProfileDTO updateProfile(UpdateProfileRequest request) {
        ProfileEntity profile = getCurrentProfile();
        profile.setFullName(request.getFullName().trim());
        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }
        profile = profileRepository.save(profile);
        return profileMapper.toDTO(profile);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        ProfileEntity profile = getCurrentProfile();
        if (!passwordEncoder.matches(request.getCurrentPassword(), profile.getPassword())) {
            throw new InvalidPasswordException();
        }
        profile.setPassword(passwordEncoder.encode(request.getNewPassword()));
        profileRepository.save(profile);
    }

    @Transactional
    public void deleteAccount() {
        ProfileEntity profile = getCurrentProfile();
        profileRepository.delete(profile);
        log.info("Account deleted for profileId: {}", profile.getId());
    }

    private void authenticateUser(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        } catch (DisabledException ex) {
            throw new AccountNotActiveException(email);
        } catch (LockedException ex) {
            throw new AccountLockedException();
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }

    private JwtResponseDTO generateJwtResponse(ProfileEntity profile) {
        String accessToken = jwtUtil.generateToken(profile.getEmail());
        return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(profileMapper.toPublicDTO(profile))
                .build();
    }

    private void createOutboxEvent(ProfileEntity profile) {
        ProfileActivationEvent event = ProfileActivationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .email(profile.getEmail())
                .fullName(profile.getFullName())
                .activationToken(profile.getActivationToken())
                .build();
        String payloadJson = toJson(event);
        OutboxEvent outbox = OutboxEvent.builder()
                .eventId(event.getEventId())
                .aggregateType("PROFILE")
                .eventType("PROFILE_ACTIVATION")
                .payload(payloadJson)
                .status(EventStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        outboxRepository.save(outbox);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize activation event", e);
            throw new ProfileRegistrationException("Failed to process registration.");
        }
    }
}
