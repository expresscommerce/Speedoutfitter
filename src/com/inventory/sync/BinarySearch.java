package com.inventory.sync;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Islam-uddin on 7/14/2017.
 */



public class BinarySearch {
    public static void main(String args[]){
        String html="jlsdf.vcxvljslfjsdl;fkjsdfsjdl;fksjfsdlf; </div> </div> </div> </div> ";
        String html1=html;
        html = getBrokenDivFix(html, html1);
        System.out.println(html);



    }

    private static String getBrokenDivFix(String html, String html1) {
        System.out.println(html1.length());
        html1=html1.replace(" ","");
        System.out.println(html1.length());
        html1=html1.substring(html1.length()-30,html1.length());
        String compareStr="</div></div></div></div></div>";
        System.out.println(html1.equals(compareStr));
        if(html1.equals(compareStr)==false){
            System.out.println(html1);
            String[] tokens=html1.split("</");
            System.out.println(Arrays.toString(tokens));

            if(tokens.length==5){
                html+="</div>";
            }
            if(tokens.length==4){
                html+="</div></div>";
            }
            if(tokens.length==3){
                html+="</div></div></div>";
            }
            if(tokens.length==2){
                html+="</div></div></div></div>";
            }
            if(tokens.length==1){
                html+="</div></div></div></div></div>";
            }
        }
        return html;
    }


    public static void main1(String args[]){
/*
        String select="3";
        String number="1234";
        String[] noArray={"1","2","3","4"};
        String output="";
        //getIndexed(number, select);
        output = getIndexedString(noArray,select);
        System.err.println("output :" + output);
*/

        ArrayList<String> color_list;
        SamplePredicate<String> filter;

        color_list = new ArrayList<> ();
        filter = new SamplePredicate<> ();

        filter.varc1 = "White";

        // use add() method to add values in the list
        color_list.add("White");
        color_list.add("Black");
        color_list.add("Red");
        color_list.add("White");
        color_list.add("Yellow");
        color_list.add("White");

        System.out.println("List of Colors");
        System.out.println(color_list);

        // Remove all White colors from color_list
        color_list.removeIf(filter);

        System.out.println("Color list, after removing White colors :");
        System.out.println(color_list);


    }

    private static void getTest() {
        System.out.println("getTest invoked..");

    }

    private static String getIndexedString(String[] noArray,String select) {
        String output="";
        boolean isfirst=true;
        for(int i=0; i<noArray.length; i++){
            String current=noArray[i];
            if(current.equals(select)){
                isfirst=false;
                continue;
            }

            //way 1
            if(isfirst) output=output+current;
            else        output=current+output;

            //way 2
            //output=(isfirst)? output+current:current+output;
        }
        return output;
    }


    private static String getIndexed(String number, String select) {
        String output="";
        if(select.length()==1) {
            String[] tokens = number.split(select);
            int length = number.split(select).length;
            String first = (length > 0) ? tokens[0] : "";
            String last = (length > 1) ? tokens[1] : "";
            output = last + first;
            if (output.equals(number)) {
                System.err.println("invalid selection :" + select);
            } else {
                System.err.println("output :" + output);
            }
        }else{
            System.err.println("invalid selection :" + select);
        }
        return output;
    }

}
