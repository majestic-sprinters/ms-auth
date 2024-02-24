package kz.azure.ms.service.impl;

import kz.azure.ms.model.User;
import kz.azure.ms.model.dto.UserLoginRequest;
import kz.azure.ms.repository.UserRepository;
import kz.azure.ms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Mono<Void> registerUser(UserLoginRequest userLoginRequest) {
        return userRepository.findByUsername(userLoginRequest.getUsername())
                .flatMap(user -> Mono.error(new RuntimeException("User already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    String encodedPassword = passwordEncoder.encode(userLoginRequest.getPassword());
                    User newUser = new User();
                    newUser.setUsername(userLoginRequest.getUsername());
                    newUser.setPassword(encodedPassword);
                    return userRepository.save(newUser).then();
                })).then();
    }


}
