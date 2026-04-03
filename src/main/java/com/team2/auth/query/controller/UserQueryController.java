package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserListResponse;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService userQueryService;

    @GetMapping
    public ResponseEntity<PagedResponse<UserListResponse>> getUsers(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) String userStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userQueryService.getUsers(
                userName, departmentId, userRole, userStatus, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<User>> getUser(@PathVariable Integer id) {
        User user = userQueryService.getUser(id);
        return ResponseEntity.ok(EntityModel.of(user,
                linkTo(methodOn(UserQueryController.class).getUser(id)).withSelfRel(),
                linkTo(methodOn(UserQueryController.class).getUsers(null, null, null, null, 0, 10)).withRel("users")));
    }
}
