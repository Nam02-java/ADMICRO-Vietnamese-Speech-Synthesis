package com.example.speech.aiservice.vn.service.workflow;

import com.example.speech.aiservice.vn.dto.response.NovelInfoResponseDTO;
import com.example.speech.aiservice.vn.model.entity.Chapter;
import com.example.speech.aiservice.vn.model.entity.Novel;
import com.example.speech.aiservice.vn.model.entity.SeleniumConfig;
import com.example.speech.aiservice.vn.model.repository.ChapterRepository;
import com.example.speech.aiservice.vn.model.repository.NovelRepository;
import com.example.speech.aiservice.vn.service.chapter.ChapterService;
import com.example.speech.aiservice.vn.service.console.CommandType;
import com.example.speech.aiservice.vn.service.executor.MyRunnableService;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.novel.NovelService;
import com.example.speech.aiservice.vn.service.selenium.SeleniumConfigService;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import jakarta.annotation.PreDestroy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class PreProcessorService {

    private final GoogleChromeLauncherService googleChromeLauncherService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final WaitService waitService;
    private final NovelService novelService;
    private final ChapterService chapterService;
    private final ExecutorService executorService;
    private final ApplicationContext applicationContext;
    private final SeleniumConfigService seleniumConfigService;

    @Autowired
    public PreProcessorService(GoogleChromeLauncherService googleChromeLauncherService, WebDriverLauncherService webDriverLauncherService, WaitService waitService, NovelRepository novelRepository, ChapterRepository chapterRepository, NovelService novelService, ChapterService chapterService, ApplicationContext applicationContext, SeleniumConfigService seleniumConfigService) {
        this.googleChromeLauncherService = googleChromeLauncherService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.waitService = waitService;
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.applicationContext = applicationContext;
        this.seleniumConfigService = seleniumConfigService;
        this.executorService = Executors.newFixedThreadPool(3);
    }

    @Scheduled(fixedDelay = 600000) // 10 mins
    public void executeWorkflow() {

        NovelInfoResponseDTO novelInfo = scanNovelTitle();
        fetchFullPageContent(novelInfo);

        List<SeleniumConfig> threadConfigs = seleniumConfigService.getAllConfigs();
        List<Chapter> unscannedChapters;
        int maxThreads = 3; // Limit scanning 3 chapters at a time


        while (true) {

            Novel novel = novelService.findByTitle(novelInfo.getTitle());

            // Get list of unscanned chapters
            unscannedChapters = chapterService.getUnscannedChapters(novel.getId());

            // If there are no more chapters to scan, stop the loop.
            if (unscannedChapters.isEmpty()) {
                System.out.println("All chapters have been scanned. Stopping workflow.");
                break;
            }

            int taskCount = Math.min(maxThreads, unscannedChapters.size());
            CountDownLatch latch = new CountDownLatch(taskCount);

            for (int i = 0; i < taskCount; i++) {

                Chapter chapter = unscannedChapters.get(i);
                SeleniumConfig config = threadConfigs.get(i);

                FullWorkFlow fullWorkFlow = applicationContext.getBean(FullWorkFlow.class);
                MyRunnableService myRunnableService = new MyRunnableService(
                        fullWorkFlow,
                        config.getPort(),
                        config.getSeleniumFileName(),
                        chapter.getNovel(),
                        chapter
                );
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            myRunnableService.run();
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            try {
                latch.await(); // Wait for all 3 threads to finish running before continuing
            } catch (InterruptedException e) {
                System.err.println("Error completing task : " + e.getMessage());
            }
            System.out.println("Complete threads, continue scanning...");
        }
    }

    private NovelInfoResponseDTO scanNovelTitle() {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.println("Enter the novel link to scan: ");
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                try {
                    Document doc = Jsoup.connect(input).get();
                    String title = doc.title();
                    title = title.split(" - ", 2)[0].trim();
                    System.out.println("Title: " + title);
                    scanner.close();
                    return new NovelInfoResponseDTO(title, input);
                } catch (Exception e) {
                    System.err.println("Error fetching novel title: " + e.getMessage());
                }
            }
        }
    }


    private void fetchFullPageContent(NovelInfoResponseDTO novelInfo) {

        WebDriver driver = null;
        String defaultPort = "9222";

        if (!novelService.isNovelExists(novelInfo.getTitle())) {

            try {
                SeleniumConfig seleniumConfig = seleniumConfigService.getConfigByPort(defaultPort);
                if (seleniumConfig == null) {
                    System.out.println("Could not find configuration with port " + defaultPort);
                    System.exit(1);
                }

                googleChromeLauncherService.openGoogleChrome(seleniumConfig.getPort(), seleniumConfig.getSeleniumFileName());
                driver = webDriverLauncherService.initWebDriver(seleniumConfig.getPort());

                driver.get(novelInfo.getLink());

                waitService.waitForSeconds(1);
                driver.findElement(By.xpath("//*[@id=\"svelte\"]/div[1]/main/article[1]/div[2]/div[1]/svelte-css-wrapper/div/div[1]/button")).click();
                waitService.waitForSeconds(1);
                driver.findElement(By.xpath("//*[@id=\"svelte\"]/div[1]/main/article[1]/div[2]/div[1]/svelte-css-wrapper/div/div[2]/div/a[1]/span[1]")).click();

                waitService.waitForSeconds(3);
                driver.navigate().refresh();
                waitService.waitForSeconds(3);

//            WebElement novelElement = driver.findElement(By.cssSelector("nav.bread a[href*='/wn/books']:nth-last-of-type(2) span"));
//            String novelTitle = novelElement.getText();
//            String novelLink = novelElement.findElement(By.xpath("./parent::a")).getAttribute("href");

                Novel novel = new Novel(novelInfo.getTitle(), novelInfo.getLink());
                novelService.saveNovel(novel);

                // Get a list of pressable buttons
                List<WebElement> buttons = driver.findElements(By.cssSelector("section.article._padend.island button.cpage.svelte-ssn7if"));

                while (true) {
                    boolean hasClicked = false;
                    for (WebElement button : buttons) {
                        try {
                            WebElement svgIcon = button.findElement(By.cssSelector("svg.m-icon use"));
                            String iconHref = svgIcon.getAttribute("xlink:href");
                            if (iconHref.equals("/icons/tabler.svg#chevron-down")) {
                                button.click();
                                waitService.waitForSeconds(1);
                                hasClicked = true;
                            }
                        } catch (NoSuchElementException e) {
                            System.out.println(e);
                        }
                    }
                    if (!hasClicked) {
                        break; // Exit the loop if there are no buttons to click
                    }
                    buttons = driver.findElements(By.cssSelector("section.article._padend.island button.cpage.svelte-ssn7if"));
                }

                waitService.waitForSeconds(2);

                // Get the list of chapters
                List<WebElement> chapters = driver.findElements(By.cssSelector("div.chaps a.cinfo"));

                // Browse each chapter to get information
                for (WebElement chapter : chapters) {
                    try {
                        String chapterTitle = chapter.findElement(By.cssSelector("span.title")).getText();
                        String chapterNumberText = chapter.findElement(By.cssSelector("span.ch_no")).getText();
                        String chapterLink = chapter.getAttribute("href");

                        String chapterNumber = chapterNumberText.replaceAll("[^0-9]", "");

                        System.out.println("Chapter " + chapterNumberText + ": " + chapterTitle);
                        System.out.println("Link: " + chapterLink);
                        System.out.println("-------------------------");

                        chapterService.addChapter(novel, Integer.valueOf(chapterNumber), chapterTitle, chapterLink);


                    } catch (RuntimeException e) {
                        System.out.println("Skip chapter due to error : " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("Error outside the protocol : " + e.getMessage());
                        e.printStackTrace();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                googleChromeLauncherService.shutdown();
                webDriverLauncherService.shutDown(driver);
            }
        } else {
            System.out.println(String.format("%s with link: %s already exists in the database system, stop crawling from the website! ", novelInfo.getTitle(), novelInfo.getLink()));
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
        System.out.println("ExecutorService đã shutdown.");
    }
}
