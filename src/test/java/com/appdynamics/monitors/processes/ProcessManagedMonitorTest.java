package com.appdynamics.monitors.processes;

import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProcessManagedMonitorTest {
    private ProcessManagedMonitor processManagedMonitor;
    private Map<String, String> taskArguments;
    @Mock
    private TaskExecutionContext taskContext;
    @Mock
    private Logger logger;

    @Before
    public void before() throws Exception {
        when(taskContext.getLogger()).thenReturn(logger);
        taskArguments = new HashMap<String, String>();
        processManagedMonitor = new ProcessManagedMonitor();
    }

    @Test
    public void initialiseShouldInitialiseWithMinimumArguments() throws TaskExecutionException {
        // arrange
        Map<String, String> taskArguments = new HashMap<String, String>();
        taskArguments.put(ProcessManagedMonitor.PROPERTIES_PATH_KEY, "conf/properties.xml");

        // act
        processManagedMonitor.initialise(taskArguments, taskContext);

        // assert
        ProcessMonitorContext context = processManagedMonitor.getProcessMonitorContext();
        assertThat(context, notNullValue());
        assertThat(context.getPropertiesPath(), is("conf/properties.xml"));
        assertThat(context.getProcessSampleInterval(), is(1));
        assertThat(context.getProcessIdentityCheckInterval(), is(60));
        assertThat(context.getOperatingSystem(), notNullValue());
        assertThat(context.getMetricPath(), is("Custom Metrics"));
        assertThat(context.getProcessMonitors(), notNullValue());
    }

    @Test
    public void execute() throws Exception {

    }
}
