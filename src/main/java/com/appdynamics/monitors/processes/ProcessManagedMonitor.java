/**
 * Copyright 2013 AppDynamics
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appdynamics.monitors.processes;

import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.appdynamics.monitors.processes.ProcessMonitorContext.OperatingSystem.*;
import static com.appdynamics.monitors.processes.ProcessMonitorContext.OperatingSystem.getOperatingSystem;
import static java.lang.String.valueOf;

public class ProcessManagedMonitor extends AManagedMonitor {
    public static final String PROPERTIES_PATH_KEY = "properties-path";
    public static final String PROCESS_SAMPLE_INTERVAL_KEY = "process-sample-interval";
    public static final String PROCESS_IDENTITY_CHECK_INTERVAL_KEY = "process-identity-check-interval";
    public static final String METRIC_PATH_KEY = "metric-path";
    private ExecutorService executorService;
    private ProcessMonitorContext context;    
    private AtomicBoolean initialised;
    private Logger logger;
       
    public ProcessManagedMonitor() {
        this(Executors.newCachedThreadPool());
    }

    public ProcessManagedMonitor(ExecutorService executorService) {
        this.executorService = executorService;
        initialised = new AtomicBoolean(false);
    }

    public ProcessMonitorContext getProcessMonitorContext() {
        return context;
    }

    public void initialise(Map<String, String> taskArguments, TaskExecutionContext taskContext)
            throws TaskExecutionException {
        // TODO: rather than initialise, detect changes and apply, although be-careful of the periodic frequency
        // TODO: reading from the XML file is time consuming, maybe use a file watcher instead

        // mandatory
        if (!taskArguments.containsKey(PROPERTIES_PATH_KEY)) {
            throw new ProcessMonitorException("Monitor configuration is missing 'properties-path'");
        }

        if (getOperatingSystem() == null) {
            throw new ProcessMonitorException("Unsupported operating system");
        }

        logger = taskContext.getLogger();
        logger.info("Initialising process monitor");

        context = new ProcessMonitorContext();
        context.setLogger(taskContext.getLogger());
        context.setPropertiesPath(taskArguments.get(PROPERTIES_PATH_KEY));
        context.setOperatingSystem(getOperatingSystem());

        // optional
        if (taskArguments.containsKey(PROCESS_SAMPLE_INTERVAL_KEY)) {
            context.setProcessSampleInterval(taskArguments.get(PROCESS_SAMPLE_INTERVAL_KEY));
        }

        if (taskArguments.containsKey(PROCESS_IDENTITY_CHECK_INTERVAL_KEY)) {
            context.setProcessIdentityCheckInterval(taskArguments.get(PROCESS_IDENTITY_CHECK_INTERVAL_KEY));
        }

        if (taskArguments.containsKey(METRIC_PATH_KEY)) {
            context.setMetricPath(taskArguments.get(METRIC_PATH_KEY));
        }

        try {
            ProcessPropertiesParser parser = new ProcessPropertiesParser();
            for (ProcessProperties processProperties : parser.parseXML(context.getPropertiesPath())) {
                logger.info("Initialising process monitor for [" + processProperties + "]");
                ProcessMonitor processMonitor;
                if (context.getOperatingSystem() == Linux) {
                    processMonitor = new LinuxProcessMonitor(processProperties, context);
                } else {
                    processMonitor = new WindowsProcessMonitor(processProperties, context);
                }
                processMonitor.initialize();
                context.addProcessMonitors(processProperties, processMonitor);
            }
        }  catch (Exception e) {
            logger.error("Unexpected failure while initialising process monitoring", e);
            throw new ProcessMonitorException("Unexpected failure while process monitoring", e);
        }

        logger.info("Completed initialising process monitor with context [" + context + "]");
    }

    @Override
    public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext taskContext)
        throws TaskExecutionException {

        // make sure we initialise at least once
        if (initialised.compareAndSet(false, true)) {
            initialise(taskArguments, taskContext);
        }

        logger.trace("Process monitor execute started");

        try {
            Collection<ProcessMonitor> monitors = context.getProcessMonitors();
            Collection<Future<ProcessMetrics>> futures = new ArrayList<Future<ProcessMetrics>>(monitors.size());

            for (ProcessMonitor monitor : monitors) {
                logger.debug("Submitting process monitor [" + monitor + "]");
                futures.add(executorService.submit(monitor));
            }

            for (Future<ProcessMetrics> future : futures) {
                ProcessMetrics metrics = future.get();
                logger.debug("Printing process metrics [" + metrics + "]");
                printMetrics(metrics);
            }

        } catch (InterruptedException e) {
            logger.info("Process monitor interrupted");
        } catch (Exception e) {
            logger.error("Unexpected failure during process monitoring", e);
            throw new ProcessMonitorException("Unexpected failure during process monitoring", e);
        }

        logger.trace("Process monitor execute completed");
        return new TaskOutput("Process monitor execute completed");
    }

    @Override
    public void stop() {
        logger.info("Process monitor stopping");
        executorService.shutdownNow();
        super.stop();
    }

    public void printMetrics(ProcessMetrics processMetrics) {
        String processName = processMetrics.getName();
        logger.debug("Process monitor starting reporting metrics for " + processName);

        getMetricWriter(metricPath(processName, "CPU Utilization in Percent"),
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL)
                .printMetric(valueOf(processMetrics.getCpuPercent()));
        logger.debug("Metric reported: " + processName + ", CPU Utilization in Percent: " + processMetrics.getCpuPercent());

        getMetricWriter(metricPath(processName, "Memory Utilization in Percent"),
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL)
                .printMetric(valueOf(processMetrics.getMemoryPercent()));
        logger.debug("Metric reported: " + processName + ", Memory Utilization in Percent: " + processMetrics.getMemoryPercent());

        getMetricWriter(metricPath(processName, "Memory Utilization Absolute"),
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL)
                .printMetric(valueOf(processMetrics.getMemoryAbsolute()));
        logger.debug("Metric reported: " + processName + ", Memory Utilization Absolute: " + processMetrics.getMemoryAbsolute());

        getMetricWriter(metricPath(processName, "Number of Instances"),
                MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE,
                MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE)
                .printMetric(valueOf(processMetrics.getNumOfInstances()));
        logger.debug("Metric reported: " + processName + ", Number of Instances: " + processMetrics.getNumOfInstances());

        logger.debug("Process monitor completed reporting metrics for " + processName);
    }

    private String metricPath(String processName, String metricName) {
        return String.format("%s|%s|%s|%s",
            context.getMetricPath(), context.getOperatingSystem().getProcessGroupName(), processName, metricName);
    }
}
