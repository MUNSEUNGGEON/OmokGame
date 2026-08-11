package ui;

import DB.Database;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.io.File;

public class AdminFrame {
    private JFrame adminFrame;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private Database database;
    private JTextField idField, nameField, nicknameField, pwField, emailField, phoneField, postalField, addressField, detailAddressField;
    private JRadioButton maleRadio, femaleRadio;
    private JLabel profilePictureLabel;
    private JButton uploadImageButton, viewButton, editButton, deleteButton, togglePasswordButton, closeButton;
    private boolean showPassword = false; // 비밀번호 표시 여부
    private JComboBox<String> searchTypeCombo;
    private JTextField searchField;
    private JPanel detailsPanel;  // 상세 정보 패널
    private JSplitPane splitPane;
    private boolean isDetailsPanelVisible = false;  // 상세 패널 표시 상태

    // 색상 상수 정의
    private static final Color PRIMARY_COLOR = new Color(51, 122, 183);      // 기본 기능 (조회, 추가)
    private static final Color WARNING_COLOR = new Color(217, 83, 79);       // 주의 기능 (삭제)
    private static final Color SECONDARY_COLOR = new Color(91, 102, 113);    // 부가 기능 (로그, 비밀번호)
    private static final Color NEUTRAL_COLOR = new Color(108, 117, 125);     // 기타 기능 (닫기)

    // 버튼 크기 최적화
    private static final Dimension BUTTON_SIZE = new Dimension(100, 30);  // 버튼 기본 크기 축소

    public AdminFrame() {
        database = new Database();
        openAdminPage();
    }

    private void openAdminPage() {
        adminFrame = new JFrame("관리자 페이지");
        adminFrame.setSize(1500, 700);  // 가로 크기를 1500으로 증가
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setBackground(Color.WHITE);

        // 테이블 디자인 개선
        String[] columnNames = {"ID", "이름", "닉네임", "PW", "이메일", "전화번호", "우편번호", "주소", "성별"};
        tableModel = new DefaultTableModel(columnNames, 0);
        userTable = new JTable(tableModel);
        
        // 테이블 스타일링
        userTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        userTable.setRowHeight(25);
        userTable.setGridColor(new Color(230, 230, 230));
        userTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));
        userTable.getTableHeader().setBackground(new Color(240, 240, 240));
        userTable.getTableHeader().setForeground(Color.BLACK);
        userTable.setSelectionBackground(new Color(232, 242, 254));
        userTable.setShowVerticalLines(true);
        userTable.setShowHorizontalLines(true);

        // 스크롤팬 스타일링
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(Color.WHITE); // 배경색 설정

        // 상단 버튼 패널 디자인
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 10));  // 컴포넌트 간 간격을 5로 축소
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        // 검색 컴포넌트 생성 및 크기 조정
        String[] searchTypes = {"닉네임", "이름", "아이디", "성별", "전체 조회"};
        searchTypeCombo = new JComboBox<>(searchTypes);
        searchTypeCombo.setPreferredSize(new Dimension(90, 30));
        searchField = new JTextField(12);  // 텍스트 필드 크기 축소
        searchField.setPreferredSize(new Dimension(120, 30));
        JButton searchButton = createStyledButton("검색", PRIMARY_COLOR);
        searchButton.setPreferredSize(new Dimension(70, 30));

        // 버저 검색 컴포넌트 추가
        buttonPanel.add(searchTypeCombo);
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(Box.createHorizontalStrut(20));  // 구분자

        // 버튼들 생성 및 크기 조정
        JButton addUserButton = createStyledButton("회원 추가", PRIMARY_COLOR);
        viewButton = createStyledButton("전체 조회", PRIMARY_COLOR);
        editButton = createStyledButton("수정", PRIMARY_COLOR);
        deleteButton = createStyledButton("삭제", WARNING_COLOR);
        togglePasswordButton = createStyledButton("비밀번호 보기", SECONDARY_COLOR);
        closeButton = createStyledButton("닫기", NEUTRAL_COLOR);
        
        JButton gameLogButton = createStyledButton("게임 기록", SECONDARY_COLOR);
        JButton chatLogButton = createStyledButton("채팅 기록", SECONDARY_COLOR);
        JButton deletedUsersButton = createStyledButton("회원 복원", WARNING_COLOR);

        // 버튼들 추가
        buttonPanel.add(addUserButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));  // 구분자
        buttonPanel.add(togglePasswordButton);
        buttonPanel.add(Box.createHorizontalStrut(20));  // 구분자
        buttonPanel.add(gameLogButton);
        buttonPanel.add(chatLogButton);
        buttonPanel.add(deletedUsersButton);
        buttonPanel.add(Box.createHorizontalStrut(20));  // 구분자
        buttonPanel.add(closeButton);

        // 상세 정보 버튼 추가
        JButton showDetailsButton = createStyledButton("회원 상세", PRIMARY_COLOR);
        buttonPanel.add(showDetailsButton);

        // 프레임에 버튼 패널 추가
        adminFrame.add(buttonPanel, BorderLayout.NORTH);
        
        // 상세 정보 패널 생성
        detailsPanel = createDetailsPanel();
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // SplitPane 설정
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScrollPane, detailsPanel);
        splitPane.setDividerLocation(1500);  // 처음에는 상세 패널이 보이지 않도록 설정
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);

        // 프레임에 컴포넌트 추가
        adminFrame.add(splitPane, BorderLayout.CENTER);
        adminFrame.setVisible(true);

        loadUserData();
        userTable.getSelectionModel().addListSelectionListener(e -> showSelectedUserDetails());

        // 이벤트 핸들러 등록
        addUserButton.addActionListener(new AddUserButtonListener());
        viewButton.addActionListener(new ViewButtonListener());
        editButton.addActionListener(new EditButtonListener());
        deleteButton.addActionListener(new DeleteButtonListener());
        togglePasswordButton.addActionListener(new TogglePasswordListener());
        closeButton.addActionListener(e -> adminFrame.dispose());
        gameLogButton.addActionListener(e -> showGameLogDialog());
        chatLogButton.addActionListener(e -> showChatLogDialog());
        deletedUsersButton.addActionListener(e -> showDeletedUsersDialog());
        searchButton.addActionListener(e -> performSearch());
        showDetailsButton.addActionListener(e -> toggleDetailsPanel());
    }

    // 스타일된 버튼 생성 메소드 수정
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(BUTTON_SIZE);  // 모든 버튼에 동일한 크기 적용
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 호버 효과
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    // 상세 정보 패널 스타일링 수정
    private JPanel createDetailsPanel() {
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH; // 필드가 가로 및 세로로 채우도록 설정
        gbc.weightx = 1.0; // 필드의 가로 확장 비율
        gbc.weighty = 0.5; // 필드의 세로 확장 비율

        Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 12);
        
        // 각 필드에 대한 스타일 적용
        Component[] fields = {idField, nameField, nicknameField, pwField, emailField, 
                             phoneField, postalField, addressField, detailAddressField};
        
        for (Component field : fields) {
            if (field instanceof JTextField) {
                JTextField textField = (JTextField) field;
                textField.setFont(fieldFont);
                textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        }

        // 필드 생성
        idField = new JTextField();
        idField.setEditable(false);
        nameField = new JTextField();
        nicknameField = new JTextField();
        pwField = new JTextField();
        pwField.setEditable(false); // 기본적으로 읽기 전용으로 설정
        emailField = new JTextField();
        phoneField = new JTextField();
        postalField = new JTextField(20);
        addressField = new JTextField(20);
        detailAddressField = new JTextField(20);

        maleRadio = new JRadioButton("남성");
        femaleRadio = new JRadioButton("여성");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        profilePictureLabel = new JLabel();
     // 프로필 이미지 레이블의 크기를 더 키우기
        profilePictureLabel.setPreferredSize(new Dimension(180, 120)); // 크기 조정
        profilePictureLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        uploadImageButton = new JButton("이미지 업로드");

        new JButton("수정");
        new JButton("탈퇴");
        // GridBagConstraints를 사용하여 컴포넌트 배치
        gbc.gridx = 0;
        gbc.gridy = 0;
        detailsPanel.add(new JLabel("아이디:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        detailsPanel.add(new JLabel("이름:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        detailsPanel.add(new JLabel("닉네임:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(nicknameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        detailsPanel.add(new JLabel("비밀번호:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(pwField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        detailsPanel.add(new JLabel("이메일:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        detailsPanel.add(new JLabel("전화번호:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        detailsPanel.add(new JLabel("성별:"), gbc);
        JPanel genderPanel = new JPanel();
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        gbc.gridx = 1;
        detailsPanel.add(genderPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        detailsPanel.add(new JLabel("우편번호:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(postalField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        detailsPanel.add(new JLabel("주소:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        detailsPanel.add(new JLabel("상세 주소:"), gbc);
        gbc.gridx = 1;
        detailsPanel.add(detailAddressField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        detailsPanel.add(profilePictureLabel, gbc);
        gbc.gridheight = 1;
        gbc.gridy = 4;
        detailsPanel.add(uploadImageButton, gbc);



        uploadImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".jpg") ||
                               f.getName().toLowerCase().endsWith(".png") ||
                               f.getName().toLowerCase().endsWith(".gif") ||
                               f.isDirectory();
                    }
                    public String getDescription() {
                        return "Image Files (*.jpg, *.png, *.gif)";
                    }
                });

                int result = fileChooser.showOpenDialog(adminFrame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        byte[] imageBytes = Files.readAllBytes(selectedFile.toPath());
                        
                        // 이미지 크기 조정
                        ImageIcon originalIcon = new ImageIcon(imageBytes);
                        Image originalImage = originalIcon.getImage();
                        // 이미지 업로드 시 크기 조정
                        Image resizedImage = originalImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                        
                        // 프로필 이미지 업데이트
                        profilePictureLabel.setIcon(new ImageIcon(resizedImage));
                        
                        // DB 업데이트
                        String userId = idField.getText();
                        if (!userId.isEmpty()) {
                            String query = "UPDATE Users SET profile_picture = ? WHERE id = ?";
                            PreparedStatement pstmt = database.con.prepareStatement(query);
                            pstmt.setBytes(1, imageBytes);
                            pstmt.setString(2, userId);
                            pstmt.executeUpdate();
                            
                            JOptionPane.showMessageDialog(adminFrame, 
                                "프로필 이미지가 업데이트되었습니다.",
                                "업데이트 성���",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(adminFrame,
                            "이미지 업로드 중 오류가 발생했습니다.",
                            "업로드 실패",
                            JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            }
        });

        return detailsPanel;
    }

    // 모든 회원 데이터를 로드하여 테이블에 표시
    private void loadUserData() {
        tableModel.setRowCount(0);  // 테이블 초기화
        String query = "SELECT id, name, nickname, password, email, phone, postal, address, gender, deleted " +
                      "FROM Users WHERE deleted = 0 " +  // 삭제되지 않은 회원만 조회
                      "ORDER BY id";

        try (PreparedStatement pstmt = database.con.prepareStatement(query)) {
            ResultSet result = pstmt.executeQuery();

            while (result.next()) {
                String status = "활성";  // deleted = 0인 회원만 조회하므로 항상 "활성" 상태
                tableModel.addRow(new Object[]{
                    result.getString("id"),
                    result.getString("name"),
                    result.getString("nickname"),
                    showPassword ? result.getString("password") : "********",
                    result.getString("email"),
                    result.getString("phone"),
                    result.getString("postal"),
                    result.getString("address"),
                    result.getString("gender"),
                    status
                });
            }

            result.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(adminFrame, "회원 정보를 로드하는  오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 상세 정보 패널 토글 메소드
    private void toggleDetailsPanel() {
        isDetailsPanelVisible = !isDetailsPanelVisible;
        if (isDetailsPanelVisible) {
            splitPane.setDividerLocation(900);  // 상세 패널이 보이도록 분할 위치 조정
        } else {
            splitPane.setDividerLocation(1500);  // 상세 패널이 숨겨지도록 분할 위치 조정
        }
    }

    // 선택된 회원 정보 표시 메소드 수정
    private void showSelectedUserDetails() {
        if (!isDetailsPanelVisible) {
            splitPane.setDividerLocation(900);  // 상세 패널이 보이도록 분할 위치 조정
            isDetailsPanelVisible = true;
        }
        
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) tableModel.getValueAt(selectedRow, 0);
            try {
                String query = "SELECT * FROM Users WHERE id=?";
                PreparedStatement pstmt = database.con.prepareStatement(query);
                pstmt.setString(1, userId);
                ResultSet result = pstmt.executeQuery();

                if (result.next()) {
                    idField.setText(result.getString("id"));
                    nameField.setText(result.getString("name"));
                    nicknameField.setText(result.getString("nickname"));
                    pwField.setText(showPassword ? result.getString("password") : "********");
                    emailField.setText(result.getString("email"));
                    phoneField.setText(result.getString("phone"));
                    postalField.setText(result.getString("postal"));
                    addressField.setText(result.getString("address"));
                    detailAddressField.setText(result.getString("detail_address"));

                    String gender = result.getString("gender");
                    if ("남성".equals(gender)) {
                        maleRadio.setSelected(true);
                    } else {
                        femaleRadio.setSelected(true);
                    }

                    byte[] profileImageBytes = result.getBytes("profile_picture");
                    if (profileImageBytes != null && profileImageBytes.length > 0) {
                        ImageIcon profileIcon = new ImageIcon(profileImageBytes);
                        Image img = profileIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                        profilePictureLabel.setIcon(new ImageIcon(img));
                    } else {
                        profilePictureLabel.setIcon(null);
                    }
                }

                result.close();
                pstmt.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(adminFrame, "회원 정보를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // "비밀번호 보기/숨기기" 버튼 기능
    private class TogglePasswordListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            showPassword = !showPassword;
            togglePasswordButton.setText(showPassword ? "비밀번호 숨기기" : "비밀번호 보기");
            loadUserData(); // 테이블의 비밀번호 필드 업데이트
        }
    }

    // 각각의 리스너 클래스들 가
    private class ViewButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadUserData();
        }
    }

    private class EditButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userId = idField.getText();
            if (!userId.isEmpty()) {
                try {
                    String gender = maleRadio.isSelected() ? "남성" : "여성";
                    String query = "UPDATE Users SET name=?, nickname=?, password=?, email=?, phone=?, gender=?, postal=?, address=?, detail_address=? WHERE id=?";
                    PreparedStatement pstmt = database.con.prepareStatement(query);
                    pstmt.setString(1, nameField.getText());
                    pstmt.setString(2, nicknameField.getText());
                    pstmt.setString(3, pwField.getText());
                    pstmt.setString(4, emailField.getText());
                    pstmt.setString(5, phoneField.getText());
                    pstmt.setString(6, gender);
                    pstmt.setString(7, postalField.getText());
                    pstmt.setString(8, addressField.getText());
                    pstmt.setString(9, detailAddressField.getText());
                    pstmt.setString(10, userId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(adminFrame, "회원 정보가 수정되었습니다.");
                    loadUserData();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(adminFrame, "회원 정보 수정 실패", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(adminFrame, "수정할 회원을 선택하세요.");
            }
        }
    }

    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) tableModel.getValueAt(selectedRow, 0);
                String userName = (String) tableModel.getValueAt(selectedRow, 1);
                
                // 삭제 확인 다이얼로그
                int confirm = JOptionPane.showConfirmDialog(
                    adminFrame,
                    userName + " 회원을 삭제하시겠습니까?",
                    "회원 삭제",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        // 회원 삭제 처리 (deleted = 1로 설정)
                        String query = "UPDATE Users SET deleted = 1, delete_date = CURRENT_TIMESTAMP WHERE id = ?";
                        PreparedStatement pstmt = database.con.prepareStatement(query);
                        pstmt.setString(1, userId);
                        int result = pstmt.executeUpdate();
                        
                        if (result > 0) {
                            JOptionPane.showMessageDialog(
                                adminFrame,
                                "회원이 삭제되었습니다.",
                                "삭제 완료",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            loadUserData(); // 전체 회원 목록 새로고침
                            clearDetailsPanel(); // 상세 정보 패널 초기화
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(
                            adminFrame,
                            "회원 삭제 중 오류가 발생했습니다.",
                            "제 실패",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                    adminFrame,
                    "삭제할 회원을 선택하세요.",
                    "선택 필요",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    // 상세 정보 패널 초기화 메소드 추가
    private void clearDetailsPanel() {
        idField.setText("");
        nameField.setText("");
        nicknameField.setText("");
        pwField.setText("");
        emailField.setText("");
        phoneField.setText("");
        postalField.setText("");
        addressField.setText("");
        detailAddressField.setText("");
        maleRadio.setSelected(false);
        femaleRadio.setSelected(false);
        profilePictureLabel.setIcon(null);
    }

    // 회원 추가 다이얼로그 및 리스너 구현
    private class AddUserButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Database 객체를 전달하여 JoinFrame 생성
            JoinFrame joinFrame = new JoinFrame(null);
            joinFrame.setVisible(true);
            
            // JoinFrame이 닫힐 때 회원 목록을 새로고침
            joinFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    loadUserData();  // 회원 목록 새로고침
                }
            });
        }
    }

    // 게임 기록 다이얼로그
    private void showGameLogDialog() {
        JDialog gameLogDialog = new JDialog(adminFrame, "게임 기록", true);
        gameLogDialog.setSize(800, 500);
        gameLogDialog.setLocationRelativeTo(adminFrame);
        
        String[] columns = {"닉네임", "승", "패", "승률", "최근 게임"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable gameLogTable = new JTable(model);
        
        try {
            String query = "SELECT nickname, win, lose, " +
                          "ROUND(DECODE(win + lose, 0, 0, (win / (win + lose)) * 100), 2) as winrate " +
                          "FROM Users ORDER BY winrate DESC";
            PreparedStatement pstmt = database.con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getString("nickname"),
                    rs.getInt("win"),
                    rs.getInt("lose"),
                    rs.getDouble("winrate") + "%",
                    "-"  // 최근 게임 시간은 필요시 추가
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        gameLogDialog.add(new JScrollPane(gameLogTable));
        gameLogDialog.setVisible(true);
    }

    // 채팅 로그 다이얼로그
    private void showChatLogDialog() {
        JDialog chatLogDialog = new JDialog(adminFrame, "채팅 기록", true);
        chatLogDialog.setSize(800, 500);
        chatLogDialog.setLocationRelativeTo(adminFrame);
        
        String[] columns = {"시간", "보낸 사람", "받는 사람", "메시지"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable chatLogTable = new JTable(model);
        
        // 검 패널
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("검색");
        searchPanel.add(new JLabel("사용자 검색:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // 채팅 로그 로드 함수
        Runnable loadChatLogs = () -> {
            try {
                String searchText = searchField.getText().trim();
                StringBuilder query = new StringBuilder(
                    "SELECT ch.timestamp, ch.sender, ch.receiver, ch.message_text " +
                    "FROM ChatMessages ch WHERE 1=1");
                
                if (!searchText.isEmpty()) {
                    query.append(" AND (ch.sender LIKE ? OR ch.receiver LIKE ?)");
                }
                query.append(" ORDER BY ch.timestamp DESC");
                
                PreparedStatement pstmt = database.con.prepareStatement(query.toString());
                
                if (!searchText.isEmpty()) {
                    String searchPattern = "%" + searchText + "%";
                    pstmt.setString(1, searchPattern);
                    pstmt.setString(2, searchPattern);
                }
                
                ResultSet rs = pstmt.executeQuery();
                model.setRowCount(0);  // 테이블 초기화
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getTimestamp("timestamp"),
                        rs.getString("sender"),
                        rs.getString("receiver"),
                        rs.getString("message_text")
                    };
                    model.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        };
        
        // 검색 버튼 이벤트
        searchButton.addActionListener(e -> loadChatLogs.run());
        
        // 초기 데이터 로드
        loadChatLogs.run();
        
        // 레이아웃 설정
        chatLogDialog.setLayout(new BorderLayout());
        chatLogDialog.add(searchPanel, BorderLayout.NORTH);
        chatLogDialog.add(new JScrollPane(chatLogTable), BorderLayout.CENTER);
        chatLogDialog.setVisible(true);
    }

    // 삭제된 회원 관리 다이얼로그
    private void showDeletedUsersDialog() {
        JDialog deletedUsersDialog = new JDialog(adminFrame, "회원 복원", true);
        deletedUsersDialog.setSize(1000, 400);
        deletedUsersDialog.setLocationRelativeTo(adminFrame);
        
        // 테이블 설정
        String[] columns = {"ID", "이름", "닉네임", "이메일", "전화번호", "삭제일"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false; // 테이블 수정 불가
            }
        };
        JTable deletedUsersTable = new JTable(model);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton restoreButton = new JButton("회원 복원");
        JButton refreshButton = new JButton("새로고침");
        buttonPanel.add(restoreButton);
        buttonPanel.add(refreshButton);
        
        // 삭제된 회원 데이터 로드 함수
        Runnable loadDeletedUsers = () -> {
            model.setRowCount(0);
            try {
                String query = "SELECT id, name, nickname, email, phone, delete_date " +
                              "FROM Users WHERE deleted = 1 " +
                              "ORDER BY delete_date DESC";
                PreparedStatement pstmt = database.con.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("nickname"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getTimestamp("delete_date")
                    };
                    model.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(deletedUsersDialog, 
                    "삭제된 회원 목록을 불러오는 중 오류가 발생했습니다.", 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
            }
        };
        
        // 복원 버튼 이벤트
        restoreButton.addActionListener(e -> {
            int selectedRow = deletedUsersTable.getSelectedRow();
            if (selectedRow != -1) {
                String userId = (String) model.getValueAt(selectedRow, 0);
                try {
                    String query = "UPDATE Users SET deleted = 0, delete_date = NULL WHERE id = ?";
                    PreparedStatement pstmt = database.con.prepareStatement(query);
                    pstmt.setString(1, userId);
                    int result = pstmt.executeUpdate();
                    
                    if (result > 0) {
                        JOptionPane.showMessageDialog(deletedUsersDialog,
                            "복원이 복원되었습니다.",
                            "복원 완료",
                            JOptionPane.INFORMATION_MESSAGE);
                        loadDeletedUsers.run(); // 테이블 새로고침
                        loadUserData(); // 메인 테이블도 새로고침
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(deletedUsersDialog,
                        "회원 복원 중 오류가 발생했습니다.",
                        "복원 실패",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(deletedUsersDialog,
                    "복원할 회원을 선택하세요.",
                    "선택 필요",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // 새로고침 버튼 이벤트
        refreshButton.addActionListener(e -> loadDeletedUsers.run());
        
        // 초기 데이터 로드
        loadDeletedUsers.run();
        
        // 레이아웃 설정
        deletedUsersDialog.setLayout(new BorderLayout());
        deletedUsersDialog.add(new JScrollPane(deletedUsersTable), BorderLayout.CENTER);
        deletedUsersDialog.add(buttonPanel, BorderLayout.SOUTH);
        deletedUsersDialog.setVisible(true);
    }

    // 검색 수행 메소드 수정
    private void performSearch() {
        String searchType = (String) searchTypeCombo.getSelectedItem();
        String searchText = searchField.getText().trim();
        
        tableModel.setRowCount(0);  // 테이블 초기화
        
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT id, name, nickname, email, phone, postal, address, gender, deleted ");
            queryBuilder.append("FROM Users WHERE deleted = 0 ");
            
            if (!searchText.isEmpty() && !searchType.equals("전체 조회")) {
                switch (searchType) {
                    case "닉네임":
                        queryBuilder.append("AND nickname LIKE ? ");
                        break;
                    case "이름":
                        queryBuilder.append("AND name LIKE ? ");
                        break;
                    case "아이디":
                        queryBuilder.append("AND id LIKE ? ");
                        break;
                    case "성별":
                        queryBuilder.append("AND gender LIKE ? ");
                        break;
                }
            }
            
            queryBuilder.append("ORDER BY id");
            
            PreparedStatement pstmt = database.con.prepareStatement(queryBuilder.toString());
            
            if (!searchText.isEmpty() && !searchType.equals("전체 조회")) {
                // 성별 검색의 경우 확한 매칭을 위해 LIKE 패턴을 다르게 적용
                if (searchType.equals("성별")) {
                    pstmt.setString(1, searchText); // 정확한 매칭
                } else {
                    pstmt.setString(1, "%" + searchText + "%"); // 부분 매칭
                }
            }
            
            ResultSet result = pstmt.executeQuery();
            
            while (result.next()) {
                tableModel.addRow(new Object[]{
                    result.getString("id"),
                    result.getString("name"),
                    result.getString("nickname"),
                    "********",
                    result.getString("email"),
                    result.getString("phone"),
                    result.getString("postal"),
                    result.getString("address"),
                    result.getString("gender")
                });
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(adminFrame, 
                    "검색 결과가 없습니다.", 
                    "검색 결과", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(adminFrame, 
                "검색 중 오류가 발생했습니다.", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
