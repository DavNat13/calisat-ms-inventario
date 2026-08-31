package com.califorge.msinventario.exception;

public class SkuDuplicadoException extends RuntimeException {

    public SkuDuplicadoException(String sku) {
        super("Ya existe un producto con el SKU: " + sku);
    }
}
