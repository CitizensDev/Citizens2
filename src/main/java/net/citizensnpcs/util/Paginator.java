package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class Paginator {
    private static final int LINES_PER_PAGE = 9;

    private String header;
    private final List<String> lines = new ArrayList<String>();

    public void addLine(String line) {
        lines.add(line);
    }

    public boolean sendPage(CommandSender sender, int page) {
        int pages = (int) ((lines.size() / LINES_PER_PAGE == 0) ? 1 : Math.ceil((double) lines.size() / LINES_PER_PAGE));
        if (page < 0 || page > pages)
            return false;

        int startIndex = LINES_PER_PAGE * page - LINES_PER_PAGE;
        int endIndex = page * LINES_PER_PAGE;

        Messaging.send(sender, StringHelper.wrapHeader("<e>" + header + " <f>" + page + "/" + pages));

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex))
            Messaging.send(sender, line);
        return true;
    }

    public void setHeaderText(String header) {
        this.header = header;
    }
}