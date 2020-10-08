package com.jiajun.practice.reactor.webflux;

import com.google.common.collect.ImmutableMap;
import com.jiajun.practice.reactor.webflux.util.FutureUtil;
import com.jiajun.practice.reactor.webflux.util.MonoUtil;
import com.ximalaya.common.auth.common.subject.WebfluxUserContext;
import com.ximalaya.service.album.api.service.IRemoteAsyncIAlbumFacadeHandler;
import com.ximalaya.service.content.model.DetailedAlbum;
import com.ximalaya.service.profile.model.DetailUserInfo;
import com.ximalaya.service.profile.service.impl.ThriftRemoteAsyncRemoteUserInfoQueryService;
import com.ximalaya.track.cell.api.model.DetailTrackDto;
import com.ximalaya.track.cell.api.service.impl.ThriftRemoteAsyncTrackCellProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author jiajun
 */
@Slf4j
@RestController
public class RemoteInvokeController {

    @Resource
    private ThriftRemoteAsyncRemoteUserInfoQueryService userInfoQueryService;

    @Resource
    private ThriftRemoteAsyncTrackCellProxyService trackCellProxyService;

    @Resource
    private IRemoteAsyncIAlbumFacadeHandler albumFacadeHandler;

    /**
     * 简单的接口聚合
     *
     * @param uidOptional
     * @param trackId
     * @return
     */
    @GetMapping("free/reactor")
    public Mono<Object> reactor(
            @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uidOptional,
            @RequestParam Long trackId
    ) {
        Mono<DetailTrackDto> trackInfoMono = MonoUtil.from(trackCellProxyService.queryDetailTrackInfo(trackId));

        Mono<Optional<DetailUserInfo>> userInfoOptional = uidOptional.map(uid ->
                MonoUtil.from(userInfoQueryService.getDetailUserInfo(uid)).map(Optional::of)
        ).orElse(Mono.just(Optional.empty()));

        Mono<Boolean> evictTrackDraft = MonoUtil.from(FutureUtil.voidMap(trackCellProxyService.evictTrackDraftsCache(123L), Boolean.TRUE));
        return Mono.zip(trackInfoMono, userInfoOptional, evictTrackDraft).map(tuple -> {
                    DetailTrackDto trackInfo = tuple.getT1();
                    Optional<DetailUserInfo> userInfo = tuple.getT2();
                    Boolean evict = tuple.getT3();
                    return ImmutableMap.of(
                            "trackInfo", trackInfo,
                            "userInfo", userInfo.orElseGet(DetailUserInfo::new),
                            "evict", evict
                    );
                }
        );
    }

    /**
     * 带超时
     *
     * @param uidOptional
     * @param trackId
     * @return
     */
    @GetMapping("free/reactor/timeout")
    public Mono<Object> timeoutReactor(
            @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uidOptional,
            @RequestParam Long trackId
    ) {
        long endTs = System.currentTimeMillis() + 80;

        Mono<Optional<DetailTrackDto>> trackInfoMono = MonoUtil.optionalFrom(endTs, () -> trackCellProxyService.queryDetailTrackInfo(trackId));

        Mono<Optional<DetailUserInfo>> userInfoOptional = uidOptional
                .map(uid -> MonoUtil.optionalFrom(endTs, () -> userInfoQueryService.getDetailUserInfo(uid)))
                .orElse(Mono.just(Optional.empty()));

        return Mono.zip(trackInfoMono, userInfoOptional).map(tuple -> {
                    Optional<DetailTrackDto> trackInfo = tuple.getT1();
                    Optional<DetailUserInfo> userInfo = tuple.getT2();
                    return ImmutableMap.of(
                            "trackInfo", trackInfo.orElseGet(DetailTrackDto::new),
                            "userInfo", userInfo.orElseGet(DetailUserInfo::new)
                    );
                }
        );
    }

    /**
     * 接口总体超时800ms
     *
     * @param uidOptional
     * @param trackId
     * @return
     */
    @GetMapping("free/reactor/timeout/v2")
    public Mono<Object> timeoutReactorV2(
            @RequestAttribute(value = WebfluxUserContext.USER_ID) Optional<Long> uidOptional,
            @RequestParam Long trackId
    ) {
        long endTime = System.currentTimeMillis() + 30;

        // 并行
        Mono<DetailTrackDto> trackInfoMono = MonoUtil.from(endTime, () -> trackCellProxyService.queryDetailTrackInfo(trackId))
                .doOnError(throwable -> log.error("call trackCellProxyService#queryDetailTrackInfo({}) error. {}", trackId, throwable.getMessage()));
        Mono<Optional<DetailUserInfo>> userInfoOptional = uidOptional
                .map(uid -> MonoUtil.optionalFrom(endTime, () -> userInfoQueryService.getDetailUserInfo(uid)))
                .orElse(Mono.just(Optional.empty()));

        // 串行: trackId -> albumId -> albumInfo
        Mono<Optional<DetailedAlbum>> albumOptional = trackInfoMono.flatMap(trackDto ->
                MonoUtil.optionalFrom(endTime, () -> albumFacadeHandler.queryDetailedAlbum(trackDto.getAlbumId()))
        );

        return Mono.zip(trackInfoMono, userInfoOptional, albumOptional).map(tuple -> {
                    DetailTrackDto trackInfo = tuple.getT1();
                    Optional<DetailUserInfo> userInfo = tuple.getT2();
                    Optional<DetailedAlbum> album = tuple.getT3();
                    return ImmutableMap.of(
                            "trackInfo", trackInfo,
                            "userInfo", userInfo.orElseGet(DetailUserInfo::new),
                            "album", album.orElseGet(DetailedAlbum::new)
                    );
                }
        );
    }
}
