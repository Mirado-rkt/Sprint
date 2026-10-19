package mg.itu.utils;

public class MethodInfo {
    public String className;
    public String methodName;
    public boolean json;

    public MethodInfo(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.json = false;
    }

    public MethodInfo(String className, String methodName, boolean json) {
        this.className = className;
        this.methodName = methodName;
        this.json = json;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isJson() {
        return json;
    }

    public void setJson(boolean json) {
        this.json = json;
    }
}