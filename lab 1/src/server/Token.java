package server;

public class Token {
    private boolean flag;
    private long size;
    private long position;
    private String name;

    public Token(boolean flag, long size, long position, String name) {
        this.flag = flag;
        this.size = size;
        this.position = position;
        this.name = name;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void incrementPosition(){
        position++;
    }
}
