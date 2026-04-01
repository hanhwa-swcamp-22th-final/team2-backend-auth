package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.ChangeStatusRequest;
import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.application.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userCommandService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userCommandService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
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
