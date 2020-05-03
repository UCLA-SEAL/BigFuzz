package edu.ucla.cs.bigfuzz.customarray.applicable.MovieRating;

import edu.berkeley.cs.jqf.instrument.tracing.TraceLogger;
import edu.ucla.cs.bigfuzz.customarray.CustomArray;
import edu.ucla.cs.bigfuzz.dataflow.FilterEvent;
import edu.ucla.cs.bigfuzz.dataflow.MapEvent;
import edu.ucla.cs.bigfuzz.dataflow.ReduceByKeyEvent;
import janala.logger.inst.METHOD_BEGIN;
import janala.logger.inst.MemberRef;

import java.util.ArrayList;

public class MovieRatingCustomArray {

    private ArrayList<Object> list;

    public void MovieRatingCustomArray() {

    }
 public static ArrayList<map4> Map1(ArrayList<String> result){
        int callersLineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();

        int iid = CustomArray.class.hashCode(); // this should be a random value associated with a program location
        MemberRef method = new METHOD_BEGIN(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "()V"); // containing method

        ArrayList< map4> ans = new ArrayList<>();
        TraceLogger.get().emit(new MapEvent(iid, method, callersLineNumber));
for (String results: result){ans.add( map4.apply( results));}
return ans;
}
 public static ArrayList<map3> Map2(ArrayList< map4> result){
        int callersLineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();

        int iid = CustomArray.class.hashCode(); // this should be a random value associated with a program location
        MemberRef method = new METHOD_BEGIN(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "()V"); // containing method

        ArrayList< map3> ans = new ArrayList<>();
        TraceLogger.get().emit(new MapEvent(iid, method, callersLineNumber));
for ( map4 results: result){ans.add( map3.apply( results._1(),results._2()));}
return ans;
}
 public static ArrayList< map3> Filter1(ArrayList< map3> result){
        int callersLineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();

        int iid = CustomArray.class.hashCode(); // this should be a random value associated with a program location
        MemberRef method = new METHOD_BEGIN(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "()V"); // containing method

        ArrayList< map3> ans = new ArrayList<>();
        TraceLogger.get().emit(new FilterEvent(iid, method, callersLineNumber,1));
for ( map3 results: result){if ( filter2.apply( results._1(),results._2())) ans.add(results);}
return ans;
}
 public static ArrayList< map3> ReduceByKey1(ArrayList< map3> result){
        int callersLineNumber = Thread.currentThread().getStackTrace()[1].getLineNumber();

        int iid = CustomArray.class.hashCode(); // this should be a random value associated with a program location
        MemberRef method = new METHOD_BEGIN(Thread.currentThread().getStackTrace()[1].getClassName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "()V"); // containing method

        ArrayList< map3> ans = new ArrayList<>();
        TraceLogger.get().emit(new ReduceByKeyEvent(iid, method, callersLineNumber));
     int[][] array;  //prepare for reduce by key, array[][] records number list
     int[] num; // num[] records how many same item for one specific item
     num = new int [result.size()+1];
     array = new int [result.size()+1][result.size()+1];
     ArrayList<map3> results55 = new ArrayList<>();
     for (map3 result1: result){
         int a = 0; //flag
         for (map3 result2: results55){
             if (result1._1().equals(result2._1())) {
                 num[a] = num[a] + 1;
                 a = a - 99999;
                 break;
             }
             a = a+1;
         }
         if (a>=0) {
             array[results55.size()][num[results55.size()]] = result1._2();
             num[results55.size()]=1;
             results55.add(result1);
         }
         else {
             array[a+99999][num[a+99999]] = result1._2();
         }
     }

     int a=0;
     for (map3 results: results55)
     {
         int k = 0;
         for (int i=0;i<num[a];i++) {
             k= reduceByKey1.applyReduce(array[a]);
         }
         ans.add(new map3(results._1(),k));
         a=a+1;
     }
     return ans;
}
}