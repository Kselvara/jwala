package com.cerner.jwala.ui.selenium.component;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper component that simplifies the calling of Selenium driver commands
 *
 * Created by Jedd Cuison on 6/28/2017
 */
@Component
public class JwalaUi {

    @Autowired
    private WebDriver driver;

    @Autowired
    private WebDriverWait webDriverWait;

    @Autowired
    @Qualifier("seleniumTestProperties")
    private Properties properties;

    public void clickTab(final String tabLabel) {
        final WebElement configTag = driver.findElement(By.xpath("//li[a[text()='" + tabLabel + "']]"));
        if (!configTag.getAttribute("class").equalsIgnoreCase("current")) {
            configTag.click();
            webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("AppBusyScreen")));
        }
    }

    /**
     * Wait until element is clickable before clicking
     * @param by {@link By}
     */
    public void clickWhenReady(final By by) {
        webDriverWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".AppBusyScreen")));
        webDriverWait.until(ExpectedConditions.elementToBeClickable(by)).click();
    }

    public WebElement clickTreeItemExpandCollapseIcon(final String itemLabel) {
        final WebElement webElement =
                driver.findElement(By.xpath("//li[span[text()='" +  itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    public WebElement clickTreeItemExpandCollapseIcon(final WebElement parentNode, final String itemLabel) {
        final WebElement webElement =
                parentNode.findElement(By.xpath("//li[span[text()='" +  itemLabel + "']]/img[contains(@class, 'expand-collapse-padding')]"));
        new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOf(webElement));
        webElement.click();
        return webElement;
    }

    public String getBaseUrl() {
        return properties.getProperty("base.url");
    }

    public void testForBusyIcon(final int showTimeout, final int hideTimeout) {
        new FluentWait(driver).withTimeout(showTimeout, TimeUnit.SECONDS).pollingEvery(100, TimeUnit.MILLISECONDS)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 1));
        // Please take note that when "display none" is re-inserted, the whitespace in between the attribute and the value is not there anymore e.g.
        // display: none => display:none hence the xpath => contains(@style,'display:none')
        new WebDriverWait(driver, hideTimeout)
                .until(ExpectedConditions.numberOfElementsToBe(
                        By.xpath("//div[contains(@class, 'AppBusyScreen') and contains(@class, 'show')]"), 0));
    }

    public void rightClick(final By by) {
        final Actions action = new Actions(driver).contextClick(driver.findElement(by));
        action.build().perform();
    }

    public boolean isElementExists(final String xPath) {
        try {
            driver.findElement(By.xpath(xPath));
        } catch (final NoSuchElementException e) {
            return false;
        }
        return true;
    }

    public void sendKeys(final CharSequence val) {
        driver.switchTo().activeElement().sendKeys(val);
    }

    public void sendKeys(final By by, final CharSequence val) {
        driver.findElement(by).sendKeys(val);
    }

    public void click(final By by) {
        driver.findElement(by).click();
    }

    public void waitUntilElementIsVisible(final By by) {
        webDriverWait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public void waitUntilElementIsNotVisible(final By by) {
        webDriverWait.until(ExpectedConditions.numberOfElementsToBe(by, 0));
    }

    public void loadPath(final String path) {
        driver.get(getBaseUrl() + path);
    }

    public Properties getProperties() {
        return properties;
    }
}