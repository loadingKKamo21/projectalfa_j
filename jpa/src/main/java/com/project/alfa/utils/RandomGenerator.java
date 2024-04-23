package com.project.alfa.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomGenerator {
    
    private static final Random RANDOM    = new Random();
    private static final String NUMS      = "0123456789";
    private static final String UP_CHARS  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOW_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String SP_CHARS  = "`~!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
    
    /**
     * 영문 대/소문자, 숫자 각각 최소 1개 이상으로 이루어진 랜덤 문자열 생성
     * length는 최소 3 이상, 3 미만일 경우 null 반환
     *
     * @param length - 길이
     * @return 생성된 문자열
     */
    public static String randomString(final int length) {
        if (length < 3) return null;
        
        StringBuilder sb = new StringBuilder(length);
        
        int n, u, l;
        do {
            n = RANDOM.nextInt(length - 1) + 1;
            u = RANDOM.nextInt(length - 1) + 1;
            l = RANDOM.nextInt(length - 1) + 1;
        } while (n + u + l != length);
        
        while (n > 0 && n-- != 0) sb.append(NUMS.charAt(RANDOM.nextInt(NUMS.length() - 1)));
        while (u > 0 && u-- != 0) sb.append(UP_CHARS.charAt(RANDOM.nextInt(UP_CHARS.length() - 1)));
        while (l > 0 && l-- != 0) sb.append(LOW_CHARS.charAt(RANDOM.nextInt(LOW_CHARS.length() - 1)));
        
        List<String> chars = Arrays.asList(sb.toString().split(""));
        Collections.shuffle(chars);
        
        return String.join("", chars);
    }
    
    /**
     * 영문 대/소문자, 숫자, 특수문자 각각 최소 1개 이상으로 이루어진 랜덤 문자열(임시 비밀번호) 생성
     * length는 최소 4 이상, 4 미만일 경우 null 반환
     *
     * @param length - 길이
     * @return 생성된 문자열
     */
    public static String randomPassword(final int length) {
        if (length < 4) return null;
        
        StringBuilder sb = new StringBuilder(length);
        
        int n, u, l, s;
        do {
            n = RANDOM.nextInt(length - 1) + 1;
            u = RANDOM.nextInt(length - 1) + 1;
            l = RANDOM.nextInt(length - 1) + 1;
            s = RANDOM.nextInt(length - 1) + 1;
        } while (n + u + l + s != length);
        
        while (n > 0 && n-- != 0) sb.append(NUMS.charAt(RANDOM.nextInt(NUMS.length() - 1)));
        while (u > 0 && u-- != 0) sb.append(UP_CHARS.charAt(RANDOM.nextInt(UP_CHARS.length() - 1)));
        while (l > 0 && l-- != 0) sb.append(LOW_CHARS.charAt(RANDOM.nextInt(LOW_CHARS.length() - 1)));
        while (s > 0 && s-- != 0) sb.append(SP_CHARS.charAt(RANDOM.nextInt(SP_CHARS.length() - 1)));
        
        List<String> chars = Arrays.asList(sb.toString().split(""));
        Collections.shuffle(chars);
        
        return String.join("", chars);
    }
    
    public static int randomNumber(final int min, final int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
    
    public static String randomHangul(final int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append((char) ((Math.random() * 11172) + 0xAC00));
        return sb.toString();
    }
    
}
