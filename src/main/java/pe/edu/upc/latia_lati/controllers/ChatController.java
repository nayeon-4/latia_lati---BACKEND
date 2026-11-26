package pe.edu.upc.latia_lati.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.latia_lati.dtos.ChatRequestDTO;
import pe.edu.upc.latia_lati.dtos.ChatResponseDTO;
import pe.edu.upc.latia_lati.serviceinterfaces.IChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final IChatService chatService;

    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> enviarMensaje(
            @Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(chatService.generateReply(request));
    }
}
