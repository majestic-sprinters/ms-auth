package kz.azure.ms.service.impl;

import kz.azure.ms.model.dto.UserLoginRequest;
import kz.azure.ms.repository.UserRepository;
import kz.azure.ms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Mono<Void> registerUser(UserLoginRequest userLoginRequest) {
        Mono<Void> userAlreadyExists = userRepository.findByUsername(userLoginRequest.getUsername())
                .flatMap(user -> Mono.error(new RuntimeException("User already exists")))
                .then();
        return userAlreadyExists;
    }


}
