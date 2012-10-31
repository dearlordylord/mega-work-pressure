package ru.megaplan.jira.plugins.megaworkpressure.pageobjects;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.component.Header;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 19.07.12
 * Time: 14:54
 * To change this template use File | Settings | File Templates.
 */
public class DashboardPagePressure extends DashboardPage {

    @Inject
    private PageBinder pageBinder;

    @Inject
    protected PageElementFinder pageElementFinder;

    @Inject
    private AtlassianWebDriver webDriver;

    @FindBy(id = "create_link")
    private WebElement quickCreateButton;

    @WaitUntil
    public void waitUntilBody() {
        webDriver.waitUntilElementIsLocated(By.id("create_link"));
    }

    public QuickCreateIssuePressure getQuickCreateIssuePressureDialog() {
        quickCreateButton.click();
        return pageBinder.bind(QuickCreateIssuePressure.class);
    }

}
