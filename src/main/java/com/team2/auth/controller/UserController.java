package com.team2.auth.controller;

import com.team2.auth.dto.ChangeStatusRequest;
import com.team2.auth.dto.CreateUserRequest;
import com.team2.auth.dto.UpdateUserRequest;
import com.team2.auth.entity.User;
import com.team2.auth.service.UserCommandService;
import com.team2.auth.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userCommandService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userQueryService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userQueryService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Integer id,
                                           @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userCommandService.updateUser(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<User> changeStatus(@PathVariable Integer id,
                                             @RequestBody ChangeStatusRequest request) {
        return ResponseEntity.ok(userCommandService.changeStatus(id, request.getStatus()));
    }
}
