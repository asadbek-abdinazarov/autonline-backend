package uz.javachi.autonline.exceptions;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler({UserIsNotActiveException.class, RuntimeException.class})
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.internalServerError()
                .body(Map.of("error", "Authentication failed", "message", ex.getMessage()));
    }

    @ExceptionHandler(CustomRoleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomRoleNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Role", "message", ex.getMessage()));
    }

}
