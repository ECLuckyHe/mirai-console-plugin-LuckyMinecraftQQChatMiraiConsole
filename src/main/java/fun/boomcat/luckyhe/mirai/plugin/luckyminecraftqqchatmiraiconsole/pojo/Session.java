package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.pojo;

import java.util.List;

public class Session {
    private long id;
    private String name;
    private List<SessionGroup> groups;
    private String formatString;

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

    public List<SessionGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<SessionGroup> groups) {
        this.groups = groups;
    }

    public String getFormatString() {
        return formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    public Session(long id, String name, List<SessionGroup> groups, String formatString) {
        this.id = id;
        this.name = name;
        this.groups = groups;
        this.formatString = formatString;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", groups=" + groups +
                ", formatString='" + formatString + '\'' +
                '}';
    }
}
