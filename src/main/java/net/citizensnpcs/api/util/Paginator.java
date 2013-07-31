package net.citizensnpcs.api.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class Paginator {
    private String header;
    private final List<String> lines = new ArrayList<String>();

    public void addLine(String line) {
        lines.add(line);
    }

    public Paginator header(String header) {
        this.header = header;
        return this;
    }

    public boolean sendPage(CommandSender sender, int page) {
        int pages = (int) ((lines.size() / LINES_PER_PAGE == 0) ? 1 : Math.ceil((double) lines.size() / LINES_PER_PAGE));
        if (page < 0 || page > pages)
            return false;

        int startIndex = LINES_PER_PAGE * page - LINES_PER_PAGE;
        int endIndex = page * LINES_PER_PAGE;

        Messaging.send(sender, wrapHeader("<e>" + header + " <f>" + page + "/" + pages));

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex))
            Messaging.send(sender, line);
        return true;
    }

    public static String wrapHeader(Object string) {
        String highlight = "<e>";
        return highlight + "=====[ " + string.toString() + highlight + " ]=====";
    }

    private static final int LINES_PER_PAGE = 9;
}