package ui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;
import config.AppConfig;

public class WeatherWidget extends JPanel {
    private static final String API_KEY = AppConfig.require("OMOK_OPENWEATHER_API_KEY");
    private static final String LANGUAGE = "kr"; // 언어 설정

    private JLabel weatherLabel;
    private JLabel temperatureLabel;
    private JLabel imageLabel;
    private JTextField locationField;
    private JButton searchButton;
    private JLabel instructionLabel; // 안내 문구 레이블 추가

    // 한글 지역명과 영어 지역명 매핑
    private Map<String, String> locationMap;

    public WeatherWidget() {
        setLayout(null); // 절대 위치 사용
        setPreferredSize(new Dimension(373, 268)); // 기본 크기 설정

        // 한글 지역명과 영어 지역명 매핑 초기화
        initializeLocationMap();

        // 날씨 정보 컴포넌트
        weatherLabel = new JLabel();
        temperatureLabel = new JLabel();
        imageLabel = new JLabel();
        locationField = new JTextField(10); // 지역 입력 필드
        searchButton = new JButton("검색"); // 검색 버튼

        // 안내 문구 레이블 추가
        instructionLabel = new JLabel("* 지역을 검색하세요 *");
        instructionLabel.setForeground(Color.GRAY); // 색상 설정
        instructionLabel.setVisible(true); // 초기에는 보이도록 설정

        // 컴포넌트 위치 설정
        weatherLabel.setBounds(10, 10, 100, 20); // 위치 및 크기 설정
        temperatureLabel.setBounds(10, 30, 100, 20);
        imageLabel.setBounds(80, 10, 40, 40);
        locationField.setBounds(10, 10, 70, 25);
        searchButton.setBounds(90, 10, 60, 20);
        instructionLabel.setBounds(10, 35, 150, 20); // 안내 문구 위치 설정

        // 컴포넌트 추가
        add(weatherLabel);
        add(temperatureLabel);
        add(imageLabel);
        add(locationField);
        add(searchButton);
        add(instructionLabel);

        // 버튼 클릭 이벤트 처리
        searchButton.addActionListener(e -> {
            String location = locationField.getText().trim(); // 공백 제거
            String englishLocation = locationMap.get(location);
            String actualLocation = (englishLocation != null) ? englishLocation : location;

            // 영어 입력일 경우 직접 사용
            if (englishLocation == null && !isEnglish(location)) {
                JOptionPane.showMessageDialog(this, "유효한 지역을 입력하세요.");
                return;
            }

            String weatherData = getWeather(actualLocation);
            if (weatherData != null) {
                String condition = parseWeatherData(weatherData);
                weatherLabel.setText("날씨: " + condition);
            }
            // 검색 후 텍스트 필드와 버튼, 안내 문구 숨기기
            locationField.setVisible(false);
            searchButton.setVisible(false);
            instructionLabel.setVisible(false); // 안내 문구 숨김
        });
    }
    
    public String parseWeatherData(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        int temperature = (int) jsonObject.getJSONObject("main").getDouble("temp");
        String condition = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description");

        // 날씨 상태에 따라 이미지 파일 설정
        String imagePath = "";
        if (condition.contains("clear") || condition.contains("맑음")) {
            imagePath = "./Weather_img/맑음.png";
        } else if (condition.contains("cloud") || condition.contains("흐림")) {
            imagePath = "./Weather_img/cloudy-day.png";
        } else if (condition.contains("rain") || condition.contains("비")) {
            imagePath = "./Weather_img/비.png";
        } else if (condition.contains("snow") || condition.contains("눈")) {
            imagePath = "./Weather_img/snow.png";
        } else {
            imagePath = "./Weather_img/cloudy-day.png"; // 기본 이미지
        }

        // 기온 및 이미지 설정
        temperatureLabel.setText("기온: " + temperature + "°C");

        File imgFile = new File(imagePath);
        if (!imgFile.exists()) {
            System.out.println("이미지 파일이 존재하지 않습니다: " + imagePath);
        } else {
            System.out.println("이미지 파일이 존재합니다: " + imagePath);
            try {
                ImageIcon weatherIcon = new ImageIcon(imagePath);
                Image image = weatherIcon.getImage(); // 원본 이미지 가져오기
                Image scaledImage = image.getScaledInstance(40, 40, Image.SCALE_SMOOTH); // 원하는 크기로 조정
                imageLabel.setIcon(new ImageIcon(scaledImage)); // 조정된 이미지를 JLabel에 설정
                System.out.println("이미지 로드 성공: " + imagePath);
            } catch (Exception e) {
                System.out.println("이미지 로드 실패: " + e.getMessage());
            }
        }
        return condition; // 날씨 상태 반환
    }

    private void initializeLocationMap() {
        locationMap = new HashMap<>();
        locationMap.put("서울", "Seoul,KR");
        locationMap.put("부산", "Busan,KR");
        locationMap.put("대구", "Daegu,KR");
        locationMap.put("인천", "Incheon,KR");
        locationMap.put("광주", "Gwangju,KR");
        locationMap.put("대전", "Daejeon,KR");
        locationMap.put("울산", "Ulsan,KR");
        locationMap.put("제주", "Jeju,KR");
        locationMap.put("안양", "Anyang,KR");
        locationMap.put("수원", "Suwon,KR");
        locationMap.put("성남", "Seongnam,KR");
        // 다른 지역 추가 가능
    }

    private boolean isEnglish(String location) {
        return location.matches("^[a-zA-Z\\s]+$"); // 영어 알파벳 및 공백으로 구성된 경우
    }

    public String getWeather(String location) {
        String urlString = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&lang=%s&units=metric", location, API_KEY, LANGUAGE);
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Server returned HTTP response code: " + responseCode);
            }

            Scanner scanner = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();

            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return response.toString(); // JSON 응답 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("날씨 정보");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setResizable(true); // 크기 조정 가능하게 설정
        frame.getContentPane().add(new WeatherWidget());
        frame.setVisible(true);
    }
}


