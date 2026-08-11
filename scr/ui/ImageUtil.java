package ui;

//파일: ImageUtil.java
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageUtil {
 // 이미지 파일 경로를 받아 byte 배열로 변환
 public static byte[] convertImageToBytes(String imagePath) {
     File imageFile = new File(imagePath);
     byte[] imageData = null;

     try (FileInputStream fis = new FileInputStream(imageFile)) {
         imageData = fis.readAllBytes();
     } catch (IOException e) {
         System.out.println("이미지 변환 실패: " + e.getMessage());
     }

     return imageData;
 }
}
