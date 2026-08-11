package sound;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class BackgroundMusic {
    private Clip clip;
    private FloatControl volumeControl;
    private boolean isPlaying = false;
    private float currentVolume = -20.0f; // 기본 볼륨값 (범위: -80.0f ~ 6.0f)
    private String musicPath;

    public BackgroundMusic(String musicPath) {
        this.musicPath = musicPath;
        initializeMusic();
    }

    private void initializeMusic() {
        try {
            File musicFile = new File(musicPath);
            if (!musicFile.exists()) {
                System.out.println("[Error] 음악 파일을 찾을 수 없습니다: " + musicPath);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            
            // 볼륨 컨트롤 설정
            volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            setVolume(currentVolume);
            
            // 음악 반복 재생 설정
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            
        } catch (UnsupportedAudioFileException e) {
            System.out.println("[Error] 지원하지 않는 오디오 파일 형식입니다: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[Error] 음악 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("[Error] 오디오 라인을 사용할 수 없습니다: " + e.getMessage());
        }
    }

    public void play() {
        if (clip != null && !isPlaying) {
            clip.setMicrosecondPosition(0); // 처음부터 재생
            clip.start();
            isPlaying = true;
            System.out.println("[BGM] 배경음악 재생 시작");
        }
    }

    public void stop() {
        if (clip != null && isPlaying) {
            clip.stop();
            isPlaying = false;
            System.out.println("[BGM] 배경음악 정지");
        }
    }

    public void pause() {
        if (clip != null && isPlaying) {
            clip.stop();
            isPlaying = false;
            System.out.println("[BGM] 배경음악 일시정지");
        }
    }

    public void resume() {
        if (clip != null && !isPlaying) {
            clip.start();
            isPlaying = true;
            System.out.println("[BGM] 배경음악 재개");
        }
    }

    public void setVolume(float volume) {
        if (volumeControl != null) {
            // 볼륨 범위 제한 (-80.0f ~ 6.0f)
            volume = Math.max(-80.0f, Math.min(6.0f, volume));
            currentVolume = volume;
            volumeControl.setValue(volume);
            System.out.println("[BGM] 볼륨 설정: " + volume);
        }
    }

    public void volumeUp() {
        setVolume(currentVolume + 5.0f);
    }

    public void volumeDown() {
        setVolume(currentVolume - 5.0f);
    }

    public float getVolume() {
        return currentVolume;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void close() {
        if (clip != null) {
            clip.stop();
            clip.close();
            isPlaying = false;
            System.out.println("[BGM] 배경음악 리소스 해제");
        }
    }

    // 현재 재생 위치를 처음으로 되돌림
    public void resetPosition() {
        if (clip != null) {
            clip.setMicrosecondPosition(0);
        }
    }

    // 음악 파일 변경
    public void changeMusic(String newMusicPath) {
        if (clip != null) {
            clip.close();
        }
        this.musicPath = newMusicPath;
        initializeMusic();
        System.out.println("[BGM] 배경음악 변경: " + newMusicPath);
    }
} 