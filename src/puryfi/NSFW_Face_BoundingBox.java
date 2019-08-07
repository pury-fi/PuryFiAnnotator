/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package puryfi;

/**
 *
 * @author 0131
 */
public class NSFW_Face_BoundingBox extends NSFW_BoundingBox{
    
    String gender;
    String ethnicity;
    int age;
    
    public NSFW_Face_BoundingBox(String name, Double confidence, String gender, String ethnicity, int age, int[] box) {
        super(name, confidence, box);
        this.gender = gender;
        this.ethnicity = ethnicity;
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    
    
    public String getHeadline() {
        if(NSWFAPI.labelmodeComboBox.getSelectedIndex() == 1){
            String[] splited = getName().split("\\s+");
            String r = "";
            for (int i = 0; i < splited.length; i++) {
                if(!splited[i].isEmpty()){
                    r += splited[i].charAt(0);
                    if(splited[i].charAt(0) == 'A'){
                       r += splited[i].charAt(1); 
                    }
                }
            }
            Double confidence1 = getConfidence();
            return r + f.format(confidence1);
        }
         if(NSWFAPI.labelmodeComboBox.getSelectedIndex() == 2){
            return "";
        }
        return getName() + "\nAge: "+ age + " Race: "+ ethnicity.toLowerCase();
    }

   
    
    
     
   
}
