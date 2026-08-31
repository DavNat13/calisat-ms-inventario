package com.califorge.msinventario.service;

import com.califorge.msinventario.exception.SkuDuplicadoException;
import com.califorge.msinventario.model.Producto;
import com.califorge.msinventario.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un producto validando que el SKU sea unico.
     */
    public Producto crear(Producto producto) {
        if (productoRepository.existsBySku(producto.getSku())) {
            throw new SkuDuplicadoException(producto.getSku());
        }
        return productoRepository.save(producto);
    }

    /**
     * Lista unicamente los productos activos.
     */
    @Transactional(readOnly = true)
    public List<Producto> listar() {
        return productoRepository.findByActivoTrue();
    }

    /**
     * Busca un producto por su id.
     */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(UUID id) {
        return productoRepository.findById(id);
    }

    /**
     * Actualiza los campos editables de un producto existente.
     */
    public Optional<Producto> actualizar(UUID id, String sku, String nombre, String categoria,
                                         String descripcion, BigDecimal precio, Integer stock,
                                         String unidadMedida) {
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setSku(sku);
                    producto.setNombre(nombre);
                    producto.setCategoria(categoria);
                    producto.setDescripcion(descripcion);
                    producto.setPrecio(precio);
                    producto.setStock(stock);
                    producto.setUnidadMedida(unidadMedida);
                    return productoRepository.save(producto);
                });
    }

    /**
     * Baja logica: desactiva un producto sin eliminarlo fisicamente.
     */
    public Optional<Producto> desactivar(UUID id) {
        return productoRepository.findById(id)
                .map(producto -> {
                    producto.setActivo(false);
                    return productoRepository.save(producto);
                });
    }
}
