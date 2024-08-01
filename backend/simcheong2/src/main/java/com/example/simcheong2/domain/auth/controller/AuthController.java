package com.example.simcheong2.domain.auth.controller;

import com.example.simcheong2.domain.auth.controller.request.*;
import com.example.simcheong2.domain.auth.controller.response.SmsCheckResponse;
import com.example.simcheong2.domain.auth.controller.response.TokenResponse;
import com.example.simcheong2.domain.auth.entity.Tokens;
import com.example.simcheong2.domain.auth.entity.dto.LoginDto;
import com.example.simcheong2.domain.auth.entity.dto.LogoutDto;
import com.example.simcheong2.domain.auth.entity.dto.ReissueDto;
import com.example.simcheong2.domain.auth.service.AuthService;
import com.example.simcheong2.domain.user.entity.dto.UserSaveDTO;
import com.example.simcheong2.domain.user.service.UserCreateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "로그인 관련 API")
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserCreateService userCreateService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginDto loginDto = new LoginDto(request.getId(), request.getPassword());

        Tokens tokens = authService.login(loginDto);
        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestBody @Valid LogoutRequest request) {
        String accessToken = request.getAccessToken();
        LogoutDto logoutDto = new LogoutDto(accessToken);
        authService.logout(logoutDto);
        return ResponseEntity.ok(true);
    }

    // 코드 검사
    @PostMapping("/sms-validation")
    public ResponseEntity<SmsCheckResponse> checkCode(@RequestBody @Valid SmsValidationRequest request) {
        String sessionId = authService.validateSmsCode(request.getPhone(), request.getCode());
        return ResponseEntity.ok(new SmsCheckResponse(sessionId));
    }

    // 코드 생성 요청
    @PostMapping("/sms-code")
    public ResponseEntity<Boolean> createCode(@RequestBody @Valid SmsCheckRequest request) {
        authService.createCode(request.getPhone());
        return ResponseEntity.ok(true);
    }

    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@RequestBody @Valid SignupRequest request) {
        UserSaveDTO userSaveDTO = new UserSaveDTO(
                request.getId(),
                request.getPassword(),
                request.getEmail(),
                request.getName(),
                request.getNickname(),
                request.getOpeningDate(),
                request.getPhone(),
                request.getIsForeign(),
                request.getIsDisabled(),
                request.getSex(),
                request.getSessionId()
        );
        userCreateService.signUp(userSaveDTO);
        return ResponseEntity.ok(true);
    }

    // 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody @Valid ReissueRequest request) {
        ReissueDto reissueDto = new ReissueDto(request.getRefreshToken());
        Tokens tokens = authService.reissue(reissueDto);
        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(),tokens.getRefreshToken()));
    }

}
