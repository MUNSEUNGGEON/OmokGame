package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ReplayFrame extends JFrame {
    private JPanel basePanel;
    private JPanel centerPanel;  // 오목판
    private int[][] omok = new int[20][20];
    private List<String> moveHistory = new ArrayList<>();
    private int currentMoveIndex = 0;
    private JLabel moveCountLabel;  // 필드 추가
    private JSlider speedSlider;  // 필드 추가ㄴ
    private static final int DEFAULT_DELAY = 1000;  // 1초
    private boolean isGameFinished = false;  // 필드 추가
    private JLabel gameStatusLabel;  // 필드 추가

    public ReplayFrame(String replayData) {
        setTitle("대국 복기");
        setSize(650, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        initializeUI();
        setMoveHistory(replayData);
    }

    private void initializeUI() {
        basePanel = new JPanel(new BorderLayout());
        centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawStones(g);
            }
        };
        centerPanel.setPreferredSize(new Dimension(620, 620));
        
        // 컨트롤 패널 개선
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(620, 50));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 재생 속도 슬라이더 초기화
        speedSlider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 1000);
        speedSlider.setInverted(true);  // 왼쪽이 빠르게
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        
        JPanel speedPanel = new JPanel();
        speedPanel.add(new JLabel("재생 속도: "));
        speedPanel.add(speedSlider);
        
        JButton prevBtn = new JButton("이전");
        JButton nextBtn = new JButton("다음");
        JButton autoPlayBtn = new JButton("자동 재생");
        
        // 버튼 크기 통일
        Dimension buttonSize = new Dimension(100, 30);
        prevBtn.setPreferredSize(buttonSize);
        nextBtn.setPreferredSize(buttonSize);
        autoPlayBtn.setPreferredSize(buttonSize);
        
        prevBtn.addActionListener(e -> showPreviousMove());
        nextBtn.addActionListener(e -> showNextMove());
        autoPlayBtn.addActionListener(e -> toggleAutoPlay());
        
        controlPanel.add(prevBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(autoPlayBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(nextBtn);
        controlPanel.add(Box.createHorizontalStrut(20));
        
        moveCountLabel = new JLabel("0 / 0 수", SwingConstants.CENTER);
        moveCountLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        controlPanel.add(moveCountLabel);
        controlPanel.add(speedPanel);  // 속도 조절 패널 추가
        
        // 게임 상태 표시 레이블 추가
        gameStatusLabel = new JLabel("", SwingConstants.CENTER);
        gameStatusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        gameStatusLabel.setForeground(new Color(220, 53, 69));  // 빨간색
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(gameStatusLabel);
        controlPanel.add(Box.createHorizontalStrut(20));
        
        basePanel.add(centerPanel, BorderLayout.CENTER);
        basePanel.add(controlPanel, BorderLayout.SOUTH);
        setContentPane(basePanel);
    }

    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 바둑판 배경 그리기 (정사각형으로 수정)
        g2d.setColor(new Color(206, 167, 61));  // 나무 색상
        g2d.fillRect(10, 10, 600, 600);  // 위치와 크기 조정
        
        // 격자 선 그리기
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // 세로선
        for (int i = 0; i < 19; i++) {
            int x = 40 + i * 30;  // 시작점 조정
            g2d.drawLine(x, 40, x, 580);  // 선 길이 조정
        }
        
        // 가로선
        for (int i = 0; i < 19; i++) {
            int y = 40 + i * 30;  // 시작점 조정
            g2d.drawLine(40, y, 580, y);  // 선 길이 조정
        }
        
        // 화점(바둑판의 점) 그리기
        g2d.setColor(Color.BLACK);
        int[] starPoints = {3, 9, 15};  // 화점 위치 조정
        for (int i : starPoints) {
            for (int j : starPoints) {
                g2d.fillOval(37 + i * 30, 37 + j * 30, 6, 6);  // 위치와 크기 조정
            }
        }
        
        // 좌표 표시
        g2d.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        // 가로 좌표 (A-S)
        for (int i = 0; i < 19; i++) {
            g2d.drawString(String.valueOf((char)('A' + i)), 35 + i * 30, 30);
        }
        
        // 세로 좌표 (1-19)
        for (int i = 0; i < 19; i++) {
            g2d.drawString(String.valueOf(i + 1), 20, 45 + i * 30);
        }
    }

    private void drawStones(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (omok[i][j] > 0) {
                    // 그림자 효과
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval(25 + i * 30, 25 + j * 30, 25, 25);
                    
                    // 돌 그리기
                    if (omok[i][j] == 1) {  // 흑돌
                        GradientPaint gp = new GradientPaint(
                            25 + i * 30, 25 + j * 30, Color.DARK_GRAY,
                            50 + i * 30, 50 + j * 30, Color.BLACK
                        );
                        g2d.setPaint(gp);
                    } else {  // 백돌
                        GradientPaint gp = new GradientPaint(
                            25 + i * 30, 25 + j * 30, Color.WHITE,
                            50 + i * 30, 50 + j * 30, Color.LIGHT_GRAY
                        );
                        g2d.setPaint(gp);
                    }
                    g2d.fillOval(25 + i * 30, 25 + j * 30, 25, 25);
                }
            }
        }
        
        // 마지막 착수 위치 표시
        if (currentMoveIndex > 0) {
            String[] lastMove = moveHistory.get(currentMoveIndex - 1).split(",");
            int x = Integer.parseInt(lastMove[0]);
            int y = Integer.parseInt(lastMove[1]);
            
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawRect(23 + x * 30, 23 + y * 30, 29, 29);
        }
    }

    public void setMoveHistory(String historyData) {
        isGameFinished = false;
        gameStatusLabel.setText("");
        moveHistory.clear();
        clearBoard();  // 보드 초기화
        
        if (historyData != null && !historyData.isEmpty()) {
            // 대괄호 제거 및 공백 제거
            historyData = historyData.replaceAll("[\\[\\]]", "").trim();
            String[] moves = historyData.split(", ");
            
            for (String move : moves) {
                if (!move.isEmpty()) {
                    moveHistory.add(move);
                }
            }
            System.out.println("[ReplayFrame] 수 기록 로드 완료: " + moveHistory.size() + "수");
        }
        
        currentMoveIndex = 0;  // 처음부터 시작
        updateMoveCountLabel();
        repaint();
    }

    private void showNextMove() {
        if (currentMoveIndex < moveHistory.size()) {
            String moveData = moveHistory.get(currentMoveIndex);
            
            try {
                String[] parts = moveData.split(",");
                if (parts.length >= 3) {
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int color = Integer.parseInt(parts[2]);
                    omok[x][y] = color;
                    currentMoveIndex++;
                    
                    // 마지막 수인 경우 승리 판정
                    if (currentMoveIndex == moveHistory.size()) {
                        if (checkWin(x, y, color)) {
                            isGameFinished = true;
                            String winner = (color == 1) ? "흑" : "백";
                            gameStatusLabel.setText("★ " + winner + " 승리! ★");
                            gameStatusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
                            gameStatusLabel.setForeground(new Color(220, 53, 69));  // 빨간색
                            
                            // 승리 메시지 다이얼로그 표시
                            JOptionPane.showMessageDialog(this, 
                                winner + " 승리!\n게임이 종료되었습니다.", 
                                "게임 종료", 
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    
                    repaint();
                    updateMoveCountLabel();
                }
            } catch (NumberFormatException e) {
                System.err.println("Failed to parse move data: " + moveData);
                e.printStackTrace();
            }
        }
    }

    private void showPreviousMove() {
        if (currentMoveIndex > 0) {
            currentMoveIndex--;
            String[] moveData = moveHistory.get(currentMoveIndex).split(",");
            int x = Integer.parseInt(moveData[0]);
            int y = Integer.parseInt(moveData[1]);
            omok[x][y] = 0;  // 돌 제거
            repaint();
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                omok[i][j] = 0;
            }
        }
    }

    private Timer autoPlayTimer;
    private boolean isAutoPlaying = false;

    private void toggleAutoPlay() {
        if (isAutoPlaying) {
            stopAutoPlay();
        } else {
            startAutoPlay();
        }
    }

    private void startAutoPlay() {
        if (isGameFinished) {
            return;
        }
        
        int delay = (speedSlider != null) ? speedSlider.getValue() : DEFAULT_DELAY;
        
        if (autoPlayTimer == null) {
            autoPlayTimer = new Timer(delay, e -> {
                if (currentMoveIndex < moveHistory.size()) {
                    showNextMove();
                } else {
                    stopAutoPlay();
                }
            });
        }
        autoPlayTimer.setDelay(delay);
        autoPlayTimer.start();
        isAutoPlaying = true;
    }

    private void stopAutoPlay() {
        if (autoPlayTimer != null) {
            autoPlayTimer.stop();
        }
        isAutoPlaying = false;
    }

    private void updateMoveCountLabel() {
        if (moveCountLabel != null) {
            moveCountLabel.setText(currentMoveIndex + " / " + moveHistory.size() + " 수");
        }
    }

    // 승리 판정을 위한 메서드 추가
    private boolean checkWin(int x, int y, int color) {
        int[][] directions = {
            {1,0}, {0,1}, {1,1}, {1,-1}  // 가로, 세로, 대각선
        };
        
        for (int[] dir : directions) {
            int count = 1;  // 현재 위치 포함
            
            // 양방향으로 체크
            for (int i = -1; i <= 1; i += 2) {
                int dx = dir[0] * i;
                int dy = dir[1] * i;
                int nx = x;
                int ny = y;
                
                while (true) {
                    nx += dx;
                    ny += dy;
                    if (nx < 0 || nx >= 19 || ny < 0 || ny >= 19 || omok[nx][ny] != color) {
                        break;
                    }
                    count++;
                }
            }
            
            if (count >= 5) return true;
        }
        return false;
    }
} 