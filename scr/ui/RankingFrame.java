package ui;

import core.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class RankingFrame extends JFrame {
	private static final long serialVersionUID = 1L;
    private JPanel panel = new JPanel(new BorderLayout());
    private JTable rankTable;
    public DefaultTableModel tableModel;
    private JScrollPane sp;
    private JLabel titleLabel;
    Client c = null;
    
    public RankingFrame(Client _c) {
        c = _c;
        setTitle("전체 랭킹");
        
        // 타이틀 레이블 추가
        titleLabel = new JLabel("랭킹", SwingConstants.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK); // 글씨 색상을 검정으로 설정
        titleLabel.setBorder(new EmptyBorder(15, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        
        // 테이블 설정
        String[] columnNames = {"순위", "닉네임", "승", "패", "승률"};
        tableModel = new DefaultTableModel(columnNames, 0) {
        	private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rankTable = new JTable(tableModel);
        
        // 테이블 스타일링
        rankTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        rankTable.setRowHeight(30);
        rankTable.setShowGrid(true);
        rankTable.setGridColor(new Color(230, 230, 230));
        
        // 테이블 헤더 스타일링
        JTableHeader header = rankTable.getTableHeader();
        header.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        // 컬럼 너비 설정
        rankTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // 순위
        rankTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 닉네임
        rankTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 승
        rankTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // 패
        rankTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 승률
        
        // 셀 가운데 정렬
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
        	private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // 상위 3등 강조
                if (row < 3) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    if (row == 0) setForeground(new Color(255, 215, 0));      // 금
                    else if (row == 1) setForeground(new Color(192, 192, 192));// 은
                    else if (row == 2) setForeground(new Color(205, 127, 50)); // 동
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setForeground(Color.BLACK);
                }
                
                // 순위 열에 메달 이모지 추가
                if (column == 0) {
                    if (row == 0) value = "1위";
                    else if (row == 1) value = "2위";
                    else if (row == 2) value = "3위";
                    else value = String.valueOf(row + 1);
                }
                
                setText(value.toString());
                return c;
            }
        };
        
        // 모든 열에 가운데 정렬 적용
        for (int i = 0; i < rankTable.getColumnCount(); i++) {
            rankTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        sp = new JScrollPane(rankTable);
        sp.setPreferredSize(new Dimension(350, 400));
        sp.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(sp, BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // 새로고침 버튼 추가
        JButton refreshButton = new JButton("새로고침");
        refreshButton.addActionListener(e -> {
            // Client를 통해 서버에 랭킹 데이터 요청
            c.sendMsg("RANK//");
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.setBackground(Color.WHITE);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(panel);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        panel.setBackground(Color.WHITE);
        centerPanel.setBackground(Color.WHITE);
        
        // 초기 데이터 로드
        c.sendMsg("RANK//");
    }
}