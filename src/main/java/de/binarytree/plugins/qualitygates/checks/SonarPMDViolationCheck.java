package de.binarytree.plugins.qualitygates.checks;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.DataBoundConstructor;

public class SonarPMDViolationCheck extends SonarCheck {

	private String threshold; 
	@DataBoundConstructor
	public SonarPMDViolationCheck(String threshold){
	 this.threshold = threshold; 	
	}
	public void setThreshold(final String threshold){
		System.out.println("Setting PMD threshold"); 
		this.threshold = threshold; 
	}
	public String getThreshold(){
		return this.threshold; 
	}
	
	@Override
	public String toString(){
		return super.toString() +"[PMD <= " + this.getThreshold() + "]"; 
	}
	
	@Override
	public Result doCheck(AbstractBuild build, BuildListener listener,
			Launcher launcher) {
		return Result.FAILURE;
	}

    @Override
    public SonarPMDViolationCheckDescriptor getDescriptor() {
        return (SonarPMDViolationCheckDescriptor)super.getDescriptor();
    }
	
    @Extension
    public static final class SonarPMDViolationCheckDescriptor extends SonarCheckDescriptor{

		@Override
		public String getDisplayName() {
			System.out.println("getDisplayName in PMD"); 
			return "Quality Gate: Sonar PMD Violation Check";
			
		}
    	
    	
    }
}