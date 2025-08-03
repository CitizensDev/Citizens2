package net.citizensnpcs.api.persistence;

import com.google.common.io.BaseEncoding;

import net.citizensnpcs.api.util.DataKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class ComponentPersister implements Persister<Component> {
    @Override
    public Component create(DataKey root) {
        String component = root.getString("");
        return GsonComponentSerializer.gson().deserialize(new String(BaseEncoding.base64().decode(component)));
    }

    @Override
    public void save(Component text, DataKey root) {
        String output = GsonComponentSerializer.gson().serialize(text);
        root.setString("", BaseEncoding.base64().encode(output.getBytes()));
    }
}
