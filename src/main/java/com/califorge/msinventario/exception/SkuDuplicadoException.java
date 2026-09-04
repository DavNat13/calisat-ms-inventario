package com.califorge.msinventario.exception;

public class SkuDuplicadoException extends RuntimeException {

    public SkuDuplicadoException(String sku) {
        super("Ya existe un Stock con el SKU: " + sku);
    }
}
