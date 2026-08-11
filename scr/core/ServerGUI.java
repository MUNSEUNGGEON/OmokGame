package core;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.BindException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Field;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JButton clearButton;
    private JButton startClientButton;
    private JLabel statusLabel;
    private JLabel connectedUsersLabel;
    private JLabel roomCountLabel;
    private Server server;
    private boolean isServerRunning = false;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private Thread serverThread;
    private Timer statsTimer;
    private int clientCount = 0;
    private static final int MAX_CLIENTS = 10; // 최대 클라이언트 수 제한

    public ServerGUI() {
        super("서버실행");
        
        // UTF-8 인코딩 설정
        System.setProperty("file.encoding", "UTF-8");
        Charset.defaultCharset(); // 기본 문자셋 초기화
        
        // 로그 영역에 대한 인코딩 설정
        logArea = new JTextArea();
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        setTitle("오목 게임 서버/클라이언트 관리자");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 메인 패널
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 상단 패널 (상태 + 통계)
        JPanel topPanel = new JPanel(new BorderLayout());
        
        // 상태 패널
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("서버 상태: 중지됨");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        statusPanel.add(statusLabel);
        
        // 통계 패널
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        connectedUsersLabel = new JLabel("접속자 수: 0");
        roomCountLabel = new JLabel("방 개수: 0");
        connectedUsersLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        roomCountLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        statsPanel.add(connectedUsersLabel);
        statsPanel.add(new JLabel(" | "));
        statsPanel.add(roomCountLabel);

        topPanel.add(statusPanel, BorderLayout.WEST);
        topPanel.add(statsPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 로그 영역
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        ((javax.swing.text.DefaultCaret)logArea.getCaret())
            .setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("서버 로그"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        startButton = createStyledButton("서버 시작", new Color(46, 204, 113));
        stopButton = createStyledButton("서버 중지", new Color(231, 76, 60));
        clearButton = createStyledButton("로그 지우기", new Color(189, 195, 199));
        startClientButton = createStyledButton("클라이언트 실행", new Color(189, 195, 199));
        
        stopButton.setEnabled(false);
        startClientButton.setEnabled(false); // 서버 시작 전에는 비활성화

        buttonPanel.add(startClientButton);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 이벤트 리스너 추가
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        clearButton.addActionListener(e -> clearLog());
        startClientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewClient();
            }
        });

        // 윈도우 종료 이벤트
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isServerRunning) {
                    stopServer();
                }
            }
        });

        add(mainPanel);

        // System.out을 로그 영역으로 리다이렉트
        redirectSystemOut();

        // 통계 업데이트 타이머 설정
        setupStatsTimer();
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        button.setOpaque(true);
        button.setBorderPainted(false);
        
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor, 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                    BorderFactory.createEmptyBorder(5, 15, 5, 15)
                ));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    private void setupStatsTimer() {
        statsTimer = new Timer(5000, e -> updateStats()); // 5초마다 업데이트
        statsTimer.start();
    }

    private void updateStats() {
        if (isServerRunning && server != null) {
            SwingUtilities.invokeLater(() -> {
                int userCount = server.getAllUsers() != null ? server.getAllUsers().size() : 0;
                int roomCount = server.getRooms() != null ? server.getRooms().size() : 0;
                connectedUsersLabel.setText("접속자 수: " + userCount);
                roomCountLabel.setText("방 개수: " + roomCount);
            });
        }
    }

    private void startServer() {
        if (!isServerRunning) {
            serverThread = new Thread(() -> {
                try {
                    server = new Server();
                    addLog("서버를 시작합니...");
                    server.initialize();
                    isServerRunning = true;
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("서버 상태: 실행 중");
                        statusLabel.setForeground(new Color(34, 139, 34));
                        startButton.setEnabled(false);
                        stopButton.setEnabled(true);
                        startClientButton.setEnabled(true); // 서버 시작 후 활성화
                    });
                } catch (BindException e) {
                    addLog("포트가 이미 사용 중입니다. 서버를 시작할 수 없습니다.");
                    resetServerState();
                } catch (Exception e) {
                    addLog("서버 시작 중 오류 발생: " + e.getMessage());
                    resetServerState();
                }
            });
            serverThread.start();
        }
    }

    private void stopServer() {
        if (isServerRunning && server != null) {
            try {
                server.stopServer();
                addLog("서버를 종료합니다...");
                
                if (serverThread != null && serverThread.isAlive()) {
                    serverThread.interrupt();
                    try {
                        serverThread.join(5000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                addLog(" 중지되었습니다.");
                resetServerState();
                
                // 클라이언트 카운트 초기화
                clientCount = 0;
            } catch (Exception e) {
                addLog("서버 중지 중 오류 발생: " + e.getMessage());
            }
        }
    }

    private void resetServerState() {
        isServerRunning = false;
        server = null;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("서버 상태: 중지됨");
            statusLabel.setForeground(Color.RED);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            startClientButton.setEnabled(false); // 서버 중지 시 비활성화
            connectedUsersLabel.setText("접속자 수: 0");
            roomCountLabel.setText("방 개수: 0");
        });
    }

    private void clearLog() {
        logArea.setText("");
    }

    private void addLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = timeFormat.format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void redirectSystemOut() {
        try {
            PrintStream printStream = new PrintStream(
                new OutputStream() {
                    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    @Override
                    public void write(int b) throws IOException {
                        baos.write(b);
                        if (b == '\n') {
                            String line = new String(baos.toByteArray(), StandardCharsets.UTF_8);
                            SwingUtilities.invokeLater(() -> {
                                String timestamp = timeFormat.format(new Date());
                                logArea.append("[" + timestamp + "] " + line);
                            });
                            baos.reset();
                        }
                    }
                },
                true,
                StandardCharsets.UTF_8
            );
            System.setOut(printStream);
        } catch (Exception e) {
            System.err.println("로그 리다이렉션 실패: " + e.getMessage());
            addLog("로그 리다이렉션 실패: " + e.getMessage());
        }
    }

    private void startNewClient() {
        if (clientCount < MAX_CLIENTS) {
            try {
                // 새로운 스레드에서 클라이언트 실행
                new Thread(() -> {
                    try {
                        String[] args = new String[0];
                        Client.main(args);  // Client의 main 메서드 직접 호출
                        clientCount++;
                        addLog("새로운 클라이언트가 실행되었습니다. (총 " + clientCount + "개)");
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                ServerGUI.this,
                                "클라이언트 실행 중 오류가 발생했습니다: " + ex.getMessage(),
                                "오류",
                                JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }
                }).start();
            } catch (Exception ex) {
                addLog("클라이언트 실행 실패: " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "최대 클라이언트 수(" + MAX_CLIENTS + "개)에 도달했습니다.",
                "경고",
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    @Override
    public void dispose() {
        if (statsTimer != null) {
            statsTimer.stop();
        }
        if (isServerRunning) {
            stopServer();
        }
        super.dispose();
    }

    public static void main(String[] args) {
        // VM 옵션 설정
        System.setProperty("file.encoding", "UTF-8");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}