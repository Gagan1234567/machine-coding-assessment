package machineCodingAssesment.controller;

import lombok.RequiredArgsConstructor;
import machineCodingAssesment.dto.response.ApiResponse;
import machineCodingAssesment.dto.response.ItemResponse;
import machineCodingAssesment.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(itemService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(itemService.getById(id)));
    }
}
