
public class EditDistance {
    
    static int mem[][];
    
    public static void main(String[] args) {
    	String s = "burger";
    	String u = "bridge";
    	System.out.println(calculate(s,u));
    	System.out.println(calculate(s,u)==calculate(u,s));
    }
    
    private static void initArray(int m, int n){
        mem = new int[m+1][n+1];
    }
    
    public static int calculate(String A, String B){
        initArray(A.length(),B.length());
        for(int j = 1;j<=B.length();j++){
            mem[0][j] = j;
        }
        
        for(int i = 1;i<=A.length();i++){
            mem[i][0] = i;
            for(int j = 1;j<=B.length();j++){
                if(A.charAt(i-1)==B.charAt(j-1)){
                    mem[i][j] = Math.min(Math.min(mem[i-1][j]+1, mem[i][j-1]+1), mem[i-1][j-1]);
                }
                else {
                    mem[i][j] = Math.min(Math.min(mem[i-1][j]+1, mem[i][j-1]+1), mem[i-1][j-1]+1);
                }
            }
        }
        return mem[A.length()][B.length()];
    }

}
