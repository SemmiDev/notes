package blog.sammi.lab.notes.presentation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // Validation Errors
    VALIDATION_ERROR("VALIDATION_ERROR", "Permintaan tidak valid"),
    
    // Authentication & Authorization
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "Autentikasi gagal"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Username atau password salah"),
    ACCOUNT_NOT_VERIFIED("ACCOUNT_NOT_VERIFIED", "Akun belum diverifikasi"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Akun telah dinonaktifkan"),
    ACCESS_DENIED("ACCESS_DENIED", "Akses ditolak"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token telah kedaluwarsa"),
    INVALID_TOKEN("INVALID_TOKEN", "Token tidak valid"),
    
    // User Management
    USER_NOT_FOUND("USER_NOT_FOUND", "Pengguna tidak ditemukan"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "Pengguna sudah ada"),
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email sudah terdaftar"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "Username sudah digunakan"),
    
    // OTP & Password Reset
    INVALID_OTP("INVALID_OTP", "Kode OTP tidak valid"),
    OTP_EXPIRED("OTP_EXPIRED", "Kode OTP telah kedaluwarsa"),
    INVALID_RESET_TOKEN("INVALID_RESET_TOKEN", "Token reset password tidak valid"),
    RESET_TOKEN_EXPIRED("RESET_TOKEN_EXPIRED", "Token reset password telah kedaluwarsa"),
    
    // Resource Management
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Resource tidak ditemukan"),
    NOTE_NOT_FOUND("NOTE_NOT_FOUND", "Catatan tidak ditemukan"),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Kategori tidak ditemukan"),
    TAG_NOT_FOUND("TAG_NOT_FOUND", "Tag tidak ditemukan"),
    
    // Business Logic
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Izin tidak mencukupi"),
    OPERATION_NOT_ALLOWED("OPERATION_NOT_ALLOWED", "Operasi tidak diizinkan"),
    DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource sudah ada"),
    
    // System Errors
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Terjadi kesalahan sistem"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Layanan tidak tersedia"),
    DATABASE_ERROR("DATABASE_ERROR", "Kesalahan database"),
    EMAIL_SEND_FAILED("EMAIL_SEND_FAILED", "Gagal mengirim email"),
    
    // Rate Limiting & Throttling
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Terlalu banyak permintaan"),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Batas rate limit terlampaui");
    
    private final String code;
    private final String defaultMessage;
}
