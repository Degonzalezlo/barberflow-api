package com.barberflow.modules.users.domain.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.barberflow.modules.users.domain.entities.Product;

@Repository
public interface IProductRepository extends JpaRepository<Product, Long> {
    
    List<String> findProductsByBarbershopBarbershopId(Long barbershopId);

    List<Product> findByBarbershopBarbershopIdAndStockQuantityLessThan(Long barbershopId, Integer threshold);
}
