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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class LinuxProcessMonitor extends AbstractProcessMonitor {
    private static final int PID_WORD_POSITION = 1;
    private static final int CPU_PERCENT_POSITION = 6;
    private static final int MEMORY_ABSOLUTE_POSITION = 11;
    private static final int MEMORY_PERCENT_POSITION = 12;
    private List<String> processes;
    private long lastProcessIdentityCheck;

    public LinuxProcessMonitor(ProcessProperties properties, ProcessMonitorContext context) {
        super(properties, context);
        processes = new ArrayList<String>(2);
        lastProcessIdentityCheck = 0;
    }

    public void initialize() throws ProcessMonitorException {
        identifyProcesses();
    }

    public ProcessMetrics processMetrics() throws ProcessMonitorException {
        // periodically check if new processes that match the properties command are present,
        // this is useful if processes are (re)started, killed or additional processes added
        if (shouldCheckProcessIdentities()) {
            identifyProcesses();
        }

        ProcessMetrics processMetrics = new ProcessMetrics(getProperties().getName());

        if (!processes.isEmpty()) {
            // build the pidstat command to execute
            StringBuilder pidStatCommand = new StringBuilder();
            pidStatCommand.append("pidstat -ruh ").append(getContext().getProcessSampleInterval()).append(" 1 -p ");

            for (int i = 0; i < processes.size(); i++) {
                pidStatCommand.append(processes.get(i));
                if (i + 1 < processes.size()) {
                    pidStatCommand.append(",");
                }
            }

            // execute the pidstat command (run for sample period for 1 iteration)
            List<String> processLines = execute(new String[] {
                    "/bin/sh" ,
                    "-c",
                    pidStatCommand.toString()});

            // the fist 3 lines of pidstat need contain metadata, anything after that is valuable
            if (processLines.size() > 3) {
                for (int i = 3; i < processLines.size(); i++) {
                    String[] processWords = processLines.get(i).split("\\s+");
                    processMetrics.addCpuPercent(getProcessWord(processWords, CPU_PERCENT_POSITION))
                        .addMemoryAbsolute(getProcessWord(processWords, MEMORY_ABSOLUTE_POSITION))
                        .addMemoryPercent(getProcessWord(processWords, MEMORY_PERCENT_POSITION))
                        .incrementNumOfInstances();
                }
            } else {
                // the processes are no longer there, reset the process identity check interval
                logger.info("Processes " + processes + " are no longer present, re-checking identities");
                lastProcessIdentityCheck = 0;
            }
        }  else {
            logger.debug("No processes have been identified that match command [" + getProperties().getCommand() + "]");
        }

        return processMetrics;
    }

    private boolean shouldCheckProcessIdentities() {
        return (lastProcessIdentityCheck + (getContext().getProcessIdentityCheckInterval() * 1000))
            <= System.currentTimeMillis();
    }

    private void identifyProcesses() throws ProcessMonitorException {
        // need to run it as s bash because pipe is treated as a separate process
        Collection<String> processLines = execute(new String[] {
                "/bin/sh" ,
                "-c",
                String.format("ps auxww | grep '%s' | grep -v 'grep'", getProperties().getCommand())});

        processes.clear();
        for (String processLine : processLines) {
            String[] processWords = processLine.split("\\s+");
            String pid = getProcessWord(processWords, PID_WORD_POSITION);
            logger.info("Identified PID ["+ pid + "] matching command [" + getProperties().getCommand() +"]");
            processes.add(pid);
        }

        lastProcessIdentityCheck = System.currentTimeMillis();
    }

    private String getProcessWord(String[] processWords, int position) throws ProcessMonitorException {
        if (processWords.length >= position) {
            logger.trace("Identified process word [" + processWords[position] + "] at position [" + position + "]");
            return processWords[position];
        }

        throw new ProcessMonitorException("Process output for line " + Arrays.toString(processWords)
            + " is not in the expected format. Position " + position + " is missing.");
    }

    @Override
    public String toString() {
        return "LinuxProcessMonitor{" +
                "processes=" + processes +
                ", properties=" + getProperties() +
                '}';
    }
}
