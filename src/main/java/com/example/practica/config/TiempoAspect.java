package com.example.practica.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Mide cuánto tarda cada método de controller, service, repository y PasswordEncoder.
 * Imprime en consola con sangría para ver quién llamó a quién.
 * Es temporal: bórrala cuando termines de diagnosticar.
 */
@Aspect
@Component
public class TiempoAspect {

    private static final Logger log = LoggerFactory.getLogger("TIEMPOS");
    private static final long UMBRAL_MS = 200; // arriba de esto sale como WARN

    private static final ThreadLocal<Integer> PROFUNDIDAD = ThreadLocal.withInitial(() -> 0);

    @Around(
            "within(com.example.practica.controller..*) || " +
                    "within(com.example.practica.service..*) || " +
                    "within(org.springframework.data.repository.Repository+) || " +
                    "target(org.springframework.security.crypto.password.PasswordEncoder)"
    )
    public Object medir(ProceedingJoinPoint pjp) throws Throwable {
        int nivel = PROFUNDIDAD.get();
        PROFUNDIDAD.set(nivel + 1);
        long inicio = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            PROFUNDIDAD.set(nivel);
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            String nombre = pjp.getSignature().getDeclaringType().getSimpleName()
                    + "." + pjp.getSignature().getName();
            String linea = "  ".repeat(nivel) + nombre + " -> " + ms + " ms";
            if (ms >= UMBRAL_MS) {
                log.warn(linea);
            } else {
                log.info(linea);
            }
            if (nivel == 0) {
                PROFUNDIDAD.remove();
            }
        }
    }
}