package br.com.fiap.postech.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.domain.owner.exception.DuplicatedOwnerException;
import br.com.fiap.postech.domain.owner.exception.InvalidDocumentException;
import br.com.fiap.postech.domain.owner.exception.InvalidEmailException;
import br.com.fiap.postech.domain.owner.exception.NoMatchingOwnersException;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.user.exception.UserNotFoundException;
import br.com.fiap.postech.domain.user.exception.NoMatchingUsersException;
import br.com.fiap.postech.domain.user.exception.SameUsernameException;
import br.com.fiap.postech.domain.vehicle.excecption.DuplicatedVehicleException;
import br.com.fiap.postech.domain.vehicle.excecption.InvalidLicensePlateException;
import br.com.fiap.postech.domain.vehicle.excecption.InvalidVehicleYearException;
import br.com.fiap.postech.domain.vehicle.excecption.NoMatchingVehiclesException;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleNotFoundException;
import br.com.fiap.postech.domain.authentication.exception.InvalidChatbotApiKeyException;
import br.com.fiap.postech.domain.authentication.exception.InvalidPasswordException;
import br.com.fiap.postech.domain.user.exception.UsernameNotFoundException;


@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(SameUsernameException.class)
    public ResponseEntity<ErrorResponse> handleSameUsernameException(SameUsernameException ex) {
        final var httpStatus = HttpStatus.CONFLICT;
        final var response = new ErrorResponse(httpStatus.value(), "USER_CONFLICT_DUPLICATED_USERNAME", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        var response = new ErrorResponse(httpStatus.value(), "USER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(NoMatchingUsersException.class)
    public ResponseEntity<ErrorResponse> handleNoMatchingUsersException(NoMatchingUsersException ex) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoMatchingVehiclesException.class)
    public ResponseEntity<ErrorResponse> handleNoMatchingVehiclesException(NoMatchingVehiclesException ex) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicatedVehicleException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedVehicleException(DuplicatedVehicleException ex) {
        final var httpStatus = HttpStatus.CONFLICT;
        final var response = new ErrorResponse(httpStatus.value(), "VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFoundException(VehicleNotFoundException ex) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        final var response = new ErrorResponse(httpStatus.value(), "VEHICLE_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(NoMatchingOwnersException.class)
    public ResponseEntity<ErrorResponse> handleNoMatchingOwnersException(NoMatchingOwnersException ex) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(DuplicatedOwnerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedOwnerException(DuplicatedOwnerException ex) {
        final var httpStatus = HttpStatus.CONFLICT;
        final var response = new ErrorResponse(httpStatus.value(), "OWNER_ALREADY_EXISTS", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(OwnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOwnerNotFoundException(OwnerNotFoundException ex) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        final var response = new ErrorResponse(httpStatus.value(), "OWNER_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponse(httpStatus.value(), "USERNAME_NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var response = new ErrorResponse(httpStatus.value(), "INVALID_PASSWORD", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidChatbotApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChatbotApiKeyException(InvalidChatbotApiKeyException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        var response = new ErrorResponse(httpStatus.value(), "INVALID_CHATBOT_API_KEY", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDocumentException(InvalidDocumentException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var response = new ErrorResponse(httpStatus.value(), "INVALID_DOCUMENT", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailException(InvalidEmailException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var response = new ErrorResponse(httpStatus.value(), "INVALID_EMAIL", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidLicensePlateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLicensePlateException(InvalidLicensePlateException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var response = new ErrorResponse(httpStatus.value(), "INVALID_LICENSE_PLATE", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }

    @ExceptionHandler(InvalidVehicleYearException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVehicleYearException(InvalidVehicleYearException ex) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        final var response = new ErrorResponse(httpStatus.value(), "INVALID_VEHICLE_YEAR", ex.getMessage());
        return new ResponseEntity<>(response, httpStatus);
    }
}
