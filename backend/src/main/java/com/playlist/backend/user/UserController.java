package com.playlist.backend.user;

import com.playlist.backend.common.response.ApiResponse;
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

    // 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var user = userService.getUserById(userDetails.getId());

        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 내 프로필 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserProfileUpdateRequest request
    ) {
        var updated = userService.updateProfile(userDetails.getId(), request.getName());

        UserProfileResponse response = UserProfileResponse.builder()
                .id(updated.getId())
                .name(updated.getName())
                .email(updated.getEmail())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     회원 탈퇴
    */
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteMyAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        userService.deleteUser(userDetails.getId());

        // 204(noContent)로 가면 body를 보낼 수 없어서,
        // ApiResponse 래퍼를 쓰려면 200 OK + 메시지로 반환하는 게 깔끔함.
        return ResponseEntity.ok(
                ApiResponse.success("회원 탈퇴가 완료되었습니다.")
        );
    }
}
