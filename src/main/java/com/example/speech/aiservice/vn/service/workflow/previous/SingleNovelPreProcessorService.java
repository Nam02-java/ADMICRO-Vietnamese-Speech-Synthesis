package com.example.speech.aiservice.vn.service.workflow.previous;

import com.example.speech.aiservice.vn.dto.response.NovelInfoResponseDTO;
import com.example.speech.aiservice.vn.model.entity.chapter.Chapter;
import com.example.speech.aiservice.vn.model.entity.novel.Novel;
import com.example.speech.aiservice.vn.model.entity.selenium.SeleniumConfigSingle;
import com.example.speech.aiservice.vn.model.entity.track.TrackedNovelSingle;
import com.example.speech.aiservice.vn.model.repository.chapter.ChapterRepository;
import com.example.speech.aiservice.vn.model.repository.novel.NovelRepository;
import com.example.speech.aiservice.vn.service.filehandler.FileNameService;
import com.example.speech.aiservice.vn.service.image.ImageService;
import com.example.speech.aiservice.vn.service.propertie.PropertiesService;
import com.example.speech.aiservice.vn.service.queue.ScanQueue;
import com.example.speech.aiservice.vn.service.executor.MyRunnableService;
import com.example.speech.aiservice.vn.service.google.GoogleChromeLauncherService;
import com.example.speech.aiservice.vn.service.repositoryService.chapter.ChapterService;
import com.example.speech.aiservice.vn.service.repositoryService.novel.NovelService;
import com.example.speech.aiservice.vn.service.repositoryService.selenium.SeleniumConfigSingleService;
import com.example.speech.aiservice.vn.service.repositoryService.track.TrackedNovelSingleService;
import com.example.speech.aiservice.vn.service.schedule.TimeDelay;
import com.example.speech.aiservice.vn.service.selenium.WebDriverLauncherService;
import com.example.speech.aiservice.vn.service.wait.WaitService;
import com.example.speech.aiservice.vn.service.workflow.full.FullWorkFlow;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

@Service
public class SingleNovelPreProcessorService {

    private final GoogleChromeLauncherService googleChromeLauncherService;
    private final WebDriverLauncherService webDriverLauncherService;
    private final WaitService waitService;
    private final NovelService novelService;
    private final ChapterService chapterService;
    private final TrackedNovelSingleService trackedNovelService;
    private final ExecutorService executorService;
    private final ApplicationContext applicationContext;
    private final SeleniumConfigSingleService seleniumConfigSingleService;
    private final FileNameService fileNameService;
    private volatile boolean stop = false; // Volatile variable to track STOP command - true = stopping
    private volatile String imagePath = null;
    private final TaskScheduler taskScheduler;
    private volatile ScheduledFuture<?> scheduledTask;
    private final TimeDelay timeDelay;
    private final PropertiesService propertiesService;
    private final ScanQueue scanQueue;
    private final ImageService imageService;
    private long totalChaptersFromUrl;
    private long totalChaptersFromDatabase;

    @Autowired
    public SingleNovelPreProcessorService(GoogleChromeLauncherService googleChromeLauncherService, WebDriverLauncherService webDriverLauncherService, WaitService waitService, NovelService novelService, ChapterService chapterService, TrackedNovelSingleService trackedNovelService, ApplicationContext applicationContext, SeleniumConfigSingleService seleniumConfigSingleService, FileNameService fileNameService, TaskScheduler taskScheduler, TimeDelay timeDelay, PropertiesService propertiesService, ScanQueue scanQueue, ImageService imageService) {
        this.googleChromeLauncherService = googleChromeLauncherService;
        this.webDriverLauncherService = webDriverLauncherService;
        this.waitService = waitService;
        this.novelService = novelService;
        this.chapterService = chapterService;
        this.trackedNovelService = trackedNovelService;
        this.applicationContext = applicationContext;
        this.seleniumConfigSingleService = seleniumConfigSingleService;
        this.fileNameService = fileNameService;
        this.taskScheduler = taskScheduler;
        this.timeDelay = timeDelay;
        this.propertiesService = propertiesService;
        this.scanQueue = scanQueue;
        this.imageService = imageService;
        this.executorService = Executors.newFixedThreadPool(3);
    }


    public void startWorkflow(long delay) {
        System.out.println("delay - " + timeDelay.getSecond() + "ms");
        timeDelay.setSecond(0);
        if (scheduledTask != null && !scheduledTask.isDone()) {
            return;
        }
        scheduledTask = taskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                executeWorkflow();
                stop = false;
            }
        }, new Date(System.currentTimeMillis() + delay));
    }


    public void executeWorkflow() {
        NovelInfoResponseDTO novelInfo = scanNovelTitle();
        fetchFullPageContent(novelInfo);

        List<SeleniumConfigSingle> threadConfigs = seleniumConfigSingleService.getAllConfigs();
        List<Chapter> unscannedChapters;
        int maxThreads = 3;

        if (!stop) {

            /**
             * get image novel
             */
            imagePath = imageService.getValidImagePath(null, novelInfo);

            System.out.println("stop is false");
            while (true) {
                Novel novel = novelService.findByTitle(novelInfo.getTitle());
                unscannedChapters = chapterService.getUnscannedChapters(novel.getId());

                if (unscannedChapters.isEmpty()) {
                    System.out.println("⚡ All chapters have been scanned, scheduling next check...");
                    if (novel != null) {
                        //trackedNovelService.trackNovel(novel);
                        trackedNovelService.clearTracking();
                    }
                    timeDelay.setSecond(1000);
                    return;
                }

                int taskCount = Math.min(maxThreads, unscannedChapters.size());
                CountDownLatch latch = new CountDownLatch(taskCount);

                for (int i = 0; i < taskCount; i++) {
                    if (stop) {
                        System.out.println("STOP command received! No new tasks will be started.");
                        timeDelay.setSecond(5000);
                        return;
                    }

                    Chapter chapter = unscannedChapters.get(i);
                    SeleniumConfigSingle config = threadConfigs.get(i);


                    FullWorkFlow fullWorkFlow = applicationContext.getBean(FullWorkFlow.class);
                    MyRunnableService myRunnableService = new MyRunnableService(fullWorkFlow, config.getPort(), config.getSeleniumFileName(), chapter.getNovel(), chapter, imagePath);
                    executorService.execute(() -> {
                        try {
                            myRunnableService.run();
                        } finally {
                            latch.countDown();
                        }
                    });
                }

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    System.err.println("Error completing task : " + e.getMessage());
                }
                System.out.println("Complete threads, continue scanning...");
            }
        } else {
            System.out.println("stop is true");
            stopConditions();
            timeDelay.setSecond(5000);
        }
    }

    private NovelInfoResponseDTO scanNovelTitle() {
        String inputLink;

        Optional<TrackedNovelSingle> trackedNovelOptional = trackedNovelService.getTrackedNovel();

        if (!trackedNovelOptional.isPresent()) {
            while (true) {

                inputLink = scanQueue.takeFromQueue();
                scanQueue.printQueue();

                if (!inputLink.isEmpty()) {
                    try {
                        Document doc = Jsoup.connect(inputLink).get();
                        String title = doc.title();
                        String safeTitle = title.split(" - ", 2)[0].trim();
                        System.out.println("Title: " + safeTitle);

                        return new NovelInfoResponseDTO(safeTitle, inputLink);
                    } catch (Exception e) {
                        //System.err.println("Error fetching novel title: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            String novelTitle = trackedNovelOptional.get().getNovel().getTitle();
            String novelLink = trackedNovelOptional.get().getNovel().getLink();
            return new NovelInfoResponseDTO(novelTitle, novelLink);
        }
    }


    private void fetchFullPageContent(NovelInfoResponseDTO novelInfo) {

        WebDriver driver = null;
        String defaultPort = propertiesService.getDefaultPort();

        scanChapter(driver, novelInfo, defaultPort);

    }

    private void scanChapter(WebDriver driver, NovelInfoResponseDTO novelInfo, String defaultPort) {
        try {
            SeleniumConfigSingle seleniumConfig = seleniumConfigSingleService.getConfigByPort(defaultPort);
            if (seleniumConfig == null) {
                System.out.println("Could not find configuration with port " + defaultPort);
                System.exit(1);
            }

            googleChromeLauncherService.openGoogleChrome(seleniumConfig.getPort(), seleniumConfig.getSeleniumFileName());
            driver = webDriverLauncherService.initWebDriver(seleniumConfig.getPort());

            waitService.waitForSeconds(1);

            driver.get(novelInfo.getLink());

            /**
             * get total chapter number on novel's website
             */
            waitService.waitForSeconds(1);
            driver.findElement(By.xpath("//*[@id=\"svelte\"]/div[1]/main/article[1]/div[2]/div[1]/svelte-css-wrapper/div/div[1]/button")).click();
            waitService.waitForSeconds(5);
            WebElement spanElement = driver.findElement(By.cssSelector("span.ctext.svelte-1d9icyk"));
            totalChaptersFromUrl = Long.parseLong(spanElement.getText());
            System.out.println("Total chapters from \"" + novelInfo.getTitle() + "\" (URL: " + novelInfo.getLink() + "): " + totalChaptersFromUrl);

            /**
             * get total chapter number in database
             */
            Novel novel = novelService.findByTitle(novelInfo.getTitle());
            totalChaptersFromDatabase = 0;
            if (!(novel == null)) {
                long novelId = novel.getId();
                totalChaptersFromDatabase = chapterService.getTotalChapters(novelId);
            }

            System.out.println("Total chapters from \"" + novelInfo.getTitle() + "\" (Database): " + totalChaptersFromDatabase);
            if (totalChaptersFromDatabase < totalChaptersFromUrl) {

                waitService.waitForSeconds(1);
                /**
                 * get image novel
                 */
                imagePath = imageService.getValidImagePath(driver, novelInfo);

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

                novel = new Novel(novelInfo.getTitle(), novelInfo.getLink());
                if (!novelService.isNovelExists(novel.getTitle())) {
                    novelService.saveNovel(novel);
                }

                novel = novelService.findByTitle(novel.getTitle()); // Retrieve saved object from database
                if (novel != null) {
                    trackedNovelService.trackNovel(novel); // Always follow novel
                }

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

                        /**
                         * only stop when crawling chapters on the website and not saving anything to the database
                         */
                        if (stop) {
                            System.out.println("stop at fetchFullPageContent method");
                            return;
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
            } else {
                System.out.println("No new chapters found on the : " + novelInfo.getLink());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            googleChromeLauncherService.shutdown();
            webDriverLauncherService.shutDown(driver);
        }
    }

    public void stopConditions() {
        trackedNovelService.clearTracking();
        imagePath = null;
        stop = true;
    }
}
