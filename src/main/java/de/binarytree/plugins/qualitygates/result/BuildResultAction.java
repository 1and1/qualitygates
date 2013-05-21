package de.binarytree.plugins.qualitygates.result;

import java.io.IOException;

import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.AbstractBuild;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import de.binarytree.plugins.qualitygates.GateEvaluator;
import de.binarytree.plugins.qualitygates.checks.Check;
import de.binarytree.plugins.qualitygates.checks.ManualCheck;

public class BuildResultAction implements ProminentProjectAction {

	private static final String MANUALLY_APPROVED = "Manually approved";
	private final String ICONS_PREFIX = "/plugin/qualitygates/images/24x24/";
	private GateEvaluator gateEvaluator;

	public BuildResultAction(GateEvaluator gateEvaluator) {
		this.gateEvaluator = gateEvaluator;
	}

	public GatesResult getGatesResult() {
		return this.gateEvaluator.getLatestResults();
	}

	public String getIconFileName() {
		return ICONS_PREFIX + "qualitygate_icon.png";
	}

	public String getDisplayName() {
		return "Execution Results of Quality Gates";
	}

	public String getUrlName() {
		return "qualitygates";
	}

	public void doApprove(StaplerRequest req, StaplerResponse res) throws IOException {

		GatesResult gatesResult = this.getGatesResult(); 
		GateResult unbuiltGate = this.getNextUnbuiltGate(gatesResult); 
		CheckResult unbuiltCheck = this.getNextUnbuiltCheck(unbuiltGate); 
		Check check = unbuiltCheck.getCheck(); 
		if(check instanceof ManualCheck){
		((ManualCheck) check).approve(); 	
		}else{
			throw new IllegalArgumentException("Next unbuilt check is no check which can be approved."); 
		}
		AbstractBuild build = req.findAncestorObject(AbstractBuild.class); 
		this.gateEvaluator.evaluate(build, null, null); 
		build.save(); 
		res.sendRedirect("."); 
		
	}

	public GateResult getNextUnbuiltGate(GatesResult gatesResult) {
		for(GateResult gateResult : gatesResult.getGateResults()){
			if(isPassedGate(gateResult)){
				continue; 
			}else if(isNotBuilt(gateResult)){
				return gateResult; 
			}else{
				return null; 
			}
		}
		return null;
	}

	private boolean isNotBuilt(GateResult gateResult) {
		return gateResult.getResult().equals(Result.NOT_BUILT);
	}

	private boolean isPassedGate(GateResult gateResult) {
		return gateResult.getResult().isBetterOrEqualTo(Result.UNSTABLE);
	}

	public CheckResult getNextUnbuiltCheck(GateResult gateResult) {
		for(CheckResult checkResult : gateResult.getCheckResults()){
			if(isNotBuilt(checkResult)){
				return checkResult; 
			}
		}
		return null; 
	}

	private boolean isNotBuilt(CheckResult checkResult) {
		return checkResult.getResult().equals(Result.NOT_BUILT);
	}
}
