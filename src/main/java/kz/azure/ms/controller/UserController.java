package kz.azure.ms.controller;

import kz.azure.ms.model.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class UserController {
    private final ReactiveUserDetailsService userDetailsService;


    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(@RequestBody UserLoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .map(userDetails -> {
                    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    if (passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
                        String authValue = "Basic " + Base64.getEncoder().encodeToString((request.getUsername() + ":" + request.getPassword()).getBytes());
                        HttpHeaders headers = new HttpHeaders();
                        headers.add(HttpHeaders.AUTHORIZATION, authValue);
                        return ResponseEntity.ok().headers(headers).body("Login Successful");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                }).defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/register")
    public Mono<ResponseEntity> register(@RequestBody UserLoginRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .flatMap(user -> Mono.error(new RuntimeException("User already exists")))
                .then(Mono.just(new ResponseEntity<>(HttpStatus.CREATED)));
    }
}
