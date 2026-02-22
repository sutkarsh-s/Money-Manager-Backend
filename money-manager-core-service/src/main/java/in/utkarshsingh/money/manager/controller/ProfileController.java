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
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile() {
        ProfileDTO profileDTO = profileService.getPublicProfile(null);
        return ResponseEntity.ok(profileDTO);
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        ProfileDTO updatedProfile = profileService.updateProfile(request);
        return ResponseEntity.ok(updatedProfile);
    }

    @PutMapping("/password")
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
