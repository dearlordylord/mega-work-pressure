package ru.megaplan.jira.plugins.megaworkpressure.pageobjects;

import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 20.07.12
 * Time: 18:01
 * To change this template use File | Settings | File Templates.
 */
public class QuickCreateIssuePressure {

    @Inject
    private PageBinder pageBinder;

    @Inject
    private AtlassianWebDriver webDriver;

    @FindBy(id = "assignee-field")
    private WebElement assigneeField;

    @WaitUntil
    void assigneeFieldIsPresent() {
        webDriver.waitUntilElementIsLocated(By.id("assignee-field"));
    }

    public void chooseAssignee(String assignee) {
        assigneeField.click();
        assigneeField.sendKeys("firfi");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        assigneeField.sendKeys(Keys.ENTER);
        webDriver.waitUntilElementIsLocated(By.id("pressure-link"));
    }

    public PressureDialog gotoPressureDialog() {
        WebElement pressureLink = webDriver.findElement(By.id("pressure-link"));
        Actions builder = new Actions(webDriver);
        System.out.println(pressureLink.getAttribute("href"));
        builder.moveToElement(pressureLink).perform();
        System.out.println(pressureLink.getAttribute("href"));
        pressureLink.click();
        return pageBinder.bind(PressureDialog.class);
    }

}
