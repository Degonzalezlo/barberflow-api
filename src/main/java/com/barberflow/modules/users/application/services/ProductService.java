package com.barberflow.modules.users.application.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.barberflow.modules.users.application.dtos.ProductRequestDTO;
import com.barberflow.modules.users.application.dtos.ProductResponseDTO;
import com.barberflow.modules.users.domain.entities.Barbershop;
import com.barberflow.modules.users.domain.entities.Product;
import com.barberflow.modules.users.domain.entities.User;
import com.barberflow.modules.users.domain.repositories.IProductRepository;
import com.barberflow.modules.users.domain.repositories.IUserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Integer DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final IProductRepository productRepository;
    private final IUserRepository userRepository;

    // 1. Crear producto asociado automáticamente a la sede del ADMIN autenticado
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO dto, String adminEmail) {
        User admin = getAdminByEmail(adminEmail);
        Barbershop barbershop = admin.getBarbershop();

        Product product = Product.builder()
                .barbershop(barbershop)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(dto.getCategory())
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToResponseDTO(savedProduct);
    }

    // 2. Obtener todo el inventario de la sede
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getInventoryByAdmin(String adminEmail) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();

        return productRepository.findProductsByBarbershopBarbershopId(barbershopId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // 3. Ajustar el stock (incrementar o reducir usando los métodos de dominio)
    @Transactional
    public ProductResponseDTO updateStock(Long productId, int quantityChange, String adminEmail) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();

        Product product = productRepository.findByIdAndBarbershopBarbershopId(productId, barbershopId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado o no pertenece a tu sede"));

        if (quantityChange > 0) {
            product.addStock(quantityChange);
        } else if (quantityChange < 0) {
            product.reduceStock(Math.abs(quantityChange));
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponseDTO(updatedProduct);
    }

    // 4. Consultar productos con bajo stock
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getLowStockProducts(String adminEmail, Integer threshold) {
        User admin = getAdminByEmail(adminEmail);
        Long barbershopId = admin.getBarbershop().getBarbershopId();
        int finalThreshold = (threshold != null && threshold > 0) ? threshold : DEFAULT_LOW_STOCK_THRESHOLD;

        return productRepository.findByBarbershopBarbershopIdAndStockQuantityLessThan(barbershopId, finalThreshold)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    // Métodos auxiliares de mapeo y búsqueda
    private User getAdminByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con email: " + email));
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .barbershopId(product.getBarbershop().getBarbershopId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory())
                .lastRestock(product.getLastRestock())
                .build();
    }
}
