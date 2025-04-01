package com.example.speech.aiservice.vn.service.image;

import com.example.speech.aiservice.vn.dto.response.NovelInfoResponseDTO;
import com.example.speech.aiservice.vn.service.filehandler.FileNameService;
import com.example.speech.aiservice.vn.service.propertie.PropertiesService;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {

    private final PropertiesService propertiesService;
    private final FileNameService fileNameService;

    @Autowired
    public ImageService(PropertiesService propertiesService, FileNameService fileNameService) {
        this.propertiesService = propertiesService;
        this.fileNameService = fileNameService;
    }

    public String getValidImagePath(WebDriver driver, NovelInfoResponseDTO novelInfoResponseDTO) {

        String directoryPath = propertiesService.getImageDirectory();
        String baseFileName = novelInfoResponseDTO.getTitle();
        String extension = propertiesService.getImageExtension();

        try {
            Path dirPath = Paths.get(directoryPath);
            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, "*" + extension)) {
                    for (Path filePath : stream) {
                        String fileName = filePath.getFileName().toString();
                        if (fileName.contains(baseFileName)) {
                            System.out.println("✅ Found existing image: " + filePath);
                            return filePath.toString();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (driver == null) {
            System.out.println("❌ WebDriver is null, skipping image extraction...");
            return null;
        }

        IIORegistry.getDefaultInstance().registerServiceProvider(new WebPImageReaderSpi());

        WebElement imgElement = driver.findElement(By.cssSelector("img.svelte-34gr27"));
        String imgUrl = imgElement.getAttribute("src");
        System.out.println("Image URL: " + imgUrl);

        String imageFilePath = fileNameService.getAvailableFileName(directoryPath, baseFileName, extension);

        try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Referer", "https://chivi.app");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            InputStream inputStream = connection.getInputStream();
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                System.out.println("Failed to decode image. The image might be in WebP format.");
                return null;
            }

            ImageIO.write(image, "png", new File(imageFilePath));

            System.out.println("Image saved as: " + imageFilePath);

            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageFilePath;
    }
}
