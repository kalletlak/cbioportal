package org.cbioportal.weblegacy.mixin;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SessionDataMixin {
    @JsonIgnore
    private String source;
    @JsonIgnore
    private String type;
    @JsonIgnore
    private String owner;
    @JsonIgnore
    private Long created;
    @JsonIgnore
    private Long lastUpdated;
    @JsonIgnore
    private Set<String> users;

}
