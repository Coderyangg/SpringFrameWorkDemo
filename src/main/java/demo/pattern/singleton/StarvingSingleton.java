package demo.pattern.singleton;

public class StarvingSingleton {
    private static final StarvingSingleton singleton=new StarvingSingleton();
    private StarvingSingleton(){};
    public static StarvingSingleton getSingleton(){
        return singleton;
    }
}
