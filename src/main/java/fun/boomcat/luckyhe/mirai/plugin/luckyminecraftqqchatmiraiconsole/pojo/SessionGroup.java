package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo;

public class SessionGroup {
    private long id;
    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SessionGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public SessionGroup(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
