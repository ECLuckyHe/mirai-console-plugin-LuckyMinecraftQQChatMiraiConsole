package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand;

public enum OpMcChatCommandStep {
    MAIN(OpMcChatCommandUtil.mainHelp());

    private String instruction;
    private OpMcChatCommandStep(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
