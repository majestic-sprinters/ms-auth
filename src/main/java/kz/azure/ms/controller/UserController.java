package kz.azure.ms.controller;

import kz.azure.ms.model.User;
import kz.azure.ms.model.dto.UserLoginRequest;
import kz.azure.ms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Base64;

@RestController
@RequestMapping(value = "auth")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReactiveAuthenticationManager reactiveAuthenticationManager;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, ReactiveAuthenticationManager reactiveAuthenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reactiveAuthenticationManager = reactiveAuthenticationManager;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody UserLoginRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        return this.reactiveAuthenticationManager.authenticate(authentication)
                .flatMap(authResult -> {
                    SecurityContextImpl securityContext = new SecurityContextImpl(authResult);
                    ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext));
                    return Mono.just(authResult);
                })
                .cast(Authentication.class)
                .map(auth -> {
                    if (auth.isAuthenticated()) {
                        return ResponseEntity.ok().body("Login Successful");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user)
                .map(u -> ResponseEntity.status(HttpStatus.CREATED).build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }
}
