package kz.azure.ms.service;

import kz.azure.ms.model.dto.UserLoginRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<Void> registerUser(UserLoginRequest userLoginRequest);
}
