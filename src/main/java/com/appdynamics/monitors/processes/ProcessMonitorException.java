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

import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class ProcessMonitorException extends TaskExecutionException {
	private static final long serialVersionUID = 1L;

    public ProcessMonitorException() {
    }

    public ProcessMonitorException(String message) {
        super(message);
    }

    public ProcessMonitorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessMonitorException(Throwable cause) {
        super(cause);
    }
}
