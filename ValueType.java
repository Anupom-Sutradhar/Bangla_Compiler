public enum ValueType {
    INT("int"),
    DOUBLE("double"),
    BOOL("boolean"),
    ERROR("<error>");

    private final String javaName;

    ValueType(String javaName) {
        this.javaName = javaName;
    }

    public String javaName() {
        return javaName;
    }

    public boolean isNumeric() {
        return this == INT || this == DOUBLE;
    }
}
