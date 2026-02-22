package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.CategoryDTO;
import in.utkarshsingh.money.manager.dto.request.CategoryRequest;
import in.utkarshsingh.money.manager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO> saveCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDTO savedCategory = categoryService.saveCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<CategoryDTO> categories = categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByTypeForCurrentUser(@PathVariable String type) {
        List<CategoryDTO> list = categoryService.getCategoriesByTypeForCurrentUser(type);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,
                                                      @Valid @RequestBody CategoryRequest request) {
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(updatedCategory);
    }
}
