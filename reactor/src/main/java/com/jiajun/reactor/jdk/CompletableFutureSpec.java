package com.jiajun.reactor.jdk;

import com.ximalaya.futureconverter.java8.CompletionDependentUtil;
import org.junit.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * JDK9中对CompletableFuture进行了增强
 *
 * @author jiajun
 */
public class CompletableFutureSpec {

    @Test
    public void completionStack() {
        CompletableFuture<String> albumFuture = CompletableFuture.supplyAsync(() -> {
            sleep(Duration.ofMillis(100));
            return "this is album info";
        });
        CompletableFuture<String> recTrackIdsFuture = albumFuture.thenCompose(s -> CompletableFuture.supplyAsync(() -> {
            sleep(Duration.ofMillis(500));
            return "this is recTrack ids";
        }));
        CompletableFuture<String> recTrackInfoFuture = recTrackIdsFuture.thenCompose(s -> CompletableFuture.supplyAsync(() -> {
            sleep(Duration.ofMillis(1000));
            return "this is recTrack info";
        }));

        albumFuture.thenAccept(album -> {
            sleep(Duration.ofMillis(1500));
            System.out.println(album);
        });
        recTrackIdsFuture.thenRun(() -> {
            sleep(Duration.ofMillis(1500));
            System.out.println("recTrackIdsFuture done!");
        });
        recTrackIdsFuture.thenRun(() -> System.out.println("recTrackIdsFuture done!"));
        recTrackInfoFuture.thenRun(() -> System.out.println("recTrackInfoFuture done!"));

        CompletableFuture[] futures = CompletionDependentUtil.dependentLeaf(albumFuture).toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(futures).thenRun(() -> System.out.println("all of the future done!"));

        sleep(Duration.ofMillis(10000));
    }

    /**
     * 测试applyToEither场景
     */
    @Test
    public void either() {

    }

    @Test
    public void httpClient() {
        System.setProperty("jdk.internal.httpclient.debug", "true");
        HttpClient httpClient = HttpClient.newBuilder()
                //.executor(Runnable::run) // 指定work线程池
                .build();
        CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(
                HttpRequest.newBuilder().uri(URI.create("http://www.baidu.com")).build(),
                HttpResponse.BodyHandlers.ofString()
        );
        responseFuture.thenAccept(response -> System.out.println(response.body()));

        sleep(Duration.ofSeconds(2));
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
