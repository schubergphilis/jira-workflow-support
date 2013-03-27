package ut.com.schubergphilis.jira.plugins;

import com.schubergphilis.jira.plugins.MyPluginComponent;
import com.schubergphilis.jira.plugins.MyPluginComponentImpl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}