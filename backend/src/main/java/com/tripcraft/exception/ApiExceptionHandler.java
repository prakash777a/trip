package com.tripcraft.exception;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.dao.EmptyResultDataAccessException; import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException; import org.slf4j.Logger; import org.slf4j.LoggerFactory; import java.time.Instant; import java.time.format.DateTimeParseException; import java.util.*;
@RestControllerAdvice public class ApiExceptionHandler {
  private static final Logger log=LoggerFactory.getLogger(ApiExceptionHandler.class);
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){List<Map<String,String>> fields=e.getBindingResult().getFieldErrors().stream().map(x->Map.of("field",x.getField(),"message",Objects.toString(x.getDefaultMessage(),"Invalid value"))).toList();return body(400,"VALIDATION_FAILED","Please correct the highlighted fields",fields);}
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> denied(AccessDeniedException e){return body(403,"FORBIDDEN","You do not have access to this resource",List.of());}
  @ExceptionHandler(NoSuchElementException.class) ResponseEntity<?> missing(NoSuchElementException e){return body(404,"NOT_FOUND",e.getMessage(),List.of());}
  @ExceptionHandler(EmptyResultDataAccessException.class) ResponseEntity<?> empty(EmptyResultDataAccessException e){return body(404,"NOT_FOUND","The requested record was not found",List.of());}
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> integrity(DataIntegrityViolationException e){return body(409,"DATA_CONFLICT","The requested change conflicts with existing data",List.of());}
  @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> conflict(IllegalStateException e){return body(409,"BUSINESS_CONFLICT",e.getMessage(),List.of());}
  @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> bad(IllegalArgumentException e){return body(400,"REQUEST_FAILED",e.getMessage(),List.of());}
  @ExceptionHandler(MethodArgumentTypeMismatchException.class) ResponseEntity<?> mismatch(MethodArgumentTypeMismatchException e){return body(400,"INVALID_PARAMETER","A request parameter or path value has an invalid format",List.of());}
  @ExceptionHandler(DateTimeParseException.class) ResponseEntity<?> date(DateTimeParseException e){return body(400,"INVALID_PARAMETER","A date parameter must use YYYY-MM-DD format",List.of());}
  @ExceptionHandler(Exception.class) ResponseEntity<?> error(Exception e){log.error("Unhandled API exception",e);return body(500,"INTERNAL_ERROR","Unexpected server error",List.of());}
  private ResponseEntity<?> body(int status,String code,String message,List<?> fields){return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"status",status,"code",code,"message",message==null?"Request failed":message,"fieldErrors",fields));}
}
