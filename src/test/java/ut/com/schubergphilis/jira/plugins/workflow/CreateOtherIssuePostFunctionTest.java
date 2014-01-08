package ut.com.schubergphilis.jira.plugins.workflow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.ProjectManager;
import com.schubergphilis.jira.plugins.workflow.CreateOtherIssuePostFunction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CreateOtherIssuePostFunctionTest
{
    public static final String MESSAGE = "my message";

    protected CreateOtherIssuePostFunction function;
    protected MutableIssue issue;

    @Before
    public void setup() {

//        issue = createPartialMockedIssue();
//        issue.setDescription("");

        CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        IssueFactory issueFactory = mock(IssueFactory.class);
        IssueManager issueManager = mock(IssueManager.class);
        function = new CreateOtherIssuePostFunction(projectManager, customFieldManager, issueManager, issueFactory) {
            protected MutableIssue getIssue(Map transientVars) throws DataAccessException {
                return issue;
            }
        };
    }

    @Test
    @Ignore
    public void testNullMessage() throws Exception
    {
        Map transientVars = Collections.emptyMap();

        function.execute(transientVars,null,null);

        assertEquals("message should be empty","",issue.getDescription());
    }

    @Test
    @Ignore
    public void testEmptyMessage() throws Exception
    {
        Map transientVars = new HashMap();
        transientVars.put("messageField","");
        function.execute(transientVars,null,null);

        assertEquals("message should be empty","",issue.getDescription());
    }

    @Test
    @Ignore
    public void testValidMessage() throws Exception
    {
        Map transientVars = new HashMap();
        transientVars.put("messageField",MESSAGE);
        function.execute(transientVars,null,null);

        assertEquals("message not found",MESSAGE,issue.getDescription());
    }

//    private MutableIssue createPartialMockedIssue() {
//        GenericValue genericValue = mock(GenericValue.class);
//        IssueManager issueManager = mock(IssueManager.class);
//        ProjectManager projectManager = mock(ProjectManager.class);
//        VersionManager versionManager = mock(VersionManager.class);
//        IssueSecurityLevelManager issueSecurityLevelManager = mock(IssueSecurityLevelManager.class);
//        ConstantsManager constantsManager = mock(ConstantsManager.class);
//        SubTaskManager subTaskManager = mock(SubTaskManager.class);
//        AttachmentManager attachmentManager = mock(AttachmentManager.class);
//        LabelManager labelManager = mock(LabelManager.class);
//        ProjectComponentManager projectComponentManager = mock(ProjectComponentManager.class);
//        UserManager userManager = mock(UserManager.class);
//
//        return new IssueImpl(genericValue, issueManager, projectManager, versionManager, issueSecurityLevelManager, constantsManager, subTaskManager, attachmentManager, labelManager, projectComponentManager, userManager);
//    }

}
