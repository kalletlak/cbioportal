package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class SessionData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String owner = "anonymous";

    private Set<String> origin = new HashSet<>();

    private Long created = System.currentTimeMillis();
    
    private Long lastUpdated = System.currentTimeMillis();
    
    private Set<String> users = new HashSet<>();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Set<String> getOrigin() {
        return origin;
    }

    public void setOrigin(Set<String> origin) {
        this.origin = origin;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Set<String> getUsers() {
        return users;
    }

    public void setUsers(Set<String> users) {
        this.users = users;
    }
    
}
