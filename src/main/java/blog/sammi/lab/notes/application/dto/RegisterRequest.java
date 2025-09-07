package blog.sammi.lab.notes.application.dto;

public record RegisterRequest(
    String username,
    String email,
    String password
) {}
