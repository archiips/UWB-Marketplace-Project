package com.dawgs.marketplace.controller;

import com.dawgs.marketplace.model.User;
import com.dawgs.marketplace.repository.UserRepository;
import com.dawgs.marketplace.security.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/auth/google
    // Body: { "idToken": "<Google ID token from frontend>" }
    @PostMapping("/google")
    public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("idToken");
        if (idTokenString == null || idTokenString.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken is required"));
        }

        GoogleIdToken idToken;
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        }

        if (idToken == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        if (!email.endsWith("@uw.edu")) {
            return ResponseEntity.status(403).body(Map.of("error", "Only @uw.edu accounts are allowed"));
        }

        String userId = payload.getSubject();
        String name = (String) payload.get("name");

        // Upsert user
        User user = userRepository.findById(userId).orElse(new User());
        user.setId(userId);
        user.setEmail(email);
        user.setName(name != null ? name : email);
        userRepository.save(user);

        String jwt = jwtUtil.generateToken(userId, email);

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "user", Map.of("id", userId, "email", email, "name", user.getName())
        ));
    }
}
