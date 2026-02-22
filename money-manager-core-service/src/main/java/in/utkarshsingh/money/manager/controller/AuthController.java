package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.JwtResponseDTO;
import in.utkarshsingh.money.manager.dto.ProfileDTO;
import in.utkarshsingh.money.manager.dto.request.LoginRequest;
import in.utkarshsingh.money.manager.dto.request.RegisterRequest;
import in.utkarshsingh.money.manager.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@Valid @RequestBody RegisterRequest request) {
        ProfileDTO registeredProfile = profileService.registerProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        profileService.activateProfile(token);
        return ResponseEntity.ok("Profile activated successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        JwtResponseDTO response = profileService.login(request);
        return ResponseEntity.ok(response);
    }
}
