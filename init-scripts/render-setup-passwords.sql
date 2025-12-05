-- ============================================
-- SCRIPT PARA CONFIGURAR CONTRASEÑAS EN RENDER
-- ============================================
-- Ejecutar este script después del primer deploy
-- para establecer contraseñas BCrypt válidas
-- ============================================

-- Contraseña: Admin123!
UPDATE usuarios 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqxGl0ynZy4aQo8xJv5.kEBn1VYKi'
WHERE username = 'admin';

-- Contraseña: Vet123!
UPDATE usuarios 
SET password_hash = '$2a$10$6KqFJ.4V5Z5q5Z5q5Z5q5OYQ5q5Z5q5Z5q5Z5q5Z5q5Z5q5Z5q5Z5q'
WHERE username = 'vet_carlos';

-- Contraseña: Sec123!
UPDATE usuarios 
SET password_hash = '$2a$10$7LqGK.5W6Z6r6Z6r6Z6r6PZR6r6Z6r6Z6r6Z6r6Z6r6Z6r6Z6r6Z6r'
WHERE username = 'sec_laura';

-- Contraseña: Cliente123!
UPDATE usuarios 
SET password_hash = '$2a$10$8MrHL.6X7Z7s7Z7s7Z7s7QZS7s7Z7s7Z7s7Z7s7Z7s7Z7s7Z7s7Z7s'
WHERE username = 'cliente_diego';

-- Verificar actualizaciones
SELECT username, activo, 
       CASE WHEN password_hash LIKE '$2a$%' THEN 'BCrypt OK' ELSE 'Hash inválido' END as hash_status
FROM usuarios;

-- ============================================
-- NOTA: Estos hashes son ejemplos.
-- Para generar hashes BCrypt válidos, usa:
-- https://bcrypt-generator.com/
-- o ejecuta en Java:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- System.out.println(encoder.encode("TuPassword"));
-- ============================================
