/* Pre-Processor for TopCoder submissions. (VB Version)
 * Used to determine the percentage of code that a submission uses.
 * If the percentage of unused code is more than 30% then a warning message is displayed.
 * main method demonstrates how this class should be used.
 *
 * Version: 1.3
 * Author: Dmitry Kamenetsky
 *
 * Known problems:
 *  - no discrimination is made between methods with the same name. This means that if
 * 		one method is seen then the other method is also counted as seen.
 *	- white space characters are not counted - even if they are inside Strings and characters.
 *	- do not use the following inside Strings and characters:
 *		'{', '}', "\n", "\t" and 'space' (see above problem)
 */

package com.topcoder.client.contestApplet.unusedCodeProcessor;

import java.util.StringTokenizer;
import java.util.Vector;

public class VBProcessor extends UCRProcessor {
    private String className;
    private String methodName;
    private String originalCode;
    private String code;
    private String[][] methods;
    private int methodsUsed;
    private String[][] classes;
    private int classesUsed;
    
    //check the validity of the code.
    //A warning message is returned if there is too much unused code.
    public String checkCode() throws RuntimeException {
        stripComments();  //store stripped version of originalCode into code
        getClassesAndMethods();
        iterateThroughMethods();
        printClasses();
        printMethods();
        
        //count used code;
        int usedCode=countClassNamesAndPublicVariables()+countCodeInMethods()+countImports();
        int totalLength=countTotalInOriginalCode();
        double usedPercentage=usedCode*1.0/totalLength;
        if(DEBUG) {
            System.out.println("Used Code: "+usedCode+" / "+totalLength+" = " +usedPercentage);
        }
        
        if((totalLength - usedCode) > CODE_LIMIT) {
            if (usedPercentage< (1 - CODE_PERCENT_LIMIT)) return INVALID_MESSAGE;
        }
        return "";
    }
        
    //create a preProcessor instance
    public void initialize(String className, String methodName, String originalCode) {
        this.className=className.toLowerCase();
        this.methodName=methodName.toLowerCase();
        this.originalCode=originalCode.toLowerCase();
        
        //ASSUME that there can be at most 100 methods
        methods=new String[100][5];		//name, class, start, finish, seen
        methodsUsed=0;
        //ASSUME that there can be at most 20 classes
        classes=new String[20][5];		//name, start, finish, seen, isComparator
        classesUsed=0;
    }
    
    
    //records all classes and methods of the program
    public void getClassesAndMethods() throws RuntimeException {
        Vector currentName=new Vector();
        Vector currentClass=new Vector();
        Vector currentIsComparator=new Vector();
        Vector currentStart=new Vector();
        Vector currentLocation=new Vector();
        currentLocation.add("nothing");
        int i=0;
        
        while(i<=code.length()-1) {
            //start of a Class
            if (currentLocation.elementAt(currentLocation.size()-1).equals("nothing") && code.indexOf("class",i)==i) {
                int classStart=code.lastIndexOf("\n",i)+1;
                if (classStart<0) classStart=0;
                int classEnd2=code.indexOf("\n",i);
                
                StringTokenizer classWords=new StringTokenizer(code.substring(i,classEnd2)," ");
                classWords.nextToken();
                String thisName=""+classWords.nextToken();
                
                String isComparator="";
                
                //					System.out.println("start of "+code.substring(classNameStart,classNameEnd)+" "+classNameStart+" "+classNameEnd);
                currentClass.add(thisName);
                currentName.add(thisName);
                currentStart.add(""+classStart);
                currentLocation.add("inClass");
                currentIsComparator.add(isComparator);
            }
            //start of a Function or Sub
            else if (currentLocation.elementAt(currentLocation.size()-1).equals("inClass") && (code.indexOf("function",i)==i || code.indexOf("sub")==i)) {
                int leftBracket=code.indexOf("(",i);
                int methodStart=code.lastIndexOf(" ",leftBracket)+1;
                if (methodStart<0) methodStart=0;
                
                String[] methodWords=split(code.substring(methodStart,leftBracket)," ");
                
                
//						System.out.println("start of "+methodWords[methodWords.length-1]);
                currentName.add(methodWords[methodWords.length-1]);
                currentStart.add(""+methodStart);
                currentLocation.add("inMethod");
            }
            //end of a Function or Sub
            else if (currentLocation.elementAt(currentLocation.size()-1).equals("inMethod") && (code.indexOf("end function",i)==i || code.indexOf("end sub",i)==i)) {
//						System.out.println("adding method "+			currentName.elementAt(currentName.size()-1)+"A");
                int methodEnd=-1;
                if (code.indexOf("end function",i)==i) {
                    methodEnd=i+"end function".length()-1;
                    i+=("end function".length()-1);
                } else {
                    methodEnd=i+"end sub".length()-1;
                    i+=("end sub".length()-1);
                }
                
                addMethod(""+currentName.elementAt(currentName.size()-1),""+currentClass.elementAt(currentClass.size()-1),""+currentStart.elementAt(currentStart.size()-1),""+methodEnd);
                currentName.removeElementAt(currentName.size()-1);
                currentStart.removeElementAt(currentStart.size()-1);
                currentLocation.removeElementAt(currentLocation.size()-1);
            }
            //end of a Class
            else if (currentLocation.elementAt(currentLocation.size()-1).equals("inClass") && code.indexOf("end class",i)==i) {
//						System.out.println("adding class "+			currentName.elementAt(currentName.size()-1)+"A");
                int classEnd=i+"end class".length()-1;
                i+=("end class".length()-1);
                addClass(""+currentName.elementAt(currentName.size()-1),""+currentStart.elementAt(currentStart.size()-1),""+classEnd,""+currentIsComparator.elementAt(currentIsComparator.size()-1));
                currentName.removeElementAt(currentName.size()-1);
                currentStart.removeElementAt(currentStart.size()-1);
                currentClass.removeElementAt(currentClass.size()-1);
                currentLocation.removeElementAt(currentLocation.size()-1);
                currentIsComparator.removeElementAt(currentIsComparator.size()-1);
            }
            
            i++;
        }
    }
    
    
    
    //record all method details. Throws an exception if array limit is reached
    public void addMethod(String name1, String name2, String start, String end) throws RuntimeException {
        try {
            methods[methodsUsed][0]=name1;
            methods[methodsUsed][1]=name2;
            methods[methodsUsed][2]=start;
            methods[methodsUsed][3]=end;
            if (methods[methodsUsed][0].equals(methodName))
                methods[methodsUsed][4]="seen";
            else
                methods[methodsUsed][4]="not seen";
            
            methodsUsed++;
        } catch(RuntimeException e) {
            System.out.println("methods array limit reached");
            throw e;
        }
    }
    
    
    //record all class details. Throws an exception if array limit is reached
    public void addClass(String name, String start, String end, String isComparator) throws RuntimeException {
        try {
            classes[classesUsed][0]=name;
            classes[classesUsed][1]=start;
            classes[classesUsed][2]=end;
            if (classes[classesUsed][0].equals(className))
                classes[classesUsed][3]="seen";
            else
                classes[classesUsed][3]="not seen";
            
            if (isComparator.equals("comparator") || isComparator.equals("comparable"))		//check if class is a Comparator class
                classes[classesUsed][4]="true";
            else
                classes[classesUsed][4]="false";
            
            classesUsed++;
        } catch(RuntimeException e) {
            System.out.println("classes array limit reached");
            throw e;
        }
    }
    
    //print details about classes
    public void printClasses() {
        if(DEBUG) {
            System.out.println("Printing Classes:");
            for (int i=0; i<classesUsed; i++)
                System.out.println(classes[i][0]+" "+classes[i][1]+" "+classes[i][2]+" "+classes[i][3]+" "+classes[i][4]);
        }
    }
    
    //print details about methods
    public void printMethods() {
        if(DEBUG) {
            System.out.println("Printing Methods:");
            for (int i=0; i<methodsUsed; i++)
                System.out.println(methods[i][0]+" "+methods[i][1]+" "+methods[i][2]+" "+methods[i][3]+" "+methods[i][4]);
        }
    }
    
    
    //This method iteratively finds used methods and classes
    public void iterateThroughMethods() {
        while(true) {
            boolean finished=true;
            
////////////////Find new methods//////////////////////
            for (int i=0; i<methodsUsed; i++) {
                if (methods[i][4].equals("not seen")) {
                    boolean found2=false;
                    for (int m=0; m<classesUsed; m++)
                        if (classes[m][0].equals(methods[i][1]) && classes[m][3].equals("seen")) {
                        found2=true;
                        break;
                        }
                    
                    if (found2) {
                        
                        boolean found=false;
                        
                        //check if this method is used by other methods that are already seen
                        for (int k=0; k<methodsUsed; k++) {
                            if (methods[k][4].equals("seen")) {
                                int cur=Integer.parseInt(methods[k][2]);
                                while(cur<=Integer.parseInt(methods[k][3])) {
                                    int methodStart=code.indexOf(methods[i][0]+"(",cur);
                                    if (methodStart<0) break;
                                    cur=methodStart+1;
                                    
                                    
                                    //method is used here
                                    if (methodStart<=Integer.parseInt(methods[k][3])) {
                                        methods[i][4]="seen";
                                        found=true;
                                        break;
                                    }
                                }
                                
                                if (found) {
                                    finished=false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            
/////////////////Find new classes///////////////////
            for (int i=0; i<classesUsed; i++) {
                if (classes[i][3].equals("not seen")) {
                    boolean found=false;
                    
                    //check if this class is used by other methods that are already seen
                    for (int k=0; k<methodsUsed; k++) {
                        if (methods[k][4].equals("seen")) {
                            int cur=Integer.parseInt(methods[k][2]);
                            while(cur<=Integer.parseInt(methods[k][3])) {
                                int classStart=code.indexOf("new "+classes[i][0],cur);
                                if (classStart<0) break;
                                cur=classStart+1;
                                
                                
                                //class is used here
                                if (classStart<=Integer.parseInt(methods[k][3])) {
                                    classes[i][3]="seen";
                                    found=true;
                                    
                                    //if this is a comparator class then make all its methods seen
                                    if (classes[i][4].equals("true"))
                                        for (int m=0; m<methodsUsed; m++)
                                            if (methods[m][1].equals(classes[i][0])) methods[m][4]="seen";
                                    
                                    break;
                                }
                            }
                            
                            if (found) {
                                finished=false;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (finished) break;
        }
    }
    
    
    //counts the amount of imported code
    public int countImports() {
        int cur=0;
        int count=0;
        
        while(true) {
            int importStart=code.indexOf("imports",cur);
            int nextEnter=code.indexOf("\n",cur);
            if (importStart<0 || importStart>nextEnter) break;
            
            cur=nextEnter+1;
            count+=(nextEnter-importStart-1);		//don't count space between 'Imports' and 'System'
        }
        
        return count;
    }
    
    
    //counts the length of class names of seen classes
    public int countClassNamesAndPublicVariables() {
        int count=0;
        
        for (int i=0; i<classesUsed; i++) {
            if (classes[i][3].equals("seen")) {
                int classTotal=0;
                for (int k=Integer.parseInt(classes[i][1]); k<=Integer.parseInt(classes[i][2]); k++)
                    if (code.charAt(k)!=' ' && code.charAt(k)!='\t' && code.charAt(k)!='\n' && code.charAt(k)!='\r')
                        classTotal++;
                
                int innerClasses=0;
                for (int m=0; m<classesUsed; m++)
                    if (m!=i && Integer.parseInt(classes[m][1])>Integer.parseInt(classes[i][1]) && Integer.parseInt(classes[m][2])<Integer.parseInt(classes[i][2]))
                        for (int k=Integer.parseInt(classes[m][1]); k<=Integer.parseInt(classes[m][2]); k++)
                            if (code.charAt(k)!=' ' && code.charAt(k)!='\t' && code.charAt(k)!='\n' && code.charAt(k)!='\r')
                                innerClasses++;
                
                int innerMethods=0;
                for (int m=0; m<methodsUsed; m++)
                    if (methods[m][1].equals(classes[i][0]) && Integer.parseInt(methods[m][2])>Integer.parseInt(classes[i][1]) && Integer.parseInt(methods[m][3])<Integer.parseInt(classes[i][2]))
                        for (int k=Integer.parseInt(methods[m][2]); k<=Integer.parseInt(methods[m][3]); k++)
                            if (code.charAt(k)!=' ' && code.charAt(k)!='\t' && code.charAt(k)!='\n' && code.charAt(k)!='\r')
                                innerMethods++;
                
                count+=(classTotal-innerClasses-innerMethods);
            }
        }
        
        return count;
    }
    
    //counts all the code inside methods that have been seen
    public int countCodeInMethods() {
        int count=0;
        
        for (int i=0; i<methodsUsed; i++)
            if (methods[i][4].equals("seen"))
                for (int k=Integer.parseInt(methods[i][2]); k<=Integer.parseInt(methods[i][3]); k++)
                    if (code.charAt(k)!=' ' && code.charAt(k)!='\t' && code.charAt(k)!='\n' && code.charAt(k)!='\r')
                        count++;
        
        
        return count;
    }
    
    //count all non-space characters in the submission
    public int countTotalInOriginalCode() {
        int count=0;
        
        for (int i=0; i<originalCode.length(); i++)
            if (originalCode.charAt(i)!=' ' && originalCode.charAt(i)!='\t' && originalCode.charAt(i)!='\n' && originalCode.charAt(i)!='\r')
                count++;
        
        return count;
    }
    
    public void stripComments() {
        code="";
        boolean inComment=false;
        
        for (int i=0; i<originalCode.length(); i++) {
            if ((int)(originalCode.charAt(i))==39 && !inComment)			// ' character is the start of a comment
                inComment=true;
            else if (originalCode.charAt(i)=='\n' && inComment)
                inComment=false;
            else if (!inComment)
                code+=originalCode.charAt(i);
        }
    }
}

