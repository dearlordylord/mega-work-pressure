package it;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.TestedProductFactory;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import org.junit.Test;
import ru.megaplan.jira.plugins.megaworkpressure.pageobjects.DashboardPagePressure;
import ru.megaplan.jira.plugins.megaworkpressure.pageobjects.PressureDialog;
import ru.megaplan.jira.plugins.megaworkpressure.pageobjects.QuickCreateIssuePressure;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TestViewProjects extends FuncTestCase {

    @Inject
    private PageBinder pageBinder;

    private static JiraTestedProduct jira = TestedProductFactory.create(JiraTestedProduct.class);

    @Test
    public void testThatThereIsOneProject() throws InterruptedException {
        HomePage home = jira.visit(LoginPage.class).loginAsSysAdmin(HomePage.class);
        DashboardPagePressure dashboardPage = jira.visit(DashboardPagePressure.class);
        QuickCreateIssuePressure quickCreateIssuePressure = dashboardPage.getQuickCreateIssuePressureDialog();
        quickCreateIssuePressure.chooseAssignee("firfi");
        PressureDialog pressureDialog = quickCreateIssuePressure.gotoPressureDialog();
        assert pressureDialog.isPresent();
        assert pressureDialog.isMainTablePresent();
    }

}
