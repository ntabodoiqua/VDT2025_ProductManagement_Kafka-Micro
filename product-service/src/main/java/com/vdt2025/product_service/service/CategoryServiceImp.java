package com.vdt2025.product_service.service;

import com.vdt2025.product_service.dto.request.category.CategoryCreationRequest;
import com.vdt2025.product_service.dto.response.CategoryResponse;
import com.vdt2025.product_service.exception.AppException;
import com.vdt2025.product_service.exception.ErrorCode;
import com.vdt2025.product_service.mapper.CategoryMapper;
import com.vdt2025.product_service.repository.CategoryRepository;
import com.vdt2025.product_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryServiceImp implements CategoryService{
    ProductRepository productRepository;
//    UserRepository userRepository;
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;
//    FileStorageService fileStorageService;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public CategoryResponse createCategory(CategoryCreationRequest request) {
        // Kiểm tra xem danh mục đã tồn tại chưa
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category {} already exists", request.getName());
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }

        // Lấy thông tin người dùng hiện tại từ SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Current user: {}", username);

        // Tạo danh mục mới
        var category = categoryMapper.toCategory(request);
        category.setCreatedBy();
        category = categoryRepository.save(category);
        log.info("Category {} created successfully by user {}", category.getName(), currentUser.getUsername());
        // Trả về thông tin danh mục đã tạo
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    public Page<CategoryResponse> searchCategories(CategoryFilterRequest filter, Pageable pageable) {
        Specification<Category> spec = CategorySpecification.withFilter(filter);
        Page<Category> resultPage = categoryRepository.findAll(spec, pageable);
        return resultPage.map(categoryMapper::toCategoryResponse);
    }

    @Override
    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        log.info("Retrieved category: {}", category.getName());
        return categoryMapper.toCategoryResponse(category);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public CategoryResponse updateCategory(String id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Lấy thông tin người dùng hiện tại từ SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra xem người dùng có phải admin không
        boolean isAdmin = currentUser.getRole().getName().equals("ADMIN");
        // Kiểm tra xem người dùng có phải người tạo danh mục không
        boolean isOwner = category.getCreatedBy().getUsername().equals(currentUser.getUsername());
        // Nếu không phải admin và cũng không phải người tạo danh mục, không cho phép cập nhật
        if (!isAdmin && !isOwner) {
            log.warn("User {} is not authorized to update category {}", currentUser.getUsername(), category.getName());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Kiểm tra xem tên danh mục mới có trùng với danh mục khác không
        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByName(request.getName())) {
            log.warn("Category {} already exists", request.getName());
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        // Cập nhật thông tin danh mục
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category = categoryRepository.save(category);

        log.info("Category {} updated successfully", category.getName());
        return categoryMapper.toCategoryResponse(category);
    }

    // Cập nhật thumbnail của danh mục
    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String setCategoryThumbnail(String id, MultipartFile file) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if (!checkAccessRights(category)) {
            log.warn("User does not have access rights to update thumbnail for category {}", category.getName());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Cập nhật thumbnail
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Invalid file type for thumbnail: {}", contentType);
            throw new AppException(ErrorCode.INVALID_IMAGE_TYPE);
        }

        String fileName = fileStorageService.storeFile(file);
        category.setImageName(fileName);
        categoryRepository.save(category);
        log.info("Thumbnail for category {} updated successfully", category.getName());
        return fileName;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        // Kiểm tra quyền truy cập
        if (!checkAccessRights(category)) {
            log.warn("User does not have access rights to delete category {}", category.getName());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        // Xóa danh mục và set Các sản phầm thuộc danh mục này sẽ được chuyển sang danh mục Chưa phân loại
        // Tìm các sản phẩm thuộc danh mục này
        List<Product> products = productRepository.findAllByCategoryId(id);
        if (!products.isEmpty()) {
            // Chuyển các sản phẩm sang danh mục "Chưa phân loại"
            Category uncategorizedCategory = categoryRepository.findByName("Chưa phân loại")
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

            products.forEach(product -> product.setCategory(uncategorizedCategory));
            productRepository.saveAll(products);
            log.info("Products in category {} have been moved to 'Chưa phân loại'", category.getName());
        }
        // Xóa danh mục
        categoryRepository.delete(category);
        log.info("Category {} has been deleted successfully", category.getName());
    }

    // Hàm chung để kiểm tra quyền truy cập
    private boolean checkAccessRights(Category category) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = currentUser.getRole().getName().equals("ADMIN");
        boolean isOwner = category.getCreatedBy().getUsername().equals(currentUser.getUsername());

        if (!isAdmin && !isOwner) {
            log.warn("User {} is not authorized to access category {}", currentUser.getUsername(), category.getName());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return true;
    }

}
