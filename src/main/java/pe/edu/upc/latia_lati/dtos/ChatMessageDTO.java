package pe.edu.upc.latia_lati.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChatMessageDTO {
    @NotBlank
    @Pattern(regexp = "user|model", message = "El rol debe ser user o model")
    private String role;

    @NotBlank
    @Size(max = 2000, message = "Cada mensaje del historial admite hasta 2000 caracteres")
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
