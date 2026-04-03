package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.ChangePasswordRequest;
import com.team2.auth.command.application.dto.ChangeStatusRequest;
import com.team2.auth.command.application.dto.CreateUserRequest;
import com.team2.auth.command.application.dto.UpdateUserRequest;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.application.service.UserCommandService;
import com.team2.auth.query.controller.UserQueryController;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<User>> createUser(@RequestBody CreateUserRequest request) {
        User user = userCommandService.createUser(request);
        EntityModel<User> model = EntityModel.of(user,
                linkTo(methodOn(UserQueryController.class).getUser(user.getUserId())).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, 0, 10)).withRel("users"));
        URI location = linkTo(methodOn(UserQueryController.class).getUser(user.getUserId())).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<User>> updateUser(@PathVariable Integer id,
                                           @RequestBody UpdateUserRequest request) {
        User user = userCommandService.updateUser(id, request);
        return ResponseEntity.ok(EntityModel.of(user,
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, 0, 10)).withRel("users")));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id,
                                               @RequestBody ChangePasswordRequest request) {
        userCommandService.changePassword(id, request.getCurrentPw(), request.getNewPw());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/password/reset")
    public ResponseEntity<Void> resetPassword(@PathVariable Integer id) {
        userCommandService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EntityModel<User>> changeStatus(@PathVariable Integer id,
                                             @RequestBody ChangeStatusRequest request) {
        User user = userCommandService.changeStatus(id, request.getStatus());
        return ResponseEntity.ok(EntityModel.of(user,
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel()));
    }
}
