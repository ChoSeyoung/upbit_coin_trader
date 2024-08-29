package my.trader.coin.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import my.trader.coin.config.AppConfig;
import my.trader.coin.enums.ColorfulConsoleOutput;
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

  /**
   * UBMI 지수 업데이트.
   */
  public void fetchUpbitMarketIndexRatio() {
    String url = "https://www.ubcindex.com/indexes/IDX.UPBIT.UBMI";

    try {
      driver.get(url);
      // XPath를 사용하여 특정 요소를 선택
      WebElement parentElement = driver.findElement(By.xpath(
            "//*[@id=\"__layout\"]/div/div[2]/section/div/div[2]/div/div/div[1]/div[2]/div[1]/div/div"));
      WebElement element = driver.findElement(By.xpath(
            "//*[@id=\"__layout\"]/div/div[2]/section/div/div[2]/div/div/div[1]/div[2]/div[1]/div/div/div[3]"));

      // 상위 요소의 클래스명 추출
      String className = parentElement.getAttribute("class");

      // 클래스명을 공백 단위로 쪼개고 rise가 있는지 확인
      String[] classNames = className.split("\\s+");
      int multiply = -1;
      for (String cls : classNames) {
        if (cls.contains("rise")) {
          multiply = 1;
          break;
        }
      }

      // 요소의 텍스트 데이터 추출
      String data = element.getText().replaceAll("[^0-9.]", "");

      AppConfig.setUpbitMarketIndexRatio(Double.parseDouble(data) * multiply);

      // 인덱스가 0% 미만으로 내려가는 경우 매매 중지
      AppConfig.setHoldTrade(AppConfig.upbitMarketIndexRatio < 0.0);

      ColorfulConsoleOutput.printWithColor(
            "Current Upbit Market Index: " + AppConfig.upbitMarketIndexRatio,
            ColorfulConsoleOutput.PURPLE);
    } catch (Exception e) {
      logger.error("스크래핑 중 에러 발생", e);
    }
  }

  /**
   * 마켓 시가총액을 기준으로 종목 선정.
   */
  public void fetchHighMarketCapitalization() {
    List<String> result = new ArrayList<>();

    String url = "https://www.ubcindex.com/indexes/IDX.UPBIT.UBMI";

    try {
      driver.get(url);

      WebElement parentElement = driver.findElement(By.xpath(
            "//*[@id=\"__layout\"]/div/div[2]/section/div/div[2]/div/div/div[2]/table/tbody/tr/td/div/div"));

      // 자식 엘리먼트들(basketItem 클래스)을 모두 찾기
      List<WebElement> basketItems =
            parentElement.findElements(By.xpath(".//div[@class='basketItem']"));

      for (WebElement item : basketItems) {
        // code 클래스에서 코인 코드 가져오기
        WebElement codeElement = item.findElement(By.xpath(".//div[@class='code']"));
        String[] coinCodes = codeElement.getText().split("/");

        // ratio 클래스에서 비율 가져오기 (추가적으로 필요하다면)
        WebElement ratioElement = item.findElement(By.xpath(".//div[@class='ratio']"));
        String coinRatioText = ratioElement.getText();
        double coinRatio = Double.parseDouble(coinRatioText.replaceAll("[^\\d.]", ""));

        if (coinRatio > 1.0) {
          result.add(coinCodes[1] + "-" + coinCodes[0]);
        } else {
          break;
        }
      }

      AppConfig.setScheduledMarket(result);
    } catch (Exception e) {
      logger.error("스크래핑 중 에러 발생", e);
    }
  }
}
