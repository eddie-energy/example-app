package energy.eddie.exampleapp.controller;

import energy.eddie.exampleapp.model.AllPermissionsItem;
import energy.eddie.exampleapp.model.db.Permission;
import energy.eddie.exampleapp.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RequestMapping("/permissions")
@Tag(name = "PermissionApi", description = "Operations related to permissions")
@RestController
public class PermissionController {
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<AllPermissionsItem>> getAllPermissionsForUser() {
        return ResponseEntity.ok(permissionService.getAllPermissionIdAndNameForUser());
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable("permissionId") Long permissionId) {
        var permission = permissionService.getPermissionById(permissionId);
        return permission
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
