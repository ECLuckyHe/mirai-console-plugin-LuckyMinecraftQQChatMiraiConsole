package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand;

public enum OpMcChatCommandStep {
    MAIN(OpMcChatCommandUtil.mainHelp()),
    MAIN_LIST(OpMcChatCommandUtil.listHelp()),
    ADD(OpMcChatCommandUtil.addHelp()),
    ADD_SESSION_NAME(OpMcChatCommandUtil.addSessionNameHelp()),
    ADD_GROUP_FORMAT(OpMcChatCommandUtil.addGroupFormatHelp()),
    ADD_CONFIRM(OpMcChatCommandUtil.addConfirmHelp()),
    DEL(OpMcChatCommandUtil.delHelp()),
    DEL_CONFIRM(OpMcChatCommandUtil.delConfirm()),
    MODIFY(OpMcChatCommandUtil.modifyHelp()),
    MODIFY_MAIN(OpMcChatCommandUtil.modifyMenuHelp()),
    MODIFY_ADD_GROUP(OpMcChatCommandUtil.modifyGroupAddHelp()),
    MODIFY_DEL_GROUP(OpMcChatCommandUtil.modifyGroupDelHelp()),
    MODIFY_FORMAT(OpMcChatCommandUtil.modifyFormatHelp());

    private String instruction;
    OpMcChatCommandStep(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
