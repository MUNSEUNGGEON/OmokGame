package ui.signup;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import config.AppConfig;

public class AddressSearchFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextField queryField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextField postalField;
    private JTextField addressField;

    // API 키
    private static final String PUBLIC_DATA_API_KEY = AppConfig.require("OMOK_POSTAL_API_KEY");
    private static final String PUBLIC_DATA_API_URL = AppConfig.optional(
            "OMOK_POSTAL_API_URL",
            "https://openapi.epost.go.kr/postal/retrieveNewAdressAreaCdSearchAllService/retrieveNewAdressAreaCdSearchAllService/getNewAddressListAreaCdSearchAll");

    // 생성자
    public AddressSearchFrame(JTextField postalField, JTextField addressField) {
        this.postalField = postalField;
        this.addressField = addressField;

        setTitle("주소찾기");
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 상단 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        queryField = new JTextField();
        inputPanel.add(queryField, BorderLayout.CENTER);

        JButton searchButton = new JButton("검색");
        searchButton.addActionListener(e -> searchAddress());
        inputPanel.add(searchButton, BorderLayout.EAST);

        // 결과 테이블
        String[] columnNames = {"No", "도로명 주소", "우편번호"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultTable = new JTable(tableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                int row = resultTable.getSelectedRow();
                if (row != -1) {
                    String selectedAddress = tableModel.getValueAt(row, 1).toString();
                    String postalCode = tableModel.getValueAt(row, 2).toString();

                    // 선택된 주소와 우편번호를 필드에 설정
                    postalField.setText(postalCode);
                    addressField.setText(selectedAddress);
                    dispose(); // 창 닫기
                }
            }
        });

        // 열 너비 조정
        setColumnWidths(resultTable);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("검색 결과"));

        // 전체 레이아웃 구성
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setColumnWidths(JTable table) {
        TableColumn column = table.getColumnModel().getColumn(0); // "No" 열
        column.setPreferredWidth(5); // 숫자 열 너비 조정
        column = table.getColumnModel().getColumn(1); // "도로명 주소" 열
        column.setPreferredWidth(380); // 도로명 주소 열 너비
        column = table.getColumnModel().getColumn(2); // "우편번호" 열
        column.setPreferredWidth(40); // 우편번호 열 너비
    }

    private void searchAddress() {
        String query = queryField.getText();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "주소를 입력하세요.");
            return;
        }

        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String apiUrl = PUBLIC_DATA_API_URL + "?serviceKey=" + PUBLIC_DATA_API_KEY + "&srchwrd=" + encodedQuery + "&countPerPage=10&currentPage=1";

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();

            // XML 응답 처리
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(response.toString())));

            NodeList zipNoList = document.getElementsByTagName("zipNo");
            NodeList lnmAdresList = document.getElementsByTagName("lnmAdres");

            tableModel.setRowCount(0); // 기존 데이터 초기화
            if (zipNoList.getLength() > 0 && lnmAdresList.getLength() > 0) {
                for (int i = 0; i < zipNoList.getLength(); i++) {
                    String postalCode = zipNoList.item(i).getTextContent().trim();
                    String address = lnmAdresList.item(i).getTextContent().trim();
                    tableModel.addRow(new Object[]{i + 1, address, postalCode});
                }
            } else {
                JOptionPane.showMessageDialog(this, "주소를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "주소 검색 중 오류가 발생했습니다. 다시 시도해 주세요.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddressSearchFrame frame = new AddressSearchFrame(new JTextField(), new JTextField());
            frame.setVisible(true);
        });
    }
}
