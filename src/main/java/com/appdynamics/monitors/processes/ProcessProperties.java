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

import java.util.regex.Pattern;

class ProcessProperties {
    private String name;
    private Pattern command;

    public ProcessProperties() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name can not be null");
        }
        this.name = name;
    }

    public Pattern getCommand() {
        return command;
    }

    public void setCommand(Pattern command) {
        if (command == null) {
            throw new IllegalArgumentException("Command can not be null");
        }
        this.command = command;
    }

    public boolean isValid() {
        return name != null && command != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessProperties)) {
            return false;
        }

        ProcessProperties that = (ProcessProperties) o;
        return command.toString().equals(that.command.toString()) && name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProcessProperties{name = [" + name + "], command = [" + command + "]}";
    }
}
