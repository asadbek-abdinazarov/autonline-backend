package uz.javachi.autonline.exceptions;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler({UserIsNotActiveException.class})
    public ResponseEntity<Map<String, String>> userIsNotActiveException(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Authentication failed", "message", ex.getMessage()));
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed", "message", ex.getMessage()));
    }

    @ExceptionHandler({TokenException.class})
    public ResponseEntity<Map<String, String>> handleTokenException(TokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "REFRESH_TOKEN_INVALID", "message", ex.getMessage()));
    }

    @ExceptionHandler({UserBlockedOrDeletedException.class})
    public ResponseEntity<Map<String, String>> handleUserBlockedOrDeletedException(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Blocked or Deleted", "message", ex.getMessage()));
    }

    @ExceptionHandler(CustomRoleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomRoleNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Role", "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        Map<String, Object> response = Map.of(
                "error", "Validation Failed",
                "message", messages
        );

        return ResponseEntity.badRequest().body(response);
    }


}
