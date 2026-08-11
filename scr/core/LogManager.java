package core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class LogManager {
    private static LogManager instance;
    private final Set<String> recentLogs = new HashSet<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    private LogManager() {}
    
    public static LogManager getInstance() {
        if (instance == null) {
            instance = new LogManager();
        }
        return instance;
    }
    
    public void log(String tag, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String fullMessage = String.format("[%s] [%s] %s", timestamp, tag, message);
        
        // 중복 체크 - 태그와 메시지만 비교 (시간 제외)
        String messageWithoutTime = String.format("[%s] %s", tag, message);
        
        if (!recentLogs.contains(messageWithoutTime)) {
            System.out.println(fullMessage);
            recentLogs.add(messageWithoutTime);
            
            // 메모리 관리: 일정 크기 이상이면 초기화
            if (recentLogs.size() > 1000) {
                recentLogs.clear();
            }
        }
    }
    
    public void clearLogs() {
        recentLogs.clear();
    }
} 