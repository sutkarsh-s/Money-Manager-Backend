package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.ProfileDTO;
import in.utkarshsingh.money.manager.dto.request.ChangePasswordRequest;
import in.utkarshsingh.money.manager.dto.request.UpdateProfileRequest;
import in.utkarshsingh.money.manager.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {
        return ResponseEntity.ok(profileService.getPublicProfile(null));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount() {
        profileService.deleteAccount();
        return ResponseEntity.noContent().build();
    }
}
