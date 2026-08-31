package com.califorge.msinventario.repository;

import com.califorge.msinventario.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {

    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Producto> findByActivoTrue();
}
