import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);

        long time_m =  System.nanoTime();
        BSBI m = new BSBI("C:\\Users\\alena\\IdeaProjects\\Practice1.Vocabulary\\Collection");
        time_m = System.nanoTime()-time_m;
        System.out.println(time_m/ 1000000000 + " s");
        System.out.println(m.Collection_size);
        System.out.println(m.end_block_size);



    }
}
