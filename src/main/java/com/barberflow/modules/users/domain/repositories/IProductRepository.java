package com.barberflow.modules.users.domain.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.barberflow.modules.users.domain.entities.Product;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findProductsByBarbershopBarbershopId(Long barbershopId);

    List<Product> findByBarbershopBarbershopIdAndStockQuantityLessThan(Long barbershopId, Integer threshold);

    Optional<Product> findByIdAndBarbershopBarbershopId(Long productId, Long barbershopId);
}
