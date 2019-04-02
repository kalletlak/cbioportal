package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualStudyData extends SessionData implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
	private String name;

	private String description;

	private Set<VirtualStudySamples> studies;

	private StudyViewFilter studyViewFilter;

	private Float version = 1.0f;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<VirtualStudySamples> getStudies() {
		return studies;
	}

	public void setStudies(Set<VirtualStudySamples> studies) {
		this.studies = studies;
	}

	@Override
	public Set<String> getOrigin() {
		if (super.getOrigin() == null || super.getOrigin().size() == 0) {
			return studies.stream().map(map -> map.getId()).collect(Collectors.toSet());
		}
		return super.getOrigin();
	}

	public Float getVersion() {
		return version;
	}

	public void setVersion(Float version) {
		this.version = version;
	}

	public StudyViewFilter getStudyViewFilter() {
		return studyViewFilter;
	}

	public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
		this.studyViewFilter = studyViewFilter;
	}
	
}
