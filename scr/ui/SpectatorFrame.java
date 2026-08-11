package ui;

import core.Client;
import core.Room;
import ui.ThemeManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import static core.MessageType.*;
public class SpectatorFrame extends JFrame {
    private JPanel basePanel;
    private JPanel centerPanel;  // 오목판
    private JPanel sidePanel;    // 플레이어 정보, 채팅
    private ThemeManager themeManager;
    private Client client;
    private int[][] omok = new int[20][20];
    private List<String> moveHistory = new ArrayList<>();
    private int currentMoveIndex = 0;
    private javax.swing.Timer historyUpdateTimer;  // 수 기록 업데이트용 타이머 추가
    private JLabel moveCountLabel;  // 필드 추가
    private JTextArea moveLogArea;  // 필드 추가
    private JLabel gameStatusLabel;  // 필드 추가
    private boolean isGameFinished = false;  // 필드 추가

    public SpectatorFrame(Client client) {
        this.client = client;
        this.themeManager = client.mf.themeManager;
        setTitle("오목 게임 - 관전 모드");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setResizable(false);
        initializeUI();
        startHistoryUpdate();
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                client.mf.setVisible(true);
            }
        });
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
        
        // 우측 패널 구성 수정
        sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(260, 620));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        
        // 상단 상태 표시 패널
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        moveCountLabel = new JLabel("0 / 0 수", SwingConstants.CENTER);
        moveCountLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        gameStatusLabel = new JLabel("", SwingConstants.CENTER);
        gameStatusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        gameStatusLabel.setForeground(new Color(220, 53, 69));  // 빨간색
        
        statusPanel.add(moveCountLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(gameStatusLabel);
        
        sidePanel.add(statusPanel);
        sidePanel.add(Box.createVerticalStrut(10));
        
        // 착수 기록 패널
        JPanel moveLogPanel = new JPanel(new BorderLayout());
        moveLogPanel.setBorder(BorderFactory.createTitledBorder("착수 기록"));
        
        moveLogArea = new JTextArea();
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        moveLogArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(moveLogArea);
        scrollPane.setPreferredSize(new Dimension(240, 300));
        
        moveLogPanel.add(scrollPane, BorderLayout.CENTER);
        sidePanel.add(moveLogPanel);
        
        basePanel.add(centerPanel, BorderLayout.CENTER);
        basePanel.add(sidePanel, BorderLayout.EAST);
        
        setContentPane(basePanel);
        applyTheme();
        
        // 하단 컨트롤 패널 제거
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
        
        // 가로 좌표 (A-S) - 상단에만 표시
        for (int i = 0; i < 19; i++) {
            String label = String.valueOf((char)('A' + i));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, 35 + i * 30 - labelWidth/2, 30);  // 위쪽만 표시
        }
        
        // 세로 좌표 (1-19) - 좌측에만 표시
        for (int i = 0; i < 19; i++) {
            String label = String.valueOf(i + 1);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawString(label, 20 - labelWidth/2, 45 + i * 30);  // 왼쪽만 표시
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
        if (currentMoveIndex > 0 && !moveHistory.isEmpty()) {
            String[] lastMove = moveHistory.get(currentMoveIndex - 1).split(",");
            int x = Integer.parseInt(lastMove[0]);
            int y = Integer.parseInt(lastMove[1]);
            
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawRect(23 + x * 30, 23 + y * 30, 29, 29);
        }
    }

    public void updateGameState(String gameState) {
        if (gameState == null || gameState.isEmpty()) {
            System.out.println("[SpectatorFrame] 빈 게임 상태 수신");
            return;
        }

        try {
            String[] states = gameState.split(",");
            int index = 0;
            
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                    if (index < states.length) {
                        omok[i][j] = Integer.parseInt(states[index++]);
                    }
                }
            }
            
            // 화면 갱신
            repaint();
            System.out.println("[SpectatorFrame] 게임 상태 업데이트 완료");
        } catch (Exception e) {
            System.out.println("[SpectatorFrame] 게임 상태 업데이트 실패: " + e.getMessage());
        }
    }

    private void applyTheme() {
        basePanel.setBackground(themeManager.getCurrentMainBgColor());
        centerPanel.setBackground(themeManager.getCurrentMainBgColor());
    }

    public void setMoveHistory(String historyData) {
        moveHistory.clear();
        clearBoard();
        isGameFinished = false;
        gameStatusLabel.setText("");
        
        if (historyData != null && !historyData.isEmpty()) {
            String[] moves = historyData.split(";");
            for (String move : moves) {
                if (!move.isEmpty()) {
                    moveHistory.add(move);
                    String[] moveData = move.split(",");
                    int x = Integer.parseInt(moveData[0]);
                    int y = Integer.parseInt(moveData[1]);
                    int color = Integer.parseInt(moveData[2]);
                    omok[x][y] = color;
                }
            }
            updateMoveLog();  // 착수 기록 업데이트
            System.out.println("[SpectatorFrame] 수 기록 로드 및 표시 완료: " + moveHistory.size() + "수");
        }
        
        currentMoveIndex = moveHistory.size();
        updateMoveCountLabel();
        repaint();
    }

    private void startReplay() {
        // 자동 업데이트 타이머는 더 이상 필요 없으므로 제거
    }

    private void pauseReplay() {
        // 자동 업데이트 타이머는 더 이상 필요 없으므로 제거
    }

    private void showNextMove() {
        if (currentMoveIndex < moveHistory.size()) {
            try {
                String[] moveData = moveHistory.get(currentMoveIndex).split(",");
                int x = Integer.parseInt(moveData[0]);
                int y = Integer.parseInt(moveData[1]);
                int color = Integer.parseInt(moveData[2]);
                omok[x][y] = color;
                currentMoveIndex++;
                repaint();
                System.out.println("[SpectatorFrame] " + currentMoveIndex + "번째 수 표시");
            } catch (Exception e) {
                System.out.println("[SpectatorFrame] 수 표시 오류: " + e.getMessage());
            }
        }
    }

    private void showPreviousMove() {
        if (currentMoveIndex > 0) {
            currentMoveIndex--;
            String[] moveData = moveHistory.get(currentMoveIndex).split(",");
            int x = Integer.parseInt(moveData[0]);
            int y = Integer.parseInt(moveData[1]);
            omok[x][y] = 0;
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

    private void startHistoryUpdate() {
        if (historyUpdateTimer != null) {
            historyUpdateTimer.stop();
        }
        
        // 1초마다 수 기록 요청
        historyUpdateTimer = new javax.swing.Timer(1000, e -> {
            try {
                client.dos.writeUTF(REQUEST_MOVE_HISTORY);
            } catch (IOException ex) {
                System.out.println("[SpectatorFrame] 수 기록 요청 실패: " + ex.getMessage());
            }
        });
        historyUpdateTimer.start();
    }

    // 창이 닫힐 때 타이머 정리
    @Override
    public void dispose() {
        if (historyUpdateTimer != null) {
            historyUpdateTimer.stop();
        }
        client.mf.setVisible(true);  // 메인프레임 보이기
        super.dispose();
    }

    public void addNewMove(int x, int y, int color) {
        if (isGameFinished) return;  // 게임이 이미 종료된 경우 무시
        
        String moveData = x + "," + y + "," + color;
        moveHistory.add(moveData);
        omok[x][y] = color;
        currentMoveIndex = moveHistory.size();
        
        // 승리 조건 체크
        if (checkWin(x, y, color)) {
            isGameFinished = true;
            String winner = (color == 1) ? "흑" : "백";
            gameStatusLabel.setText(winner + " 승리!");
            System.out.println("[SpectatorFrame] 게임 종료: " + winner + " 승리");
        }
        
        updateMoveLog();
        updateMoveCountLabel();
        repaint();
    }

    private boolean checkWin(int x, int y, int color) {
        // 8방향 체크를 위한 방향 배열
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
                
                // 한 방향으로 연속된 같은 색 돌 카운트
                while (true) {
                    nx += dx;
                    ny += dy;
                    if (nx < 0 || nx >= 19 || ny < 0 || ny >= 19 || omok[nx][ny] != color) {
                        break;
                    }
                    count++;
                }
            }
            
            if (count >= 5) return true;  // 5개 이상 연속되면 승리
        }
        return false;
    }

    private void updateMoveCountLabel() {
        if (moveCountLabel != null) {
            moveCountLabel.setText(currentMoveIndex + " / " + moveHistory.size() + " 수");
        }
    }

    private void updateMoveLog() {
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            String[] moveData = moveHistory.get(i).split(",");
            int x = Integer.parseInt(moveData[0]);
            int y = Integer.parseInt(moveData[1]);
            int color = Integer.parseInt(moveData[2]);
            
            // 알파벳과 숫자로 좌표 변환
            char col = (char)('A' + x);
            int row = y + 1;
            
            String colorStr = (color == 1) ? "흑" : "백";
            log.append(String.format("%3d수: %s %c%d\n", i + 1, colorStr, col, row));
        }
        moveLogArea.setText(log.toString());
        
        // 스크롤을 가장 아래로 이동
        moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
    }
} 