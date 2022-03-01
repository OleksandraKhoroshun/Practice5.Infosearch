import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BSBI {
    ArrayList<String> voc_terms;
    ArrayList<Integer> voc_docs;
    ArrayList<String> inverted_terms;
    ArrayList< ArrayList<Integer>> inverted_docs;

    private int block_size = 100000;
    private int block_size_count = 0;
    private int block_count = 0;

    double Collection_size=0;
    double end_block_size=0;

    public BSBI(String folder){
        voc_terms = new ArrayList<>();
        voc_docs = new ArrayList<>();
        inverted_terms = new ArrayList<>();
        inverted_docs = new ArrayList<>();

        File dir = new File(folder);
        File[] files = dir.listFiles();

        int doc=-1;
        for (File file : files) {
            doc++;
            Collection_size+=file.length()/1024.0;

            if(file.isFile()) {

                BufferedReader br = null;
                String line;
                try {
                    br = new BufferedReader(new FileReader(file));
                    while ((line = br.readLine()) != null) {
                        addLine(doc,line);
                    }

                }catch(IOException e) {
                    System.out.println(e);
                }
                finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if(block_size_count<block_size)second_step();
        merge();
    }

    private void addLine(int doc, String line){
        if(line.equals("")) return;
        String[] temp = line.split("[^a-zA-Z0-9_]+");
        for(int i=0;i<temp.length;i++){
            if(temp[i].matches("[a-zA-Z0-9_]+")) {
                addWord(temp[i].toLowerCase(),doc);
            }
        }
    }

    public void addWord(String word,int doc){
        block_size_count++;
            voc_terms.add(word);
            voc_docs.add(doc);
        if(block_size_count==block_size){
            second_step();
        }
    }

    public void second_step(){
        invert();
        block_count++;
        block_size_count=0;
        voc_terms = new ArrayList<>();
        voc_docs = new ArrayList<>();
        inverted_terms = new ArrayList<>();
        inverted_docs = new ArrayList<>();
    }
    public void invert(){
        ArrayList<String> res_list = new ArrayList();
        for(String p : voc_terms){
            res_list.add(p);
        }
        Collections.sort(res_list);
        ArrayList<String> temp_terms = new ArrayList<>();
        ArrayList< ArrayList<Integer>> temp_docs = new ArrayList<>();

        for(int i=0;i<res_list.size();i++){

            if(!temp_terms.contains(res_list.get(i))) {
                ArrayList<Integer> temp_list = new ArrayList<>();
                for(int j=0;j<voc_terms.size();j++){
                    if(voc_terms.get(j).equals(res_list.get(i))) {
                        if(!temp_list.contains(voc_docs.get(j)))temp_list.add(voc_docs.get(j));}
                }
                temp_terms.add(res_list.get(i));
                temp_docs.add(temp_list);

            }
        }
        inverted_terms = temp_terms;
        inverted_docs = temp_docs;
        write();
    }

    public void write(){
        BufferedWriter bw = null;
        try {

            FileWriter fw = new FileWriter("C:\\practice5\\block"+block_count+".txt");
            bw = new BufferedWriter(fw);
            for(int i=0;i<inverted_terms.size();i++)
            bw.write(inverted_terms.get(i)+inverted_docs.get(i)+"\n");

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }
    }

    public void merge(){
        BufferedReader[] readers = new BufferedReader[block_count];
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter("C:\\practice5\\block_final.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bw = new BufferedWriter(fw);

        try {
            long longest_file_length=0;
            int longest_file=-1;
            for(int i=0;i<block_count;i++) {
                File f = new File("C:\\practice5\\block" + i + ".txt");
                readers[i] = new BufferedReader(new FileReader(f));
                if(f.length()>longest_file_length){
                    longest_file_length = f.length();
                    longest_file = i;
                }
            }

            boolean longest_file_ended=false;

            while(!longest_file_ended){

                ArrayList<String> lines = new ArrayList<>();
                for(int i=0;i<block_count;i++) {

                    //checking if line is null
                    String fileLine = readers[i].readLine();
                    if(i==longest_file && fileLine==null) {
                        longest_file_ended = true;
                        break;

                    }
                    if(fileLine!=null){
                        lines.add(fileLine);


                    }
                }

                if(longest_file_ended) break;

                HashMap<String,ArrayList<String>> names= new HashMap<>();
                for (String str : lines) {
                    String[] res = str.split("\\[");

                    res[1] = res[1].replace("]", "");
                    String[] ids_temp = res[1].split(",\\s");
                    ArrayList<String> ids_temp_al = new ArrayList<>();

                    for(int i=0;i<ids_temp.length;i++){
                        ids_temp_al.add(ids_temp[i]);
                    }

                    if(names.containsKey(res[0])){
                        for(String s : ids_temp_al) {
                           if(!names.get(res[0]).contains(s)) names.get(res[0]).add(s);
                        }
                    }
                    else names.put(res[0],ids_temp_al);
                }

                while(!names.isEmpty()) {
                    String smallest=getSmallest(names);
                    ArrayList<Integer> ids = new ArrayList<>();
                    for (String str : names.keySet()) {

                        if (str.equals(smallest)) {
                            for (String st : names.get(str)) {
                                Integer j = Integer.parseInt(st);
                                if (!ids.contains(j)) ids.add(j);
                            }

                        }
                    }

                    names.remove(smallest);
                    //System.out.println(lines);
                    bw.write(smallest + ids + "\n");

                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally
        {
            try{
                if(bw!=null)
                    bw.close();
            }catch(Exception ex){
                System.out.println("Error in closing the BufferedWriter"+ex);
            }
        }

        end_block_size = (new File("C:\\practice5\\block_final.txt").length()/1024.0);
    }

    public String getSmallest(HashMap<String,ArrayList<String>> lines){
        ArrayList<String> l = new ArrayList<>();
        for(String s:lines.keySet()){
            l.add(s);
        }

        String smallest=l.get(0);
        for(int i=1;i<l.size();i++){
            if(l.get(i).compareTo(smallest)<0) smallest = l.get(i);
        }
        return smallest;
    }

}
