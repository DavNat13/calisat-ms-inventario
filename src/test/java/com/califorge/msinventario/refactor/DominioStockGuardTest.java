package com.califorge.msinventario.refactor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Liga la claim del dominio: calisat-ms-inventario gestiona Stock, no Producto.
 * Falla si en src/ aparece la palabra Producto/producto (classes, imports, referencias, SQL).
 * Se excluye a si mismo por nombre de archivo.
 */
class DominioStockGuardTest {

    @Test
    void srcNoMencionaProducto() throws IOException {
        Path src = Path.of("src");
        try (Stream<Path> walk = Files.walk(src)) {
            String offenders = walk
                    .filter(p -> p.toString().endsWith(".java") || p.toString().endsWith(".sql"))
                    .filter(p -> !p.getFileName().toString().equals("DominioStockGuardTest.java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).matches("(?s).*\\bProducto\\b.*") ||
                                   Files.readString(p).contains("producto");
                        } catch (IOException e) {
                            return true;
                        }
                    })
                    .map(Path::toString)
                    .reduce((a, b) -> a + System.lineSeparator() + b)
                    .orElse("");

            assertTrue(offenders.isEmpty(),
                    "El dominio debe ser Stock. Referencias residuales a Producto en:" + System.lineSeparator() + offenders);
        }
    }
}