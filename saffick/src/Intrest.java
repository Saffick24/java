import java.util.Scanner;

public class Intrest {
    public static void main(String[] args){
        System.out.println("simple intrest calculatot");
        System.out.println("enter the amount to calculate intrest %d:");
        Scanner i=new Scanner(System.in);
        int b=i.nextInt();
        System.out.println("Enter intrest rate %d:");

        Scanner c=new Scanner(System.in);
        int ir=c.nextInt();
        System.out.println("enter your name:");
        Scanner n=new Scanner(System.in);
        String name=n.nextLine();
        System.out.println("enter no.of.years:");
        Scanner y=new Scanner(System.in);
        int year=y.nextInt();

        System.out.println("enter your gender(M/F):");
        Scanner g=new Scanner(System.in);
        String gen=g.nextLine();

        int ipy=(b*ir*year)/100;
        System.out.println("Rate of Intrest :"+ipy);










    }
}
