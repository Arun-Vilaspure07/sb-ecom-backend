package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category category;
    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(1L);
        categoryDTO.setCategoryName("Electronics");
    }

    // ---------- getAllCategories SUCCESS ----------

    @Test
    void getAllCategories_success() {
        Page<Category> page = new PageImpl<>(List.of(category));

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(Category.class), eq(CategoryDTO.class))).thenReturn(categoryDTO);

        CategoryResponse response = categoryService.getAllCategories(0, 10, "categoryName", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Electronics", response.getContent().get(0).getCategoryName());
    }

    // ---------- getAllCategories EMPTY ----------

    @Test
    void getAllCategories_noCategories_shouldThrowAPIException() {
        Page<Category> emptyPage = new PageImpl<>(List.of());

        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        assertThrows(APIException.class, () ->
                categoryService.getAllCategories(0, 10, "categoryName", "asc")
        );
    }

    // ---------- createCategory SUCCESS ----------

    @Test
    void createCategory_success() {
        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryName("Electronics")).thenReturn(null);
        when(categoryRepository.save(category)).thenReturn(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.createCategory(categoryDTO);

        assertNotNull(result);
        assertEquals("Electronics", result.getCategoryName());
    }

    // ---------- createCategory DUPLICATE ----------

    @Test
    void createCategory_duplicate_shouldThrowAPIException() {
        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.findByCategoryName("Electronics")).thenReturn(category);

        assertThrows(APIException.class, () ->
                categoryService.createCategory(categoryDTO)
        );
    }

    // ---------- deleteCategory SUCCESS ----------

    @Test
    void deleteCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.deleteCategory(1L);

        assertNotNull(result);
        verify(categoryRepository).delete(category);
    }

    // ---------- deleteCategory NOT FOUND ----------

    @Test
    void deleteCategory_notFound_shouldThrowException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.deleteCategory(1L)
        );
    }

    // ---------- updateCategory SUCCESS ----------

    @Test
    void updateCategory_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(categoryDTO, Category.class)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        CategoryDTO result = categoryService.updateCategory(categoryDTO, 1L);

        assertNotNull(result);
        assertEquals("Electronics", result.getCategoryName());
    }

    // ---------- updateCategory NOT FOUND ----------

    @Test
    void updateCategory_notFound_shouldThrowException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                categoryService.updateCategory(categoryDTO, 1L)
        );
    }
}