package com.gg.meta.ant.target;

public class FindNonReferencedClassWrapper {
	public String actionHandlerClass;
	public String actionObjectName;
	public FindNonReferencedClassWrapper(){}
	public FindNonReferencedClassWrapper(String actionHandlerClass, String actionObjectName){
		this.actionHandlerClass = actionHandlerClass;
		this.actionObjectName = actionObjectName;
	}
}
