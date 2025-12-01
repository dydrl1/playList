package com.playlist.backend.user;

import com.playlist.backend.security.CustomUserDetails;
import com.playlist.backend.user.dto.UserProfileResponse;
import com.playlist.backend.user.dto.UserProfileUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // 생성자 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 🔹 내 프로필 조회
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var user = userService.getUserById(userDetails.getId());

        return ResponseEntity.ok(
                UserProfileResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .build()
        );
    }

    /**
     *  내 프로필 수정
     * PATCH /users/me
     */
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserProfileUpdateRequest request
    ) {
        var updated = userService.updateProfile(userDetails.getId(), request.getName());

        return ResponseEntity.ok(
                UserProfileResponse.builder()
                        .id(updated.getId())
                        .name(updated.getName())
                        .email(updated.getEmail())
                        .build()
        );
    }

    /**
     *  회원 탈퇴
     * DELETE /users/me
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.noContent().build(); // 204
    }
}
