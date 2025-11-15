package uz.javachi.autonline.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.javachi.autonline.dto.request.MessageRequestDTO;
import uz.javachi.autonline.dto.request.NewsRequestDTO;
import uz.javachi.autonline.dto.request.UpdateUserRequestDTO;
import uz.javachi.autonline.dto.response.NewsResponse;
import uz.javachi.autonline.dto.response.UserResponseDTO;
import uz.javachi.autonline.service.AdminService;
import uz.javachi.autonline.service.NewsService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final NewsService newsService;

    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }


    @PostMapping("/edit-user-subscription")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editUserSubscription(
            @RequestParam Integer userId,
            @RequestParam Integer subscriptionId
    ) {
        return ResponseEntity.ok(adminService.editUserSubscription(userId, subscriptionId));
    }

    @PostMapping("/add-role-to-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addRoleToUser(
            @RequestParam Integer userId,
            @RequestParam Integer roleId
    ) {
        return ResponseEntity.ok(adminService.addRoleToUser(userId, roleId));
    }

    @PostMapping("/delete-role-from-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoleFromUser(
            @RequestParam Integer userId,
            @RequestParam Integer roleId
    ) {
        return ResponseEntity.ok(adminService.deleteRoleFromUser(userId, roleId));
    }

    @GetMapping("/get-all-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @GetMapping("/get-all-subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllSubscriptions() {
        return ResponseEntity.ok(adminService.getAllSubscriptions());
    }

    @PatchMapping("/particle-update-user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserPartially(
            @PathVariable("id") Integer id,
            @RequestBody UpdateUserRequestDTO dto) {
        return ResponseEntity.ok(adminService.partialUpdateUser(id, dto));
    }

    @PostMapping("/block/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.blockUser(userId));
    }

    @PostMapping("/unblock/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unblockUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.unblockUser(userId));
    }


    @PostMapping("/create-news")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNews(@RequestBody NewsRequestDTO dto) {
        return ResponseEntity.ok(adminService.createNews(dto));
    }

    @GetMapping("/news")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NewsResponse>> getAllActiveNews() {
        return newsService.getAllActiveNews(true);
    }

    @DeleteMapping("/delete-news/{newsId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNews(@PathVariable("newsId") Integer newsId) {
        return ResponseEntity.ok(newsService.deleteNews(newsId));
    }

}
