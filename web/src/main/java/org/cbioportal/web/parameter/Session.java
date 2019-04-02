package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(Include.NON_NULL)
public class Session {

    private String id;
    private String source;
    private String type;
    private SessionData data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SessionData getData() {
        return data;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
    @JsonSubTypes({ @Type(value = StudyPageSettings.class, name = "settings"),
            @Type(value = VirtualStudyData.class, name = "virtual_study"),
            @Type(value = VirtualStudyData.class, name = "group") })
    public void setData(SessionData data) {
        this.data = data;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
