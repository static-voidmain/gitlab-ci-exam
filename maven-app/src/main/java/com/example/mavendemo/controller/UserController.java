package com.example.mavendemo.controller;

import com.example.mavendemo.model.UserSummary;
import com.example.mavendemo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummary> getUser(@PathVariable("id") String id) {
        UserSummary summary = userService.getUserSummary(id);
        return ResponseEntity.ok(summary);
    }
}
