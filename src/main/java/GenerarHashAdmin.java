import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerarHashAdmin {

    private static final Logger log = LoggerFactory.getLogger(GenerarHashAdmin.class);

    public static void main(String[] args) {
        // ⚠️ No hardcodear la contraseña en producción
        String password = System.getenv("ADMIN_PASSWORD"); // Leer de variable de entorno
        if (password == null || password.isEmpty()) {
            log.error("No se encontró la variable de entorno ADMIN_PASSWORD");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);

        log.info("Password original proporcionada vía entorno");
        log.info("Hash generado: {}", hash);

        log.info("\nSQL UPDATE:");
        log.info("UPDATE usuarios SET password_hash = '{}', activo = true WHERE username = 'admin';", hash);
    }
}
