public class TestCatalog {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("io.github.project.openubl.xbuilder.content.catalogs.Catalog6");
        for(Object c : clazz.getEnumConstants()) {
            System.out.println(c.toString() + " = " + ((Enum)c).name());
        }
    }
}
