package br.com.fiap.postech.config.ci;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// controller usado para evitar warn do spider 
@RestController
public class SpiderRootControllerSuppressWarning {

        @GetMapping("/")
        public ResponseEntity<String> root() {
            return ResponseEntity.ok("OK");
        }

}
