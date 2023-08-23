package com.gg.meta.ant.target;
import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.gg.meta.util.GGUtils;


public class DeleteCaseIntegrationLayout {

	static Logger log = Logger.getRootLogger();
	
	public static void main(String[] str) {
		String url = GGUtils.getSRCFolderURL() + "layouts\\";
		ArrayList<File> filesToDelete = new ArrayList<File>();
		File f1 = new File(url + "CaseInteraction-Case Feed Layout.layout"); 
		File f2 = new File(url + "Account-Account Layout.layout");
		File f3 = new File(url + "Goal-Goal Layout.layout");
		File f4 = new File(url + "DandBCompany-D%26B Company Layout.layout");
		File f5 = new File(url + "JobTracker-Job Tracker Layout - Winter %2716.layout");
		File f6 = new File(url + "Metric-Completion Metric Layout.layout");
		File f7 = new File(url + "Metric-Metric Layout.layout");
		File f8 = new File(url + "MetricDataLink-Metric Data Link Layout.layout");
		File f9 = new File(url + "Opportunity-Opportunity %28Marketing%29 Layout.layout");
		File f10 = new File(url + "Opportunity-Opportunity %28Sales%29 Layout.layout");
		File f11 = new File(url + "Opportunity-Opportunity %28Support%29 Layout.layout");
		File f12 = new File(url + "Opportunity-Opportunity Layout.layout");
		File f13 = new File(url + "SOSSession-SOS Session Layout.layout");
		File f14 = new File(url + "SOSSessionActivity-SOS Session Activity Layout.layout");
		File f15 = new File(url + "WorkCoaching-Coaching Layout.layout");
		File f16 = new File(url + "WorkPerformanceCycle-Performance Cycle Layout - Winter %2716.layout");
		File f17 = new File(url + "WorkCoaching-Coaching Layout 2%2E0.layout");
		File f18 = new File(url + "WorkFeedback-Feedback Layout.layout");
		File f19 = new File(url + "WorkFeedback-Feedback Layout - Summer %2715.layout");
		File f20 = new File(url + "WorkFeedback-Feedback Layout - Winter %2716.layout");
		File f21 = new File(url + "WorkFeedbackQuestion-Feedback Question Layout.layout");
		File f22 = new File(url + "WorkFeedbackQuestion-Feedback Question Layout - Summer %2715.layout");
		File f23 = new File(url + "WorkFeedbackQuestion-Feedback Question Layout - Winter %2716.layout");
		File f24 = new File(url + "WorkFeedbackQuestionSet-Feedback Question Set Layout.layout");
		File f25 = new File(url + "WorkFeedbackQuestionSet-Feedback Question Set Layout - Summer %2715.layout");
		File f26 = new File(url + "WorkFeedbackQuestionSet-Feedback Question Set Layout - Winter %2716.layout");
		File f27 = new File(url + "WorkFeedbackRequest-Feedback Request Layout.layout");
		File f28 = new File(url + "WorkFeedbackRequest-Feedback Request Layout - Summer %2715.layout");
		File f29 = new File(url + "WorkFeedbackRequest-Feedback Request Layout - Winter %2716.layout");
		File f30 = new File(url + "WorkFeedbackTemplate-Feedback Template Layout.layout");
		File f31 = new File(url + "WorkPerformanceCycle-Performance Cycle Layout.layout");
		File f32 = new File(url + "GoalLink-Goal Link Layout.layout");
		File f33 = new File(url + "JobTracker-Job Tracker Layout.layout");
		File f34 = new File(url + "JobTracker-Job Tracker Layout - Summer %2715.layout");
		File f35 = new File(url + "WorkCoaching-Coaching Layout 194.layout");
		File f36 = new File(url + "WorkPerformanceCycle-Performance Cycle Layout - Summer %2715.layout");
		File f37 = new File(url + "CollaborationGroup-Group Layout.layout");
		File f38 = new File(url + "ProfileSkill-Skill Layout.layout");
		File f39 = new File(url + "Quote-Quote Layout.layout");
		File f40 = new File(url + "ProfileSkillEndorsement-Endorsement Layout.layout");
		File f41 = new File(url + "ProfileSkillUser-Skill User Layout.layout");
		File f42 = new File(url + "WorkThanks-Thanks Layout.layout");
		File f43 = new File(url + "WorkBadgeDefinition-Badge Layout 192.layout");
		File f44 = new File(url + "WorkBadgeDefinition-Badge Definition Layout.layout");
		File f45 = new File(url + "WorkBadge-Badge Received Layout 192.layout");
		File f46 = new File(url + "WorkBadge-Badge Layout.layout");
		File f47 = new File(url + "WorkAccess-Access Layout.layout");
		File f48 = new File(url + "QuoteLineItem-Quote Line Item Layout.layout");
		File f49 = new File(url + "Campaign-Campaign Layout.layout");
		File f50 = new File(url + "Contact-Contact Layout.layout");
		File f51 = new File(url + "Event-Event Layout.layout");		
		File f52 = new File(url + "Lead-Lead Layout.layout");
		File f53 = new File(url + "Task-Task Layout.layout");
		File f54 = new File(url + "PartnerFundAllocation-Partner Fund Allocation Layout.layout");
		File f55 = new File(url + "SocialPost-Social Post Layout.layout");
		File f56 = new File(url + "PartnerFundClaim-Partner Fund Claim Layout.layout");
		File f57 = new File(url + "PartnerFundRequest-Partner Fund Request Layout.layout");
		File f58 = new File(url + "PartnerMarketingBudget-Partner Marketing Budget Layout.layout");
		File f59 = new File(url + "UserProvAccount-User Provisioning Account Layout.layout");
		File f60 = new File(url + "UserProvisioningLog-User Provisioning Log Layout.layout");
		File f61 = new File(url + "UserProvisioningRequest-User Provisioning Request Layout.layout");
		
		
		//File f62 = new File(url + "layouts/GrantBudgetCategory2__c-GrantBudgetCategory2 Layout.layout");
		File f63 = new File(url + "layouts/PartnerFundAllocation-Partner Fund Allocation Layout.layout");
		File f64 = new File(url + "layouts/PartnerFundClaim-Partner Fund Claim Layout.layout");
		File f65 = new File(url + "layouts/PartnerFundRequest-Partner Fund Request Layout.layout");
		File f66 = new File(url + "layouts/PartnerMarketingBudget-Partner Marketing Budget Layout.layout");
		//File f67 = new File(url + "layouts/ReviewQueueUser__c-Review Queue User Layout.layout");
		File f68 = new File(url + "layouts/SocialPost-Social Post Layout.layout");
		File f69 = new File(url + "layouts/UserProvAccount-User Provisioning Account Layout.layout");
		File f70 = new File(url + "layouts/UserProvisioningLog-User Provisioning Log Layout.layout");
		File f71 = new File(url + "layouts/UserProvisioningRequest-User Provisioning Request Layout.layout");
		
		
		filesToDelete.add(f71);
		filesToDelete.add(f70);
		filesToDelete.add(f69);
		filesToDelete.add(f68);
		//filesToDelete.add(f67);
		filesToDelete.add(f66);
		filesToDelete.add(f65);
		filesToDelete.add(f64);
		filesToDelete.add(f63);
		//filesToDelete.add(f62);
		
		filesToDelete.add(f61);
		filesToDelete.add(f60);
		filesToDelete.add(f59);
		filesToDelete.add(f58);
		filesToDelete.add(f57);
		filesToDelete.add(f56);
		filesToDelete.add(f55);
		filesToDelete.add(f54);
		filesToDelete.add(f53);
		filesToDelete.add(f52);
		filesToDelete.add(f51);
		filesToDelete.add(f50);
		filesToDelete.add(f49);
		filesToDelete.add(f48);
		filesToDelete.add(f47);
		filesToDelete.add(f46);
		filesToDelete.add(f45);
		filesToDelete.add(f44);
		filesToDelete.add(f43);
		filesToDelete.add(f42);
		filesToDelete.add(f41);
		filesToDelete.add(f40);
		filesToDelete.add(f39);
		filesToDelete.add(f38);
		filesToDelete.add(f37);
		filesToDelete.add(f36);
		filesToDelete.add(f35);
		filesToDelete.add(f34);
		filesToDelete.add(f33);
		filesToDelete.add(f32);
		filesToDelete.add(f31);
		filesToDelete.add(f30);
		filesToDelete.add(f29);
		filesToDelete.add(f28);
		filesToDelete.add(f27);
		filesToDelete.add(f26);
		filesToDelete.add(f25);
		filesToDelete.add(f24);
		filesToDelete.add(f23);
		filesToDelete.add(f22);
		filesToDelete.add(f21);
		filesToDelete.add(f20);
		filesToDelete.add(f19);
		filesToDelete.add(f18);
		filesToDelete.add(f17);
		filesToDelete.add(f16);
		filesToDelete.add(f15);
		filesToDelete.add(f14);
		filesToDelete.add(f13);
		filesToDelete.add(f12);
		filesToDelete.add(f11);
		filesToDelete.add(f10);
		filesToDelete.add(f9);
		filesToDelete.add(f8);
		filesToDelete.add(f7);
		filesToDelete.add(f6);
		filesToDelete.add(f5);
		filesToDelete.add(f4);
		filesToDelete.add(f3);
		filesToDelete.add(f2);
		filesToDelete.add(f1);
		
		
		for (File f : filesToDelete) {
			if (f.isFile() && f.exists()) {
				f.delete();
				log.info(f + " has been deleted");
			}
		}		
	}

}
