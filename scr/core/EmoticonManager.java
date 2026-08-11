package core;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class EmoticonManager {
    private static EmoticonManager instance;
    private Map<String, ImageIcon> emoticons;
    // 경로 수정
    private static final String EMOTICON_PATH = "resources.emoticons";
    
    private EmoticonManager() {
        emoticons = new HashMap<>();
        loadEmoticons();
    }
    
    private void loadEmoticons() {
        try {
            // 패키지 내의 리소스 목록 가져오기
            URL resourceUrl = getClass().getClassLoader().getResource(EMOTICON_PATH.replace('.', '/'));
            if (resourceUrl == null) {
                throw new Exception("이모티콘 디렉토리를 찾을 수 없습니다.");
            }
            
            File directory = new File(resourceUrl.toURI());
            File[] files = directory.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".png") || 
                name.toLowerCase().endsWith(".gif"));
                
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    URL imageUrl = getClass().getClassLoader().getResource(
                        EMOTICON_PATH.replace('.', '/') + "/" + name
                    );
                    if (imageUrl != null) {
                        ImageIcon icon = new ImageIcon(imageUrl);
                        emoticons.put(name, icon);
                        System.out.println("[EmoticonManager] 로드된 이모티콘: " + name);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[EmoticonManager] 이모티콘 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static EmoticonManager getInstance() {
        if (instance == null) {
            instance = new EmoticonManager();
        }
        return instance;
    }
    
    public ImageIcon getEmoticon(String name) {
        return emoticons.get(name);
    }
    
    public ImageIcon getScaledEmoticon(String name, int width, int height) {
        ImageIcon original = emoticons.get(name);
        if (original != null) {
            Image scaled = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
        return null;
    }
    
    public String[] getEmoticonNames() {
        return emoticons.keySet().toArray(new String[0]);
    }
}