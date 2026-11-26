package pe.edu.upc.latia_lati.dtos;

public class ChatResponseDTO {
    private final String reply;

    public ChatResponseDTO(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }
}
