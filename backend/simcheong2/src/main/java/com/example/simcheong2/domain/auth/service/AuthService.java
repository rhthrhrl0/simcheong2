package com.example.simcheong2.domain.auth.service;

import com.example.simcheong2.domain.user.entity.dto.UserDTO;
import com.example.simcheong2.domain.user.service.UserCreateService;
import com.example.simcheong2.domain.user.service.UserDeleteService;
import com.example.simcheong2.domain.user.service.UserValidationService;
import com.example.simcheong2.global.exception.model.CustomException;
import com.example.simcheong2.global.exception.model.ErrorCode;
import com.example.simcheong2.global.sms.SmsUtil;
import com.example.simcheong2.global.sms.SmsValidationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserValidationService userValidationService;
    private final UserCreateService userCreateService;
    private final UserDeleteService userDeleteService;

    private final SmsValidationCodeGenerator codeGenerator;
    private final SmsUtil smsUtil;

    private final RedisTemplate<String, String> smsRedisTemplate;

    public void validateSmsCode(String phone, String targetCode) {
        // 이미 등록된 유저 번호는 아닌지 확인
        checkExistUser(phone);
        // 레디스에 등록된 번호가 맞는지 일단 확인
        String code = Optional.ofNullable(smsRedisTemplate.opsForValue().get(phone))
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "해당 휴대폰 번호로 보낸 인증번호가 없습니다. 인증 요청 먼저 해주세요."));
        if (!code.equals(targetCode.trim())) throw new CustomException(ErrorCode.BAD_REQUEST, "인증번호가 일치하지 않습니다.");
        smsRedisTemplate.delete(phone); // 레디스에서 제거
    }

    public void createCode(String phone) {
        checkExistUser(phone);
        isAlreadySendCode(phone);
        sendCode(phone);
    }

    private void checkExistUser(String phone) {
        Optional<UserDTO> user = userValidationService.isPhoneNumberAlreadyRegistered(phone);
        if (user.isPresent()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 가입된 전화번호입니다.");
        }
    }

    private void isAlreadySendCode(String phone) {
        // 레디스에 남아있는 번호는 아닌지 확인.
        if (smsRedisTemplate.opsForValue().get(phone) != null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 인증번호를 발송했습니다. 5분간 유효합니다.");
        }
    }

    private void sendCode(String phone) {
        String code = codeGenerator.generatorCode();
//        smsUtil.sendOne(phone, code); // 하면 진짜 전송됨ㅋㅋ
        log.info("{}에게 {}를 전송함", phone, code);
        smsRedisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
    }

}
