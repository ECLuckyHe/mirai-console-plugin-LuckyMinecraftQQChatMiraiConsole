package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

public enum McChatCommandStep {
    MAIN(McChatCommandUtil.mainHelp()),
    LIST(McChatCommandUtil.listHelp()),
    MODIFY(McChatCommandUtil.modifyHelp()),
    MODIFY_MAIN(McChatCommandUtil.modifyMenuHelp()),
    MODIFY_ADD_GROUP(McChatCommandUtil.modifyGroupAddHelp()),
    MODIFY_DEL_GROUP(McChatCommandUtil.modifyGroupDelHelp()),
    MODIFY_FORMAT(McChatCommandUtil.modifyFormatHelp()),
    MODIFY_SESSION_NAME(McChatCommandUtil.modifySessionNameHelp()),
    ANNOUNCE(McChatCommandUtil.announceHelp()),
    ANNOUNCE_MC(McChatCommandUtil.announceMcHelp()),
    ANNOUNCE_CONTENT(McChatCommandUtil.announceContentHelp()),
    USER_COMMAND(McChatCommandUtil.userCommandHelp()),
    USER_COMMAND_MENU(McChatCommandUtil.userCommandMenuHelp()),
    USER_COMMAND_ADD_NAME(McChatCommandUtil.userCommandAddNameHelp()),
    USER_COMMAND_ADD_COMMAND(McChatCommandUtil.userCommandAddCommandHelp()),
    USER_COMMAND_ADD_MAPPING(McChatCommandUtil.userCommandAddMappingHelp()),
    USER_COMMAND_ADD_CONFIRM(McChatCommandUtil.userCommandAddConfirmHelp()),
    USER_COMMAND_DEL(McChatCommandUtil.userCommandDelHelp());

    private final String instruction;
    McChatCommandStep(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
