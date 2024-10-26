package com.example.simcheong2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@SpringBootTest
class Simcheong2ApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {

            }

            return "Hello";
        });

        CompletableFuture<String> mangKyu = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000L);
            } catch (Exception e) {

            }
            return "MangKyu";
        });

        List<CompletableFuture<String>> futures = List.of(hello, mangKyu);

        System.out.println("start: " + getDateTime());
        CompletableFuture<List<String>> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream().
                        map(CompletableFuture::join). // join으로 가져오는건 결과가 없으면 논블럭킹이고 아직 결과 없으면 예외다.
                        collect(Collectors.toList()));
        System.out.println("mid: " + getDateTime());
        Thread.sleep(3000);
        result.get().forEach(System.out::println);
        System.out.println("end: " + getDateTime());
    }

    private String getDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        return formattedNow;
    }

}
