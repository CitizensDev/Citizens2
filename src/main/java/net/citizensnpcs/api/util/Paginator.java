package net.citizensnpcs.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.command.CommandSender;

public class Paginator {
    private boolean console;
    private String header;
    private final List<String> lines = new ArrayList<String>();
    private String pageCommand;
    private boolean pageSwitcher;

    public Paginator() {
    }

    public Paginator(Collection<String> lines) {
        this.lines.addAll(lines);
    }

    public Paginator(int initialLinesOfText) {
        for (int i = 0; i < initialLinesOfText; i++) {
            lines.add("");
        }
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public Paginator console(boolean console) {
        this.console = console;
        return this;
    }

    public Paginator enablePageSwitcher() {
        pageSwitcher = true;
        pageCommand = "page $page";
        return this;
    }

    public Paginator enablePageSwitcher(String command) {
        pageSwitcher = true;
        pageCommand = command;
        return this;
    }

    public String getPageText(int page) {
        int linesPerPage = console ? 200 : LINES_PER_PAGE;
        int pages = (int) (Math.ceil((double) lines.size() / linesPerPage) == 0 ? 1
                : Math.ceil((double) lines.size() / linesPerPage));
        if (page <= 0 || page > pages)
            return null;

        int startIndex = linesPerPage * page - linesPerPage;
        int endIndex = page * linesPerPage;

        String pageDisplay = page + "/" + pages;
        if (pageSwitcher) {
            if (page > 1) {
                pageDisplay = "<click:run_command:" + pageCommand.replace("$page", "" + (page - 1))
                        + "><hover:show_text:Previous page><</hover></click> <white>" + pageDisplay;
            }
            if (pages > 1 && page != pages) {
                pageDisplay += " <click:run_command:" + pageCommand.replace("$page", "" + (page + 1))
                        + "><hover:show_text:Next page>></hover></click><white>";
            }
        }
        String text = header == null ? "" : wrapHeader("[[" + header + " <white>" + pageDisplay);

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex)) {
            text += "\n" + line;
        }
        return text;
    }

    public boolean hasPage(int page) {
        int linesPerPage = console ? 200 : LINES_PER_PAGE;
        int pages = (int) (Math.ceil((double) lines.size() / linesPerPage) == 0 ? 1
                : Math.ceil((double) lines.size() / linesPerPage));
        if (page <= 0 || page > pages)
            return false;
        return true;
    }

    public Paginator header(String header) {
        this.header = header;
        return this;
    }

    public boolean sendPage(CommandSender sender, int page) {
        String text = getPageText(page);
        if (text != null) {
            Messaging.send(sender, text);
            return true;
        } else {
            return false;
        }
    }

    public static String wrapHeader(Object string) {
        return "[[=====[ " + string.toString() + " [[]=====";
    }

    private static final int LINES_PER_PAGE = 9;
}