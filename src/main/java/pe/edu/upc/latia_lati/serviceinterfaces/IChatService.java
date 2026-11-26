package pe.edu.upc.latia_lati.serviceinterfaces;

import pe.edu.upc.latia_lati.dtos.ChatRequestDTO;
import pe.edu.upc.latia_lati.dtos.ChatResponseDTO;

public interface IChatService {
    ChatResponseDTO generateReply(ChatRequestDTO request);
}
