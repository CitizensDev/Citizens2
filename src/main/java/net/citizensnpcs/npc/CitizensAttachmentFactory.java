package net.citizensnpcs.npc;

import net.citizensnpcs.Metrics;
import net.citizensnpcs.api.attachment.Attachment;
import net.citizensnpcs.api.attachment.AttachmentFactory;
import net.citizensnpcs.api.attachment.AttachmentInfo;

public class CitizensAttachmentFactory implements AttachmentFactory {
    @Override
    public <T extends Attachment> T getAttachment(Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Attachment> T getAttachment(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerAttachment(AttachmentInfo info) {
        // TODO Auto-generated method stub

    }

    public void addPlotters(Metrics metrics) {
        // TODO Auto-generated method stub

    }
}
