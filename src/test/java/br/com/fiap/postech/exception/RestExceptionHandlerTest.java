package br.com.fiap.postech.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.domain.authentication.exception.InvalidChatbotApiKeyException;
import br.com.fiap.postech.domain.authentication.exception.InvalidPasswordException;
import br.com.fiap.postech.domain.owner.exception.DuplicatedOwnerException;
import br.com.fiap.postech.domain.owner.exception.InvalidDocumentException;
import br.com.fiap.postech.domain.owner.exception.InvalidEmailException;
import br.com.fiap.postech.domain.owner.exception.NoMatchingOwnersException;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.user.exception.NoMatchingUsersException;
import br.com.fiap.postech.domain.user.exception.SameUsernameException;
import br.com.fiap.postech.domain.user.exception.UserNotFoundException;
import br.com.fiap.postech.domain.user.exception.UsernameNotFoundException;
import br.com.fiap.postech.domain.vehicle.excecption.DuplicatedVehicleException;
import br.com.fiap.postech.domain.vehicle.excecption.InvalidLicensePlateException;
import br.com.fiap.postech.domain.vehicle.excecption.NoMatchingVehiclesException;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleNotFoundException;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void should_handle_same_username_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleSameUsernameException(
                new SameUsernameException()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getReason()).isEqualTo("USER_CONFLICT_DUPLICATED_USERNAME");
        assertThat(response.getBody().getMessage()).isEqualTo("Username already exists");
    }

    @Test
    void should_handle_user_not_found_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFoundException(
                new UserNotFoundException()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getReason()).isEqualTo("USER_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("User not found");
    }

    @Test
    void should_handle_no_matching_users_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleNoMatchingUsersException(
                new NoMatchingUsersException("john")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_no_matching_vehicles_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleNoMatchingVehiclesException(
                new NoMatchingVehiclesException("ABC1234")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_duplicated_vehicle_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicatedVehicleException(
                new DuplicatedVehicleException("ABC1234")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getReason()).isEqualTo("VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE");
    }

    @Test
    void should_handle_vehicle_not_found_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleVehicleNotFoundException(
                new VehicleNotFoundException(1L)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getReason()).isEqualTo("VEHICLE_NOT_FOUND");
    }

    @Test
    void should_handle_no_matching_owners_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleNoMatchingOwnersException(
                new NoMatchingOwnersException("teste@email.com")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_duplicated_owner_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicatedOwnerException(
                new DuplicatedOwnerException("31058167049")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getReason()).isEqualTo("OWNER_ALREADY_EXISTS");
    }

    @Test
    void should_handle_owner_not_found_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleOwnerNotFoundException(
                new OwnerNotFoundException(1L)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo(404);
        assertThat(response.getBody().getReason()).isEqualTo("OWNER_NOT_FOUND");
    }

    @Test
    void should_handle_username_not_found_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleUsernameNotFoundException(
                new UsernameNotFoundException("teste")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("USERNAME_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("User not found by username: teste");
    }

    @Test
    void should_handle_invalid_password_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidPasswordException(
                new InvalidPasswordException()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("INVALID_PASSWORD");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid password");
    }

    @Test
    void should_handle_invalid_chatbot_api_key_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidChatbotApiKeyException(
                new InvalidChatbotApiKeyException()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("INVALID_CHATBOT_API_KEY");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid chatbot API key");
    }

    @Test
    void should_handle_invalid_document_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidDocumentException(
                new InvalidDocumentException("123")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("INVALID_DOCUMENT");
    }

    @Test
    void should_handle_invalid_email_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidEmailException(
                new InvalidEmailException("email-invalido")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("INVALID_EMAIL");
    }

    @Test
    void should_handle_invalid_license_plate_exception() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidLicensePlateException(
                new InvalidLicensePlateException("AAA000")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(400);
        assertThat(response.getBody().getReason()).isEqualTo("INVALID_LICENSE_PLATE");
    }
}
