package dev.alphasow.ecommerce.order.infrastructure.web;

import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class OrderExceptionHandler {
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ProblemDetail> notFound(OrderNotFoundException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

    return ResponseEntity.status(404).body(problem);
  }

  @ExceptionHandler(OrderDomainException.class)
  public ResponseEntity<ProblemDetail> businessError(OrderDomainException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

    return ResponseEntity.status(409).body(problem);
  }
}
