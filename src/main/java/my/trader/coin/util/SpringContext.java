package my.trader.coin.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContext implements ApplicationContextAware {
  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    context = applicationContext;
  }

  public static ApplicationContext getContext() {
    return context;
  }
}
