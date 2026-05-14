package cl.smartlogix.inventario.config;

import cl.smartlogix.inventario.entity.Producto;
import cl.smartlogix.inventario.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    private final ProductoRepository productoRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (productoRepository.count() == 0) {
                Producto p1 = new Producto(null, "Laptop", 10);
                Producto p2 = new Producto(null, "Mouse", 50);
                productoRepository.save(p1);
                productoRepository.save(p2);
                System.out.println("Datos iniciales cargados");
            }
        };
    }
}