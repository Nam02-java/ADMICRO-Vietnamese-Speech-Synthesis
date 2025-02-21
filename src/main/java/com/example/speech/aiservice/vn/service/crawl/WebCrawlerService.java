package com.example.speech.aiservice.vn.service.crawl;

import com.example.speech.aiservice.vn.dto.response.WebCrawlResponseDTO;
import com.example.speech.aiservice.vn.service.filehandler.FileNameService;
import com.example.speech.aiservice.vn.service.filehandler.FileWriterService;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebCrawlerService {
    private final FileNameService fileNameService;
    private final FileWriterService fileWriterService;
    private final WaitService waitService;
    private final String directoryPath = "E:\\CongViecHocTap\\Content\\";
    private final String baseFileName = "content";
    private final String fileExtension = ".txt";


    @Autowired
    public WebCrawlerService(FileNameService fileNameService, FileWriterService fileWriterService, WaitService waitService) {
        this.fileNameService = fileNameService;
        this.fileWriterService = fileWriterService;
        this.waitService = waitService;
    }

    public WebCrawlResponseDTO webCrawlResponseDTO(WebDriver driver, String url) throws InterruptedException {

        driver.get(url);

        waitService.waitForSeconds(5); // Wait for the translation to be complete

        String pageSource = driver.getPageSource();

        Document doc = Jsoup.parse(pageSource);

        String content = doc.select("#svelte > div.tm-light.rd-ff-0.rd-fs-3.svelte-19vhflx > main > article:nth-child(5)").text();

        String contentFilePath = fileNameService.getAvailableFileName(directoryPath, baseFileName, fileExtension);

        fileWriterService.writeToFile(contentFilePath, content);

        return new WebCrawlResponseDTO("Crawling completed", url, contentFilePath);

    }
}

/**
 * researching
 */
//WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        wait.until(new Function<WebDriver, Boolean>() {
//            @Override
//            public Boolean apply(WebDriver d) {
//                return ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
//            }
//        });