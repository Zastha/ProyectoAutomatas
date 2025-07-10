package ArbolSintactico;

public class Whilex extends Statx {
    
    private Expx eWhile;
    private Statx sWhile;

    public Whilex(Expx eWhile, Statx sWhile) {
        this.eWhile = eWhile;
        this.sWhile = sWhile;
    }

    public Object[] getVariables() {
        Object obj[] = new Object[3];
        obj[0] = eWhile;
        obj[1] = sWhile;
        return obj;
    }
}
