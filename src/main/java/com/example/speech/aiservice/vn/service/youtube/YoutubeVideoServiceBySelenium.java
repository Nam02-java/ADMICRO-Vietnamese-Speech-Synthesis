package com.example.speech.aiservice.vn.service.youtube;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class YoutubeVideoServiceBySelenium {

    public static void main(String[] args) throws IOException, InterruptedException, AWTException {
        upload();
    }

    private static void upload() throws IOException, InterruptedException, AWTException {

        String chromePath = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
        String userDataDir = "C:\\selenium_chrome";
        String command = chromePath + " --remote-debugging-port=9222 --user-data-dir=" + userDataDir; //+ " --headless"

        Process process = Runtime.getRuntime().exec(command);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");
        WebDriver driver = new ChromeDriver(options);

                Thread.sleep(2000);
        driver.get("https://studio.youtube.com/channel/UC-sx957PH2t0ordUg8kBcHw");

        Thread.sleep(2000);

        driver.findElement(By.xpath("//*[@id=\"create-icon\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();

        driver.findElement(By.xpath("//*[@id=\"text-item-0\"]/ytcp-ve/tp-yt-paper-item-body/div/div/div/yt-formatted-string")).click();

        driver.findElement(By.xpath("//*[@id=\"select-files-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
        Thread.sleep(2000);

        String latestFile = getLatestFile("E:\\CongViecHocTap\\UploadVideo", ".mp4");

        if (latestFile != null) {
            uploadFile(latestFile);
        } else {
            System.out.println("Không tìm thấy file nào trong thư mục.");
        }

        Thread.sleep(5000);

        // Tìm phần tử `details`
        WebElement detailsSection = driver.findElement(By.xpath("//*[@id=\"details\"]"));

        // Cuộn xuống đáy của `details`
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[0].scrollHeight;", detailsSection);

        Thread.sleep(1000); // Đợi trang load (nếu cần)

        // Tìm phần tử `offRadio`
        WebElement offRadio = driver.findElement(By.xpath("//*[@id=\"offRadio\"]"));

        // Click vào `offRadio`
        offRadio.click();

        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();

        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();

        driver.findElement(By.xpath("//*[@id=\"next-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();

        Thread.sleep(2000);
        driver.findElement(By.xpath("//*[@id=\"offRadio\"]")).click();

        driver.findElement(By.xpath("//*[@id=\"done-button\"]/ytcp-button-shape/button/yt-touch-feedback-shape/div/div[2]")).click();
        Thread.sleep(60000);

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
}
