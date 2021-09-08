package org.gcube.nlphub.infra;

import java.util.List;

public class Algorithm {
	public String name;
	public String type;
	public String id;
	public List<String> annotations;
	public String language;
	public String outputTypeDescription;
	public String outputType;
	public String status;
	public String parameterName;
	public boolean supportsAnnotationList = false;
	
	@Override
	public String toString() {
	
		return name+";"+type+";"+id+";"+annotations+";"+language+";"+outputTypeDescription+";"+outputType+";"+status+";"+parameterName;
	}
}
