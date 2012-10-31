package ru.megaplan.jira.plugins.megaworkpressure.pageobjects;

import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 20.07.12
 * Time: 18:23
 * To change this template use File | Settings | File Templates.
 */
public class PressureDialog {

    private final static String ISSUETABLEID = "work-pressure-issuetable";

    @Inject
    private AtlassianWebDriver webDriver;


    public boolean isPresent() {
        WebElement dialog = webDriver.findElement(By.id("work-pressure-dialog"));
        return dialog != null;
    }

    @WaitUntil
    public void waitUntilIssueTable() {
        webDriver.waitUntilElementIsLocated(By.id(ISSUETABLEID));
    }

    public boolean isMainTablePresent() {
        WebElement mainTable = webDriver.findElement(By.id(ISSUETABLEID));
        return mainTable != null;
    }

}
