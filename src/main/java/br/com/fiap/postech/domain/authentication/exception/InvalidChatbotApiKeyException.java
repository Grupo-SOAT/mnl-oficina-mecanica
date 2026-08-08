package br.com.fiap.postech.domain.authentication.exception;

public class InvalidChatbotApiKeyException extends RuntimeException {
    public InvalidChatbotApiKeyException() {
        super("Invalid chatbot API key");
    }
}
