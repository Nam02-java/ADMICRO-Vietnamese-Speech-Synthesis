import java.io.IOException;

import java.io.*;

import java.nio.file.Files;
import java.nio.file.Paths;


public class FFmpegMerger {
    public static void main(String[] args) {
        String ffmpegPath = "E:\\CongViecHocTap\\ffmpeg\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
        String videoPath = "E:\\CongViecHocTap\\Voice\\audio_2025_02_14_15_03_19_1739520199.0357783.mp4";
        String imagePath = "E:\\CongViecHocTap\\Picture\\picture.png";
        String outputPath = "E:\\CongViecHocTap\\UploadVideo\\";

        // Đảm bảo outputPath có dấu '\'
        if (!outputPath.endsWith("\\")) {
            outputPath += "\\";
        }

        String baseFileName = "ADMICR - Vietnamese Speech Synthesis";
        String fileExtension = ".mp4";
        String fileName = getAvailableFileNameForUploadVideo(outputPath, baseFileName, fileExtension);

        // Lệnh FFmpeg
        String command = String.format("\"%s\" -loop 1 -i \"%s\" -i \"%s\" -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest \"%s\"",
                ffmpegPath, imagePath, videoPath, fileName);

        System.out.println("Chạy lệnh: " + command);

        try {
            Process process = Runtime.getRuntime().exec(command);

            // Đọc output của FFmpeg để debug
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("✅ Hoàn tất ghép ảnh vào video! File xuất ra: " + fileName);
            } else {
                System.out.println("⚠️ FFmpeg gặp lỗi, kiểm tra output trên.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static String getAvailableFileNameForUploadVideo(String directoryPath, String baseFileName, String extension) {
        int fileNumber = 1;
        String fileName = directoryPath + baseFileName + "(" + fileNumber + ")" + extension;

        while (Files.exists(Paths.get(fileName))) {
            fileNumber++;
            fileName = directoryPath + baseFileName + "(" + fileNumber + ")" + extension;
        }
        return fileName;
    }
}


