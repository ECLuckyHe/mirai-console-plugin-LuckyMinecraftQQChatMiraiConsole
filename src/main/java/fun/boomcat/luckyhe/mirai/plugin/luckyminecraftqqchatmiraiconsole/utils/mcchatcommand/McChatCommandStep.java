package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;

public enum McChatCommandStep {
    MAIN(McChatCommandUtil.mainHelp()),
    LIST(McChatCommandUtil.listHelp()),
    MODIFY(McChatCommandUtil.modifyHelp()),
    MODIFY_MAIN(McChatCommandUtil.modifyMenuHelp()),
    MODIFY_ADD_GROUP(McChatCommandUtil.modifyGroupAddHelp()),
    MODIFY_DEL_GROUP(McChatCommandUtil.modifyGroupDelHelp()),
    MODIFY_FORMAT(McChatCommandUtil.modifyFormatHelp()),
    MODIFY_SESSION_NAME(McChatCommandUtil.modifySessionNameHelp());
    private final String instruction;
    McChatCommandStep(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
