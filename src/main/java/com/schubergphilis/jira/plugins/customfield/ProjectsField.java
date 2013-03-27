package com.schubergphilis.jira.plugins.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.AbstractMultiCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ErrorCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ProjectsField extends AbstractMultiCFType<Long> {

    private static final Logger log = LoggerFactory.getLogger(ProjectsField.class);

    private ProjectManager projectManager;

    public ProjectsField(ProjectManager projectManager, CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager) {
        super(customFieldValuePersister, genericConfigManager);
        this.projectManager = projectManager;
    }

    /**
     * This is where the different aspects of a custom field such as
     * having a default value or other config items are set.
     */
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new ProjectCategoryConfigurationItem(projectManager));
        return configurationItemTypes;
    }

    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue,
            final CustomField field,
            final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        // This method is also called to get the default value, in
        // which case issue is null so we can't use it
        if (issue == null) {
            return map;
        }

        FieldConfig fieldConfig = field.getRelevantConfig(issue);

        Long projectCategory = DAO.getProjectCategory(fieldConfig);

        map.put("projectCategory", getProjectCategory(projectCategory));
        map.put("projects", getProjects(projectCategory));
        map.put("selectedProjects", getSelectedProjects((ArrayList<Long>) field.getValue(issue)));
        return map;
    }

    private Object getProjectCategory(Long projectCategory) {
        return projectManager.getProjectCategoryObject(projectCategory);
    }

    private Collection<Project> getProjects(Long projectCategory) {
        if (projectCategory < 1) {
            return projectManager.getProjectObjects();
        } else {
            return projectManager.getProjectObjectsFromProjectCategory(projectCategory);
        }
    }

    private Collection<Project> getSelectedProjects(ArrayList<Long> projectIds) {
        ArrayList<Project> answer = new ArrayList<Project>();
        if (projectIds != null) {
            for (Long projectId : projectIds) {
                answer.add(getProject(projectId));
            }
        }
        return answer;
    }

    private Project getProject(Long projectId) {
        return projectManager.getProjectObj(projectId);
    }

    @Override
    public String getStringFromSingularObject(Long singularObject) {
        return singularObject.toString();
    }

    @Override
    public Long getSingularObjectFromString(String string) throws FieldValidationException {
        return Long.parseLong(string);
    }

    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    @Override
    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config) {}

    @Override
    public Collection<Long> getValueFromCustomFieldParams(CustomFieldParams parameters) throws FieldValidationException {
        Collection<String> projectIds = parameters.getAllValues();
        Collection<Long> answer = new ArrayList<Long>();
        for (String next : projectIds) {
            answer.add(Long.parseLong(next));
        }
        return answer;
    }

    @Override
    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters) {
        final Collection ids = parameters.getValuesForNullKey();
        if ((ids == null) || ids.isEmpty()) {
            return null;
        } else {
            return ids;
        }
    }

    @Override
    protected Comparator<Long> getTypeComparator() {
        return null;
    }

    @Override
    protected Object convertTypeToDbValue(Long value) {
        return value.doubleValue();
    }

    @Override
    protected Long convertDbValueToType(Object dbValue) {
        return ((Double) dbValue).longValue();
    }

}
