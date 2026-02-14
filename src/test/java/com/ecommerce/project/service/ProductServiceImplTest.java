package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private FileService fileService;

    @Mock
    private AuthUtil authUtil;

    @Test
    void shouldAddProductSuccessfully() {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("iPhone");
        dto.setPrice(1000.0);
        dto.setDiscount(10.0);

        Category category = new Category();
        category.setProducts(List.of());

        User user = new User();
        user.setUserId(1L);

        Product product = new Product();
        Product savedProduct = new Product();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(authUtil.loggedInUser()).thenReturn(user);
        when(modelMapper.map(dto, Product.class)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(modelMapper.map(savedProduct, ProductDTO.class)).thenReturn(dto);

        ProductDTO result = productService.addProduct(1L, dto);

        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductAlreadyExists() {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("iPhone");

        Product existing = new Product();
        existing.setProductName("iPhone");

        Category category = new Category();
        category.setProducts(List.of(existing));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(APIException.class,
                () -> productService.addProduct(1L, dto));
    }

    @Test
    void shouldThrowExceptionWhenCategoryHasNoProducts() {
        Category category = new Category();
        category.setCategoryName("Electronics");

        Page<Product> emptyPage = Page.empty();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.findByCategoryOrderByPriceAsc(any(), any()))
                .thenReturn(emptyPage);

        assertThrows(APIException.class, () ->
                productService.searchByCategory(1L, 0, 5, "price", "asc"));
    }

    @Test
    void shouldThrowExceptionWhenKeywordNotFound() {
        Page<Product> emptyPage = Page.empty();

        when(productRepository.findByProductNameLikeIgnoreCase(any(), any()))
                .thenReturn(emptyPage);

        assertThrows(APIException.class, () ->
                productService.searchProductByKeyword("abc", 0, 5, "price", "asc"));
    }

    @Test
    void sellerShouldNotUpdateOthersProduct() {
        Product product = new Product();
        User owner = new User();
        owner.setUserId(1L);
        product.setUser(owner);

        User loggedIn = new User();
        loggedIn.setUserId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authUtil.loggedInUser()).thenReturn(loggedIn);

        assertThrows(APIException.class, () ->
                productService.updateProduct(1L, new ProductDTO(), "SELLER"));
    }

    @Test
    void adminCanUpdateProduct() {
        Product product = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        when(modelMapper.map(any(), eq(ProductDTO.class))).thenReturn(new ProductDTO());

        ProductDTO result =
                productService.updateProduct(1L, new ProductDTO(), "ADMIN");

        assertNotNull(result);
    }

    @Test
    void sellerCannotDeleteOthersProduct() {
        Product product = new Product();
        User owner = new User();
        owner.setUserId(1L);
        product.setUser(owner);

        User loggedIn = new User();
        loggedIn.setUserId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(authUtil.loggedInUser()).thenReturn(loggedIn);

        assertThrows(APIException.class,
                () -> productService.deleteProduct(1L, "SELLER"));
    }

    @Test
    void adminCanUpdateProductImage() throws Exception {
        Product product = new Product();

        MultipartFile file = Mockito.mock(MultipartFile.class);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(fileService.uploadImage(any(), any())).thenReturn("img.png");
        when(productRepository.save(any())).thenReturn(product);
        when(modelMapper.map(any(), eq(ProductDTO.class))).thenReturn(new ProductDTO());

        ProductDTO dto =
                productService.updateProductImage(1L, file, "ADMIN");

        assertNotNull(dto);
    }

    @Test
    void shouldGetAllProductsWithKeywordAndCategory() {
        Product product = new Product();
        product.setImage("img.png");

        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductResponse response =
                productService.getAllProducts(
                        0, 5, "price", "desc", "phone", "Electronics");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void shouldGetAllProductsForAdmin() {
        Product product = new Product();
        product.setImage("img.png");

        Page<Product> page =
                new org.springframework.data.domain.PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(page);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductResponse response =
                productService.getAllProductsForAdmin(0, 5, "price", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void shouldGetAllProductsForSeller() {
        User seller = new User();
        seller.setUserId(1L);

        Product product = new Product();
        product.setImage("img.png");

        Page<Product> page =
                new org.springframework.data.domain.PageImpl<>(List.of(product));

        when(authUtil.loggedInUser()).thenReturn(seller);
        when(productRepository.findByUser(eq(seller), any()))
                .thenReturn(page);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductResponse response =
                productService.getAllProductsForSeller(0, 5, "price", "asc");

        assertNotNull(response);
    }

    @Test
    void shouldSearchByCategorySuccessfully() {
        Category category = new Category();

        Product product = new Product();

        Page<Product> page =
                new org.springframework.data.domain.PageImpl<>(List.of(product));

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(productRepository.findByCategoryOrderByPriceAsc(any(), any()))
                .thenReturn(page);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductResponse response =
                productService.searchByCategory(1L, 0, 5, "price", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void shouldSearchProductByKeywordSuccessfully() {
        Product product = new Product();

        Page<Product> page =
                new org.springframework.data.domain.PageImpl<>(List.of(product));

        when(productRepository.findByProductNameLikeIgnoreCase(any(), any()))
                .thenReturn(page);
        when(modelMapper.map(any(Product.class), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductResponse response =
                productService.searchProductByKeyword("phone", 0, 5, "price", "asc");

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void sellerCanUpdateOwnProduct() {
        User seller = new User();
        seller.setUserId(1L);

        Product product = new Product();
        product.setUser(seller);

        when(authUtil.loggedInUser()).thenReturn(seller);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(productRepository.save(any()))
                .thenReturn(product);
        when(cartRepository.findCartsByProductId(1L))
                .thenReturn(List.of());
        when(modelMapper.map(any(), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductDTO result =
                productService.updateProduct(1L, new ProductDTO(), "SELLER");

        assertNotNull(result);
    }

    @Test
    void adminCanDeleteProduct() {
        Product product = new Product();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(modelMapper.map(any(), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductDTO result =
                productService.deleteProduct(1L, "ADMIN");

        assertNotNull(result);
        verify(productRepository).delete(product);
    }

    @Test
    void sellerCanUpdateOwnProductImage() throws Exception {
        User seller = new User();
        seller.setUserId(1L);

        Product product = new Product();
        product.setUser(seller);

        MultipartFile file = Mockito.mock(MultipartFile.class);

        when(authUtil.loggedInUser()).thenReturn(seller);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));
        when(fileService.uploadImage(any(), any()))
                .thenReturn("img.png");
        when(productRepository.save(any()))
                .thenReturn(product);
        when(modelMapper.map(any(), eq(ProductDTO.class)))
                .thenReturn(new ProductDTO());

        ProductDTO result =
                productService.updateProductImage(1L, file, "SELLER");

        assertNotNull(result);
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                productService,
                "imageBaseUrl",
                "http://localhost/images"
        );
    }
}