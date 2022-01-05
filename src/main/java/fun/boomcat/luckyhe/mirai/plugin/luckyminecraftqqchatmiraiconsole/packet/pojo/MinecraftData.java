package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.pojo;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarIntString;
import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.packet.datatype.VarLong;

public class MinecraftData {
    private VarLong sessionId;
    private VarIntString serverName;
    private VarIntString joinFormatString;
    private VarIntString quitFormatString;
    private VarIntString msgFormatString;
    private VarIntString deathFormatString;
    private VarIntString kickFormatString;

    @Override
    public String toString() {
        return "MinecraftData{" +
                "sessionId=" + sessionId +
                ", serverName=" + serverName +
                ", joinFormatString=" + joinFormatString +
                ", quitFormatString=" + quitFormatString +
                ", msgFormatString=" + msgFormatString +
                ", deathFormatString=" + deathFormatString +
                ", kickFormatString=" + kickFormatString +
                '}';
    }

    public VarLong getSessionId() {
        return sessionId;
    }

    public void setSessionId(VarLong sessionId) {
        this.sessionId = sessionId;
    }

    public VarIntString getServerName() {
        return serverName;
    }

    public void setServerName(VarIntString serverName) {
        this.serverName = serverName;
    }

    public VarIntString getJoinFormatString() {
        return joinFormatString;
    }

    public void setJoinFormatString(VarIntString joinFormatString) {
        this.joinFormatString = joinFormatString;
    }

    public VarIntString getQuitFormatString() {
        return quitFormatString;
    }

    public void setQuitFormatString(VarIntString quitFormatString) {
        this.quitFormatString = quitFormatString;
    }

    public VarIntString getMsgFormatString() {
        return msgFormatString;
    }

    public void setMsgFormatString(VarIntString msgFormatString) {
        this.msgFormatString = msgFormatString;
    }

    public VarIntString getDeathFormatString() {
        return deathFormatString;
    }

    public void setDeathFormatString(VarIntString deathFormatString) {
        this.deathFormatString = deathFormatString;
    }

    public VarIntString getKickFormatString() {
        return kickFormatString;
    }

    public void setKickFormatString(VarIntString kickFormatString) {
        this.kickFormatString = kickFormatString;
    }

    public MinecraftData(VarLong sessionId, VarIntString serverName, VarIntString joinFormatString, VarIntString quitFormatString, VarIntString msgFormatString, VarIntString deathFormatString, VarIntString kickFormatString) {
        this.sessionId = sessionId;
        this.serverName = serverName;
        this.joinFormatString = joinFormatString;
        this.quitFormatString = quitFormatString;
        this.msgFormatString = msgFormatString;
        this.deathFormatString = deathFormatString;
        this.kickFormatString = kickFormatString;
    }
}
