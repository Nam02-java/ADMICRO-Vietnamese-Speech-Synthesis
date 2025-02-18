import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Function;

public class SeleniumRemote {
    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        String chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
        String userDataDir = "C:\\selenium_chrome";
        String command = chromePath + " --remote-debugging-port=9222 --user-data-dir=" + userDataDir+ " --headless"; //+ " --headless"

        Process process = Runtime.getRuntime().exec(command);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");
        WebDriver driver = new ChromeDriver(options);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
            }
        });


        driver.get("https://chivi.app/wn/books/254cuyB25ea/_read/c1?tl=mtl_2&lp=off&ed=off");

        Thread.sleep(5000);


        String pageSource = driver.getPageSource();

        Document doc = Jsoup.parse(pageSource);
        System.out.println("Title: " + doc.title());

        String content = doc.select("#svelte > div.tm-light.rd-ff-0.rd-fs-3.svelte-19vhflx > main > article:nth-child(5)").text(); // Lấy nội dung trong thẻ có id="P0"
        System.out.println("Nội dung trang: " + content);

        String directoryPath = "E:\\CongViecHocTap\\Content\\";
        String baseFileName = "content";
        String fileExtension = ".txt";
        String fileName = getAvailableFileName(directoryPath, baseFileName, fileExtension);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(directoryPath + fileName))) {
            writer.write(content);
        }

        driver.get("https://speech.aiservice.vn/tts/tools/demo");
        Thread.sleep(5000);

        String fileContent = readFileContent(directoryPath + fileName);

        WebElement textArea = driver.findElement(By.id("edit-content"));
        textArea.sendKeys(fileContent);

        Thread.sleep(5000);

        driver.findElement(By.id("submit_btn")).click();
        Thread.sleep(15000);

        String audioUrl = driver.findElement(By.id("audio")).getAttribute("src");
        System.out.println("Audio URL: " + audioUrl);

        downloadAudio(audioUrl);

        createVideo();

        driver.quit();
        process.destroy();

    }

    private static void createVideo() {
        String ffmpegPath = "E:\\CongViecHocTap\\ffmpeg\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
        String voiceDir = "E:\\CongViecHocTap\\Voice";
        String imagePath = "E:\\CongViecHocTap\\Picture\\picture.png";
        String outputPath = "E:\\CongViecHocTap\\UploadVideo\\";

        // Lấy file video mới nhất
        String videoPath = getLatestFile(voiceDir, ".mp4");
        if (videoPath == null) {
            System.out.println("⚠️ Không tìm thấy file video nào!");
            return;
        }

        String baseFileName = "ADMICR - Vietnamese Speech Synthesis";
        String fileExtension = ".mp4";
        String outputFileName = getAvailableFileNameForUploadVideo(outputPath, baseFileName, fileExtension);

        // Lệnh FFmpeg
        String command = "\"" + ffmpegPath + "\" -loop 1 -i \"" + imagePath + "\" -i \"" + videoPath +
                "\" -c:v libx264 -tune stillimage -c:a aac -b:a 192k -pix_fmt yuv420p -shortest \"" + outputFileName + "\"";

        System.out.println("Run command : " + command);

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
                System.out.println("✅ Hoàn tất ghép ảnh vào video! File xuất ra: " + outputFileName);
            } else {
                System.out.println("⚠️ FFmpeg gặp lỗi, kiểm tra output trên.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Hàm để đọc nội dung từ file
    private static String readFileContent(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public static void downloadAudio(String audioUrl) throws IOException {
        // Tạo URL từ đường dẫn audio
        URL url = new URL(audioUrl);

        // Mở kết nối HTTP
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Kiểm tra mã trạng thái HTTP
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // Lấy dữ liệu âm thanh
            InputStream inputStream = connection.getInputStream();

            // Đặt thư mục và tên file cơ bản
            String directoryPath = "E:\\CongViecHocTap\\Voice\\";
            String baseFileName = "voice";
            String fileExtension = ".mp4";
            String fileName = getAvailableFileName(directoryPath, baseFileName, fileExtension);

            // Lưu âm thanh vào file
            try (FileOutputStream outputStream = new FileOutputStream(directoryPath + fileName)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Audio đã được tải về và lưu dưới tên: " + fileName);
            }
        } else {
            System.out.println("Không thể tải âm thanh. Mã trạng thái: " + connection.getResponseCode());
        }

        // Đóng kết nối
        connection.disconnect();
    }

    private static String getAvailableFileName(String directoryPath, String baseFileName, String extension) {
        int fileNumber = 1;
        String fileName = baseFileName + "(" + fileNumber + ")" + extension;
        while (Files.exists(Paths.get(directoryPath + fileName))) {
            fileNumber++;
            fileName = baseFileName + "(" + fileNumber + ")" + extension;
        }
        return fileName;
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


    private static String getLatestFile(String dirPath, String ext) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        File latest = null;
        long lastModified = 0;

        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            if (file.isFile() && file.getName().endsWith(ext)) {
                long modifiedTime = file.lastModified();
                if (latest == null || modifiedTime > lastModified) {
                    latest = file;
                    lastModified = modifiedTime;
                }
            }
        }

        if (latest == null) {
            return null;
        } else {
            return latest.getAbsolutePath();
        }
    }

    private static void uploadFile(String filePath) throws AWTException, InterruptedException {
        Robot robot = new Robot();

        // Copy đường dẫn file vào clipboard
        StringSelection selection = new StringSelection(filePath);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

        // Ctrl + V để dán
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);

        Thread.sleep(500);

        // Nhấn Enter để chọn file
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }
}

//        Thread.sleep(2000);
//        driver.get("https://studio.youtube.com/channel/UC-sx957PH2t0ordUg8kBcHw");
//
//        Thread.sleep(2000);
//
//        driver.findElement(By.xpath("//*[@id=\"create-icon\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//
//        driver.findElement(By.xpath("//*[@id=\"text-item-0\"]/ytcp-ve/tp-yt-paper-item-body/div/div/div/yt-formatted-string")).click();
//
//        driver.findElement(By.xpath("//*[@id=\"select-files-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//        Thread.sleep(2000);
//
//        String latestFile = getLatestFile("E:\\CongViecHocTap\\UploadVideo", ".mp4");
//
//        if (latestFile != null) {
//            uploadFile(latestFile);
//        } else {
//            System.out.println("Không tìm thấy file nào trong thư mục.");
//        }
//
//        Thread.sleep(5000);
//
//        // Tìm phần tử `details`
//        WebElement detailsSection = driver.findElement(By.xpath("//*[@id=\"details\"]"));
//
//        // Cuộn xuống đáy của `details`
//        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", detailsSection);
//
//        Thread.sleep(1000); // Đợi trang load (nếu cần)
//
//        // Tìm phần tử `offRadio`
//        WebElement offRadio = driver.findElement(By.xpath("//*[@id=\"offRadio\"]"));
//
//        // Click vào `offRadio`
//        offRadio.click();
//
//        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//
//        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//
//        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//
//        Thread.sleep(2000);
//        driver.findElement(By.xpath("//*[@id=\"offRadio\"]")).click();
//
//        driver.findElement(By.xpath("//*[@id=\"done-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
//        Thread.sleep(60000);
