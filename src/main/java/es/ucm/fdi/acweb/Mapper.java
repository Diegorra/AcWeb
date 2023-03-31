package es.ucm.fdi.acweb;

import es.ucm.fdi.acweb.model.*;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.test.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;

import static es.ucm.fdi.acweb.model.FileTreeNodeWeb.ftnFromAc;
import static es.ucm.fdi.acweb.model.SourceWeb.sourceFromAc;
import static es.ucm.fdi.acweb.model.TestResultWeb.testResultFromAc;

@Component
public class Mapper {


    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public SourceSetWeb getSourceSetWeb(SourceSet set, AnalysisWeb a){
        SourceSetWeb sourceSetWeb = new SourceSetWeb();

        FileTreeNodeWeb ftnw = ftnFromAc(set.getFilteredTree(), sourceSetWeb, null);
        entityManager.persist(ftnw);

        sourceSetWeb.fromAc(ftnw, a);
        entityManager.persist(sourceSetWeb);
        return sourceSetWeb;
    }

    @Transactional
    public ArrayList<SubmissionWeb> getSubmissions(Analysis ac, AnalysisWeb analysis){
        ArrayList<SubmissionWeb> submissionWebs = new ArrayList<>();
        for(Submission sub : ac.getSubmissions()){
            SubmissionWeb submissionWeb= new SubmissionWeb();
            entityManager.persist(submissionWeb);
            ArrayList<SourceWeb> roots = new ArrayList<>();
            for(Submission.Source source : sub.getSources()){
                SourceWeb sourceWeb = sourceFromAc(source, submissionWeb);
                entityManager.persist(sourceWeb);
                roots.add(sourceWeb);
            }
            submissionWeb.fromAc(sub, analysis, roots);
            entityManager.persist(submissionWeb);
            submissionWebs.add(submissionWeb);
        }

        return submissionWebs;
    }

    public void persistData(AnalysisWeb analysisWeb, Analysis ac, ArrayList<String> keys){
        for(Submission sub : ac.getSubmissions()){
            SubmissionWeb subWeb = entityManager.createNamedQuery("SubmissionWeb.byInternalId", SubmissionWeb.class)
                    .setParameter("id", sub.getInternalId())
                    .setParameter("analysisId", analysisWeb.getId())
                    .getSingleResult();
            subWeb.setData(getTestsResultWebForSub(ac, sub, subWeb, keys));
            entityManager.merge(subWeb);
        }
        for(String i : keys){
            analysisWeb.getAppliedTestKey().add(i);
        }
        entityManager.merge(analysisWeb);
    }

    public ArrayList<TestResultWeb> getTestsResultWebForSub(Analysis ac, Submission sub, SubmissionWeb subWeb, ArrayList<String> keys){
        ArrayList<TestResultWeb> testResultWebs = new ArrayList<>();
        for(String testKey : keys){//para cada submission chequeamos si se ha aplicado alguno de los test, en cuyo caso lo persistimos
            if(ac.hasResultsForKey(testKey)){
                TestResultWeb testResultWeb = testResultFromAc(testKey, sub.getData(testKey), subWeb);
                entityManager.persist(testResultWeb);
                testResultWebs.add(testResultWeb);
            }
        }

        return  testResultWebs;
    }

    public ArrayList<String> getAppliedTest(HashSet<Test> test){
        ArrayList<String> appliedT = new ArrayList<>();
        Iterator<Test> i = test.iterator();

        while(i.hasNext()){
            appliedT.add(i.next().getTestKey());
        }

        return appliedT;
    }

    /*
    public ArrayList<TestResultWeb> getTestResult(Submission s, AnalysisWeb a){
        ArrayList<TestResultWeb> result = new ArrayList<>();
        for(String i : a.getAppliedTestKey()){
            TestResultWeb test = new TestResultWeb();
            test.setTestKey(i);
            test.setResult((List<Float>) s.getData(i));

            result.add(test);
        }

        return result;
    }
    */
}
