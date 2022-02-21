package fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.mcchatcommand;

import fun.boomcat.luckyhe.mirai.plugin.luckyminecraftqqchatmiraiconsole.utils.opmcchatcommand.OpMcChatCommandStep;

public enum McChatCommandStep {
    MAIN(McChatCommandUtil.mainHelp());
    private final String instruction;
    McChatCommandStep(String instruction) {
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
