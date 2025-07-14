package ArbolSintactico;

public class Repeatx extends Statx {

    private Expx eRepeat;
    private Statx sRepeat;

    public Repeatx(Statx sRepeat, Expx eRepeat) {
        this.eRepeat = eRepeat;
        this.sRepeat = sRepeat;
    }

    public Object[] getVariables() {
        Object obj[] = new Object[3];
        obj[0] = eRepeat;
        obj[1] = sRepeat;
        return obj;
    }
}
