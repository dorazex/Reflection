import reflection.api.InvestigatorImpl;

public class Main {
    public static void main(String[] args){
        InvestigatorImpl investigator = new InvestigatorImpl();
        investigator.load(new String("asd"));
        System.out.println(investigator.getInheritanceChain("----"));
    }
}
