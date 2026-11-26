package pe.edu.upc.latia_lati.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ChatRequestDTO {
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 4000, message = "El mensaje admite hasta 4000 caracteres")
    private String message;

    @Size(max = 20, message = "El historial admite hasta 20 mensajes")
    private List<@Valid ChatMessageDTO> history;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDTO> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageDTO> history) {
        this.history = history;
    }
}
