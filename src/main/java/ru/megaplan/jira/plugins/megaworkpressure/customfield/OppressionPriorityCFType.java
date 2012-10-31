package ru.megaplan.jira.plugins.megaworkpressure.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.imports.project.customfield.NoTransformationCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.AbstractCustomFieldType;
import com.atlassian.jira.issue.customfields.impl.AbstractMultiCFType;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.*;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.velocity.NumberTool;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ru.megaplan.jira.plugins.megaworkpressure.customfield.util.MegaPriority;
import ru.megaplan.jira.plugins.megaworkpressure.customfield.util.MegaPriorityComparator;
import ru.megaplan.jira.plugins.megaworkpressure.customfield.util.PriorityValueSerializer;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: firfi
 * Date: 01.08.12
 * Time: 18:26
 * To change this template use File | Settings | File Templates.
 */
public class OppressionPriorityCFType extends AbstractSingleFieldType<Double> implements SortableCustomField<Double>, ProjectImportableCustomField, RestAwareCustomFieldType {


    private final DoubleConverter doubleConverter;
    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final WebResourceManager webResourceManager;

    public OppressionPriorityCFType(final CustomFieldValuePersister customFieldValuePersister, final DoubleConverter doubleConverter, final GenericConfigManager genericConfigManager, WebResourceManager webResourceManager)
    {
        super(customFieldValuePersister, genericConfigManager);
        this.doubleConverter = doubleConverter;
        this.webResourceManager = webResourceManager;
        projectCustomFieldImporter = new NoTransformationCustomFieldImporter();
    }

    @NotNull
    @Override
    protected PersistenceFieldType getDatabaseType()
    {
        return PersistenceFieldType.TYPE_DECIMAL;
    }

    public String getStringFromSingularObject(final Double customFieldObject)
    {
        return doubleConverter.getString(customFieldObject);
    }

    public Double getSingularObjectFromString(final String string) throws FieldValidationException
    {
        return doubleConverter.getDouble(string);
    }

    @Override
    public String getChangelogValue(final CustomField field, final Double value)
    {
        if (value == null)
        {
            return "";
        }
        else
        {
            return doubleConverter.getStringForChangelog(value);
        }
    }


    public int compare(@NotNull final Double customFieldObjectValue1, @NotNull final Double customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        return customFieldObjectValue1.compareTo(customFieldObjectValue2);
    }

    @Override
    protected Object getDbValueFromObject(final Double customFieldObject)
    {
        return customFieldObject;
    }

    @Override
    protected Double getObjectFromDbValue(@NotNull final Object databaseValue) throws FieldValidationException
    {
        return (Double) databaseValue;
    }

    @NotNull
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        map.put("numberTool", new NumberTool(getI18nBean().getLocale()));
        return map;
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitNumber(this);
        }

        return super.accept(visitor);
    }



    public interface Visitor<X> extends VisitorBase<X>
    {
        X visitNumber(OppressionPriorityCFType numberCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.custom(JsonType.NUMBER_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Double number = getValueFromIssue(field, issue);
        return new FieldJsonRepresentation(new JsonData(number));
    }

}
