package br.com.labs.dto.response;

public record TokenResponse(
    String token,
    String type
) {
    public TokenResponse(String token) {
        this(token, "Bearer");
    }
}
