package kz.azure.ms.controller;

import kz.azure.ms.model.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "auth")
@RequiredArgsConstructor
public class UserController {
    private final ReactiveUserDetailsService userDetailsService;


    @PostMapping("/login")
    public Mono<ResponseEntity> login(@RequestBody UserLoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .map(UserDetails::getPassword)
                .map(storedHashedPassword -> {
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    boolean matches = passwordEncoder.matches(request.getPassword(), storedHashedPassword);
                    return matches ? new ResponseEntity<>(HttpStatus.OK) : new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                });
    }

    @PostMapping("/register")
    public Mono<ResponseEntity> register(@RequestBody UserLoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .flatMap(user -> Mono.error(new RuntimeException("User already exists")))
                .then(Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }
}
