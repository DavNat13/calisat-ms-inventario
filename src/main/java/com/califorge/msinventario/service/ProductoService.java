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
     *
     * Nota sobre concurrencia: el check {@code existsBySku} es una defensa temprana
     * para responder 400 de forma rapida, NO es una garantia de unicidad. La garantia
     * real la aporta la constraint UNIQUE del SKU en BD (que lanza
     * DataIntegrityViolationException y es traducida a 400 por el
     * GlobalExceptionHandler) en escenarios TOCTOU con requests simultaneos.
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
     * Busca un producto por su id, solo si esta activo (coherente con listar).
     */
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(UUID id) {
        return productoRepository.findByIdAndActivoTrue(id);
    }

    /**
     * Actualiza los campos editables de un producto existente.
     *
     * Nota sobre concurrencia: igual que en {@code crear}, el check de SKU es defensa
     * temprana; la garantia de unicidad es la constraint UNIQUE en BD.
     */
    public Optional<Producto> actualizar(UUID id, String sku, String nombre, String categoria,
                                         String descripcion, BigDecimal precio, Integer stock,
                                         String unidadMedida) {
        if (sku != null && productoRepository.existsBySku(sku)) {
            Optional<Producto> mismo = productoRepository.findBySku(sku);
            if (mismo.isEmpty() || !mismo.get().getId().equals(id)) {
                throw new SkuDuplicadoException(sku);
            }
        }
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
