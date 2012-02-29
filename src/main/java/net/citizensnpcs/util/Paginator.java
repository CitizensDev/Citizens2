package net.citizensnpcs.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

public class Paginator {
    private static final int LINES_PER_PAGE = 9;

    private final List<String> lines = new ArrayList<String>();
    private String header;

    public void addLine(String line) {
        lines.add(line);
    }

    public void setHeaderText(String header) {
        this.header = header;
    }

    public boolean sendPage(Player player, int page) {
        int pages = (int) ((lines.size() / LINES_PER_PAGE == 0) ? 1 : Math.ceil((double) lines.size() / LINES_PER_PAGE));
        if (page < 0 || page > pages)
            return false;

        int startIndex = LINES_PER_PAGE * page - LINES_PER_PAGE;
        int endIndex = page * LINES_PER_PAGE;

        Messaging.send(player, StringHelper.wrapHeader("<e>" + header + " <f>" + page + "/" + pages));

        if (lines.size() < endIndex)
            endIndex = lines.size();
        for (String line : lines.subList(startIndex, endIndex))
            Messaging.send(player, line);
        return true;
    }
}