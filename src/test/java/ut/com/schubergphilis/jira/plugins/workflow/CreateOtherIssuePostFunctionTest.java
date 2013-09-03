package ut.com.schubergphilis.jira.plugins.workflow;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.ProjectManager;
import com.schubergphilis.jira.plugins.workflow.CreateOtherIssuePostFunction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CreateOtherIssuePostFunctionTest {
    public static final String MESSAGE = "my message";

    protected CreateOtherIssuePostFunction function;
    protected MutableIssue issue;

    @Before
    public void setup() {
        CustomFieldManager customFieldManager = mock(CustomFieldManager.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        function = new CreateOtherIssuePostFunction(projectManager, customFieldManager) {
            protected MutableIssue getIssue(Map transientVars) throws DataAccessException {
                return issue;
            }
        };
    }

    @Test
    @Ignore
    public void testNullMessage() throws Exception {
        Map transientVars = Collections.emptyMap();

        function.execute(transientVars, null, null);

        assertEquals("message should be empty", "", issue.getDescription());
    }

    @Test
    @Ignore
    public void testEmptyMessage() throws Exception {
        Map transientVars = new HashMap();
        transientVars.put("messageField", "");
        function.execute(transientVars, null, null);

        assertEquals("message should be empty", "", issue.getDescription());
    }

    @Test
    @Ignore
    public void testValidMessage() throws Exception {
        Map transientVars = new HashMap();
        transientVars.put("messageField", MESSAGE);
        function.execute(transientVars, null, null);

        assertEquals("message not found", MESSAGE, issue.getDescription());
    }
}
