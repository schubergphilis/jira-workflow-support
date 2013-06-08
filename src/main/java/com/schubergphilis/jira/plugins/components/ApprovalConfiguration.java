package com.schubergphilis.jira.plugins.components;

import com.atlassian.jira.project.Project;

import java.util.List;

public interface ApprovalConfiguration {

    boolean isCorrectlyConfigured();

    String getSubtaskType();

    List<String> getEnabledProjectIds();

    void setProjectIds(List<String> asList);

    void setSubtaskType(String subTaskType);

    boolean isEnabledForProject(Project project);

}