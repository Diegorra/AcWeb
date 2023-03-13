package es.ucm.fdi.acweb;

import es.ucm.fdi.acweb.model.*;
import es.ucm.fdi.ac.Analysis;
import es.ucm.fdi.ac.SourceSet;
import es.ucm.fdi.ac.Submission;
import es.ucm.fdi.ac.test.Test;

import java.util.*;

public class Mapper {


    public AnalysisWeb getAnaysisEntity(Analysis ac){
        AnalysisWeb analysis = new AnalysisWeb();

        //analysis.setSourceSet(getSourceSetEntity(, analysis));
        //analysis.setSubs(getSubmissionsEntity(ac.getSubmissions()/*, analysis*/));
        //analysis.setAppliedTestKey(getAppliedTest(ac.getAppliedTests()));


        return analysis;
    }

    public SourceSetWeb getSourceSetEntity(SourceSet set, AnalysisWeb a){
        SourceSetWeb sources = new SourceSetWeb();
        sources.setAnalysis(a);
        //sources.setSourceRoots();

        return sources;
    }

    public ArrayList<SubmissionWeb> getSubmissionsEntity(Submission[] sub/*, AnalysisEntity a*/){
        ArrayList<SubmissionWeb> subs = new ArrayList<>();
        for(Submission i : sub){

            SubmissionWeb s = new SubmissionWeb();
            //s.setAnalysis(a);
            s.setHash(i.getHash());
            s.setInternalId(i.getInternalId());
            s.setOriginalPath(i.getOriginalPath());
            s.setHashUpToDate(false);
            s.setSourceRoots(getSourceEntity(i.getSources()/*, s*/));
            //s.setData(getTestResult(i, a));

            subs.add(new SubmissionWeb());
        }
        return subs;
    }

    public ArrayList<String> getAppliedTest(HashSet<Test> test){
        ArrayList<String> appliedT = new ArrayList<>();
        Iterator<Test> i = test.iterator();

        while(i.hasNext()){
            appliedT.add(i.next().getTestKey());
        }

        return appliedT;
    }

    public ArrayList<SourceWeb> getSourceEntity(ArrayList<Submission.Source> source/*, SubmissionEntity sub*/){
        ArrayList<SourceWeb> sources = new ArrayList<>();
        for(Submission.Source i : source){
            SourceWeb s = new SourceWeb();
            //s.setSub(sub);
            s.setCode(i.getCode());
            s.setFileName(i.getFileName());

            sources.add(s);

        }
        return sources;
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
