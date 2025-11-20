package com.playlist.backend.api;

import com.playlist.backend.entity.User;
import com.playlist.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // 생성자 주입
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 전체 조회
    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // 유저 생성
    @PostMapping
    public User createUser(@RequestBody UserCreateRequest request) {
        User user = new User(request.getName(), request.getEmail());
        return userRepository.save(user);
    }

    // DTO 클래스 내부에 간단히 정의
    public static class UserCreateRequest {
        private String name;
        private String email;

        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
