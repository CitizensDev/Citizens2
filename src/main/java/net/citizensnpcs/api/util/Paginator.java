package net.citizensnpcs.api.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class Paginator {
    private boolean console;
    private String header;
    private final List<String> lines = new ArrayList<String>();

    public void addLine(String line) {
        lines.add(line);
    }

    public Paginator console(boolean console) {
        this.console = console;
        return this;
    }

    public Paginator header(String header) {
        this.header = header;
        return this;
    }

    public boolean sendPage(CommandSender sender, int page) {
        int linesPerPage = console ? 200 : LINES_PER_PAGE;
        int pages = (int) (Math.ceil((double) lines.size() / linesPerPage) == 0 ? 1
                : Math.ceil((double) lines.size() / linesPerPage));
        if (page <= 0 || page > pages)
            return false;

        int startIndex = linesPerPage * page - linesPerPage;
        int endIndex = page * linesPerPage;

        Messaging.send(sender, wrapHeader("[[" + header + " <f>" + page + "/" + pages));

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex)) {
            Messaging.send(sender, line);
        }
        return true;
    }

    public static String wrapHeader(Object string) {
        return "[[=====[ " + string.toString() + " [[]=====";
    }

    private static final int LINES_PER_PAGE = 9;
}