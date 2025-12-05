package com.tuorg.veterinaria.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== Generador de Hashes BCrypt para Seeders ===\n");
        
        // Admin123!
        String adminPass = "Admin123!";
        String adminHash = encoder.encode(adminPass);
        System.out.println("Password: " + adminPass);
        System.out.println("Hash: " + adminHash);
        System.out.println("Verificación: " + encoder.matches(adminPass, adminHash));
        System.out.println();
        
        // password (simple para superadmin)
        String superPass = "password";
        String superHash = encoder.encode(superPass);
        System.out.println("Password: " + superPass);
        System.out.println("Hash: " + superHash);
        System.out.println("Verificación: " + encoder.matches(superPass, superHash));
        System.out.println();
        
        // Test1234!
        String testPass = "Test1234!";
        String testHash = encoder.encode(testPass);
        System.out.println("Password: " + testPass);
        System.out.println("Hash: " + testHash);
        System.out.println("Verificación: " + encoder.matches(testPass, testHash));
    }
}
