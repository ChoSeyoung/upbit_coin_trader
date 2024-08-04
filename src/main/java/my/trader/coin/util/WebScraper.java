package my.trader.coin.util;

import java.time.Duration;
import my.trader.coin.vo.StaticConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class WebScraper implements DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(WebScraper.class);

  private WebDriver driver;

  public WebScraper() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless"); // 브라우저 창을 띄우지 않도록 설정
    this.driver = new ChromeDriver(options);
    this.driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
  }

  @Override
  public void destroy() {
    if (driver != null) {
      try {
        driver.quit();
      } catch (Exception e) {
        logger.error("드라이버 종료 중 에러 발생", e);
      }
    }
  }

  public void fetchUpbitMarketIndexRatio() {
    String url = "https://www.ubcindex.com/indexes/IDX.UPBIT.UBMI";

    try {
      driver.get(url);
      // XPath를 사용하여 특정 요소를 선택
      WebElement element = driver.findElement(By.xpath(
            "//*[@id=\"__layout\"]/div/div[2]/section/div/div[2]/div/div/div[1]/div[2]/div[1]/div/div/div[3]"));

      String data = element.getText().replaceAll("[^0-9.]", "");

      StaticConfig.setUpbitMarketIndexRatio(Double.parseDouble(data));

      System.out.println("Current Upbit Market Index: " + StaticConfig.getUpbitMarketIndexRatio());
      // 추가적으로 데이터를 저장하거나 처리하는 로직을 작성합니다.
    } catch (Exception e) {
      logger.error("스크래핑 중 에러 발생", e);
    }
  }
}
