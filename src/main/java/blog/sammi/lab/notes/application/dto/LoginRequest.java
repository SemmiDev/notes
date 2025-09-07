package blog.sammi.lab.notes.application.dto;

public record LoginRequest(
    String usernameOrEmail,
    String password
) {}
