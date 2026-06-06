package com.bengebackend.entity;

import java.util.List;

public class AIMsgDevide {
    private String MsgForUser;
    private String StrScript;
    private String Title;
    private String Background;
    private List<String> ChrScript;
    private List<String> Clues;
    private String Trues;
    private String GoBook;

    public AIMsgDevide() {
    }

    public AIMsgDevide(String Title, String StrScript) {
        this.Title = Title;
        this.StrScript = StrScript;
    }

    public AIMsgDevide(String MsgForUser, String StrScript, String Title, String Background,
            List<String> ChrScript, List<String> Clues, String Trues, String GoBook) {
        this.MsgForUser = MsgForUser;
        this.StrScript = StrScript;
        this.Title = Title;
        this.Background = Background;
        this.ChrScript = ChrScript;
        this.Clues = Clues;
        this.Trues = Trues;
        this.GoBook = GoBook;
    }

    public String getMsgForUser() {
        return MsgForUser;
    }

    public void setMsgForUser(String MsgForUser) {
        this.MsgForUser = MsgForUser;
    }

    public String getStrScript() {
        return StrScript;
    }

    public void setStrScript(String StrScript) {
        // StrScript = StrScript.replaceAll("\n", "<br>");// 替换换行符为Markdown格式的换行
        StrScript = StrScript.replaceAll("\\s*---\\s*", "\n\n---\n\n");// 替换分隔符为Markdown格式的分割线
        StrScript = StrScript.replaceAll("- C>", "<li>C>");// 替换线索开头为Markdown格式的列表项
        StrScript = StrScript.replaceAll("- CHR", "<li>CHR");// 替换角色开头为Markdown格式的列表项
        StrScript = StrScript.replaceAll("##\\s*背景\\s*", "## 背景  \n\n");// 替换背景为Markdown格式的二级标题
        StrScript = StrScript.replaceAll("##\\s*线索\\s*", "## 线索  \n\n");// 替换线索为Markdown格式的二级标题
        StrScript = StrScript.replaceAll("##\\s*人物剧本\\s*", "## 人物剧本  \n\n");// 替换人物剧本为Markdown格式的二级标题
        StrScript = StrScript.replaceAll("##\\s*真相\\s*", "## 真相  \n\n");// 替换真相为Markdown格式的二级标题
        StrScript = StrScript.replaceAll("##\\s*组织者手册\\s*", "## 组织者手册  \n\n");// 替换组织者手册为Markdown格式的二级标题
        this.StrScript = StrScript;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        Title = Title.replaceAll("#", "");
        this.Title = Title;
    }

    public String getBackground() {
        return Background;
    }

    public void setBackground(String Background) {
        this.Background = Background;
    }

    public List<String> getChrScript() {
        return ChrScript;
    }

    public void setChrScript(List<String> ChrScript) {
        this.ChrScript = ChrScript;
    }

    public List<String> getClues() {
        return Clues;
    }

    public void setClues(List<String> Clues) {
        this.Clues = Clues;
    }

    public String getTrues() {
        return Trues;
    }

    public void setTrues(String Trues) {
        this.Trues = Trues;
    }

    public String getGoBook() {
        return GoBook;
    }

    public void setGoBook(String GoBook) {
        this.GoBook = GoBook;
    }

    @Override
    public String toString() {
        return "Stage2AIMsgDevide{" +
                "MsgForUser='" + MsgForUser + "\n\n" +
                ", StrScript='" + StrScript + "\n\n" +
                ", Title='" + Title + "\n\n" +
                ", Background='" + Background + "\n\n" +
                ", ChrScript=" + ChrScript +
                ", Clues=" + Clues +
                ", Trues='" + Trues + "\n\n" +
                ", GoBook='" + GoBook + "\n\n" +
                '}';
    }
}