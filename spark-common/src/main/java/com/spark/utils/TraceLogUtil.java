package com.spark.utils;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2025/10/22 12:29
 */
public class TraceLogUtil {
    public static final String TRACK_ID_KEY = "TrackId";
    public static final String USER_ID_KEY = "UserId";

    /**
     * 构造trackId
     * @param trackId
     * @return
     */
    public static String generateTrackId(String trackId) {
        if(StringUtil.isBlank(trackId)) {
            trackId = UUID.randomUUID().toString().replaceAll("-","");
        }
        MDC.put(TRACK_ID_KEY, trackId);
        return trackId;
    }

    public static String getTrackId() {
        return MDC.get(TRACK_ID_KEY);
    }

    public static void removeTrackId() {
        MDC.remove(TRACK_ID_KEY);
    }

    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    public static void cacheTrackUserId(Long userId) {
        if(userId == null) {
            return;
        }
        MDC.put(USER_ID_KEY, String.valueOf(userId));
    }

    public static void removeUserId() {
        MDC.remove(USER_ID_KEY);
    }
}
