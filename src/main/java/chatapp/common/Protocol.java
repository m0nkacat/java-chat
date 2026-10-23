package main.java.chatapp.common;

public class Protocol {
    public static final String CMD_REG = "REG";
    public static final String CMD_AUTH = "AUTH";
    public static final String CMD_JOIN = "JOIN";
    public static final String CMD_MSG = "MSG";
    public static final String CMD_SWITCH = "SWITCH";
    public static final String CMD_PRIVATE = "PRIVATE";
    public static final String CMD_OPEN_PRIVATE = "OPEN_PRIVATE";
    public static final String CMD_HISTORY = "HISTORY";
    public static final String CMD_LOGOUT = "LOGOUT";


    public static final String RES_OK = "OK";
    public static final String RES_ERR = "ERR";
    public static final String RES_CHANNEL_MSG = "CH_MSG";
    public static final String RES_PRIVATE_MSG = "PV_MSG";
    public static final String RES_CHANNEL_HISTORY = "CH_HIST";
    public static final String RES_END_HISTORY = "END_HIST";
    public static final String RES_USER_LIST = "USERS";

    public static final String SEP = "|";
}