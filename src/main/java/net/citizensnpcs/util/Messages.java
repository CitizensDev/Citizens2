package net.citizensnpcs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ListResourceBundle;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.google.common.io.Closeables;

public class Messages {
    public static final String AGE_SET = "citizens.commands.npc.age.set";
    public static final String AGE_TRAIT_DESCRIPTION = "citizens.traits.age-description";
    public static final String ALREADY_IN_EDITOR = "citizens.editors.already-in-editor";
    public static final String ALREADY_OWNER = "citizens.commands.npc.owner.already-owner";
    public static final String AVAILABLE_WAYPOINT_PROVIDERS = "citizens.waypoints.available-providers-header";
    public static final String BEHAVIOURS_ADDED = "citizens.commands.npc.behaviour.added";
    public static final String BEHAVIOURS_REMOVED = "citizens.commands.npc.behaviour.removed";
    public static final String CITIZENS_DISABLED = "citizens.notifications.disabled";
    public static final String CITIZENS_ENABLED = "citizens.notifications.enabled";
    public static final String CITIZENS_IMPLEMENTATION_DISABLED = "citizens.changed-implementation";
    public static final String CITIZENS_INCOMPATIBLE = "citizens.notifications.incompatible-version";
    public static final String CITIZENS_RELOAD_ERROR = "citizens.notifications.error-reloading";
    public static final String CITIZENS_RELOADED = "citizens.notifications.reloaded";
    public static final String CITIZENS_RELOADING = "citizens.notifications.reloading";
    public static final String CITIZENS_SAVED = "citizens.notifications.saved";
    public static final String CITIZENS_SAVING = "citizens.notifications.saving";
    public static final String COMMAND_HELP_HEADER = "citizens.commands.help.header";
    public static final String COMMAND_ID_NOT_FOUND = "citizens.commands.id-not-found";
    public static final String COMMAND_INVALID_MOBTYPE = "citizens.commands.invalid-mobtype";
    public static final String COMMAND_INVALID_NUMBER = "citizens.commands.invalid-number";
    public static final String COMMAND_MISSING_TRAIT = "citizens.commands.requirements.missing-required-trait";
    public static final String COMMAND_MUST_BE_INGAME = "citizens.commands.requirements.must-be-ingame";
    public static final String COMMAND_MUST_BE_OWNER = "citizens.commands.requirements.must-be-owner";
    public static final String COMMAND_MUST_HAVE_SELECTED = "citizens.commands.requirements.must-have-selected";
    public static final String COMMAND_PAGE_MISSING = "citizens.commands.page-missing";
    public static final String COMMAND_REPORT_ERROR = "citizens.commands.console-error";
    public static final String COMMAND_REQUIREMENTS_INVALID_MOB_TYPE = "citizens.commands.requirements.disallowed-mobtype";
    public static final String CONTROLLABLE_REMOVED = "citizens.commands.npc.controllable.removed";
    public static final String CONTROLLABLE_SET = "citizens.commands.npc.controllable.set";
    public static final String CURRENT_WAYPOINT_PROVIDER = "citizens.waypoints.current-provider";
    public static final String DATABASE_CONNECTION_FAILED = "citizens.notifications.database-connection-failed";
    private static ResourceBundle defaultBundle;
    public static final String EQUIPMENT_EDITOR_ALL_ITEMS_REMOVED = "citizens.editors.equipment.all-items-removed";
    public static final String EQUIPMENT_EDITOR_BEGIN = "citizens.editors.equipment.begin";
    public static final String EQUIPMENT_EDITOR_END = "citizens.editors.equipment.end";
    public static final String EQUIPMENT_EDITOR_INVALID_BLOCK = "citizens.editors.equipment.invalid-block";
    public static final String EQUIPMENT_EDITOR_SHEEP_COLOURED = "citizens.editors.equipment.sheep-coloured";
    public static final String ERROR_CLEARING_GOALS = "citizens.nms-errors.clearing-goals";
    public static final String ERROR_GETTING_FIELD = "citizens.nms-errors.getting-field";
    public static final String ERROR_GETTING_ID_MAPPING = "citizens.nms-errors.getting-id-mapping";
    public static final String ERROR_INITALISING_SUB_PLUGIN = "citizens.sub-plugins.error-on-load";
    public static final String ERROR_LOADING_ECONOMY = "citizens.economy.error-loading";
    public static final String ERROR_SPAWNING_CUSTOM_ENTITY = "citizens.nms-errors.spawning-custom-entity";
    public static final String ERROR_STOPPING_NETWORK_THREADS = "citizens.nms-errors.stopping-network-threads";
    public static final String ERROR_UPDATING_NAVIGATION_WORLD = "citizens.nms-errors.updating-navigation-world";
    public static final String ERROR_UPDATING_PATHFINDING_RANGE = "citizens.nms-errors.updating-pathfinding-range";
    public static final String ERROR_UPDATING_SPEED = "citizens.nms-erorrs.updating-land-modifier";
    public static final String EXCEPTION_UPDATING_NPC = "citizens.notifications.exception-updating-npc";
    public static final String FAILED_LOAD_SAVES = "citizens.saves.load-failed";
    public static final String FAILED_TO_MOUNT_NPC = "citizens.commands.npc.mount.failed";
    public static final String FAILED_TO_REMOVE = "citizens.commands.trait.failed-to-remove";
    public static final String INVALID_AGE = "citizens.commands.npc.age.invalid-age";
    public static final String INVALID_POSE_NAME = "citizens.commands.npc.pose.invalid-name";
    public static final String INVALID_PROFESSION = "citizens.commands.npc.profession.invalid-profession";
    public static final String LINEAR_WAYPOINT_EDITOR_ADDED_WAYPOINT = "citizens.editors.waypoints.linear.added-waypoint";
    public static final String LINEAR_WAYPOINT_EDITOR_BEGIN = "citizens.editors.waypoints.linear.begin";
    public static final String LINEAR_WAYPOINT_EDITOR_EDIT_SLOT_SET = "citizens.editors.waypoints.linear.edit-slot-set";
    public static final String LINEAR_WAYPOINT_EDITOR_END = "citizens.editors.waypoints.linear.end";
    public static final String LINEAR_WAYPOINT_EDITOR_NOT_SHOWING_MARKERS = "citizens.editors.waypoints.linear.not-showing-markers";
    public static final String LINEAR_WAYPOINT_EDITOR_RANGE_EXCEEDED = "citizens.editors.waypoints.linear.range-exceeded";
    public static final String LINEAR_WAYPOINT_EDITOR_REMOVED_WAYPOINT = "citizens.editors.waypoints.linear.removed-waypoint";
    public static final String LINEAR_WAYPOINT_EDITOR_SHOWING_MARKERS = "citizens.editors.waypoints.linear.showing-markers";
    public static final String LOAD_NAME_NOT_FOUND = "citizens.notifications.npc-name-not-found";
    public static final String LOAD_TASK_NOT_SCHEDULED = "citizens.load-task-error";
    public static final String LOAD_UNKNOWN_NPC_TYPE = "citizens.notifications.unknown-npc-type";
    public static final String LOADING_SUB_PLUGIN = "citizens.sub-plugins.load";
    public static final String LOCALE_NOTIFICATION = "citizens.notifications.locale";
    public static final String METRICS_ERROR_NOTIFICATION = "citizens.notifications.metrics-load-error";
    public static final String METRICS_NOTIFICATION = "citizens.notifications.metrics-started";
    public static final String MINIMUM_COST_REQUIRED = "citizens.economy.minimum-cost-required";
    public static final String MISSING_TRANSLATIONS = "citizens.notifications.missing-translations";
    public static final String MOBTYPE_CANNOT_BE_AGED = "citizens.commands.npc.age.cannot-be-aged";
    public static final String MONEY_WITHDRAWN = "citizens.economy.money-withdrawn";
    public static final String NO_NPC_WITH_ID_FOUND = "citizens.commands.npc.spawn.missing-npc-id";
    public static final String NO_STORED_SPAWN_LOCATION = "citizens.commands.npc.spawn.no-location";
    public static final String NOT_LIVING_MOBTYPE = "citizens.commands.npc.create.not-living-mobtype";
    public static final String NPC_ALREADY_SELECTED = "citizens.commands.npc.select.already-selected";
    public static final String NPC_ALREADY_SPAWNED = "citizens.commands.npc.spawn.already-spawned";
    public static final String NPC_COPIED = "citizens.commands.npc.copy.copied";
    public static final String NPC_CREATE_INVALID_MOBTYPE = "citizens.commands.npc.create.invalid-mobtype";
    public static final String NPC_DESPAWNED = "citizens.commands.npc.despawn.despawned";
    public static final String NPC_NAME_TOO_LONG = "citizens.commands.npc.create.npc-name-too-long";
    public static final String NPC_NOT_CONTROLLABLE = "citizens.commands.npc.controllable.not-controllable";
    public static final String NPC_NOT_FOUND = "citizens.notifications.npc-not-found";
    public static final String NPC_OWNER = "citizens.commands.npc.owner.owner";
    public static final String NPC_REMOVED = "citizens.commands.npc.remove.removed";
    public static final String NPC_RENAMED = "citizens.commands.npc.rename.renamed";
    public static final String NPC_SPAWNED = "citizens.commands.npc.spawn.spawned";
    public static final String NPC_TELEPORTED = "citizens.commands.npc.tphere.teleported";
    public static final String NUM_LOADED_NOTIFICATION = "citizens.notifications.npcs-loaded";
    public static final String OVER_NPC_LIMIT = "citizens.limits.over-npc-limit";
    public static final String POSE_ADDED = "citizens.commands.npc.pose.added";
    public static final String POSE_ALREADY_EXISTS = "citizens.commands.npc.pose.already-exists";
    public static final String POSE_MISSING = "citizens.commands.npc.pose.missing";
    public static final String POSE_REMOVED = "citizens.commands.npc.pose.removed";
    public static final String PROFESSION_SET = "citizens.commands.npc.profession.set";
    public static final String REMOVED_ALL_NPCS = "citizens.commands.npc.remove.removed-all";
    public static final String SAVE_METHOD_SET_NOTIFICATION = "citizens.notifications.save-method-set";
    public static final String SCRIPT_COMPILED = "citizens.commands.script.compiled";
    public static final String SCRIPT_COMPILING = "citizens.commands.script.compiling";
    public static final String SCRIPT_FILE_MISSING = "citizens.commands.script.file-missing";
    public static final String SKIPPING_BROKEN_TRAIT = "citizens.notifications.skipping-broken-trait";
    public static final String SKIPPING_INVALID_POSE = "citizens.notifications.skipping-invalid-pose";
    public static final String SPEED_MODIFIER_ABOVE_LIMIT = "citizens.commands.npc.speed.modifier-above-limit";
    public static final String SPEED_MODIFIER_SET = "citizens.commands.npc.speed.set";
    public static final String TELEPORTED_TO_NPC = "citizens.commands.npc.tp.teleported";
    public static final String TEMPLATE_APPLIED = "citizens.commands.template.applied";
    public static final String TEMPLATE_CONFLICT = "citizens.commands.template.conflict";
    public static final String TEMPLATE_CREATED = "citizens.commands.template.created";
    public static final String TEMPLATE_MISSING = "citizens.commands.template.missing";
    public static final String TEXT_EDITOR_ADD_PROMPT = "citizens.editors.text.add-prompt";
    public static final String TEXT_EDITOR_ADDED_ENTRY = "citizens.editors.text.added-entry";
    public static final String TEXT_EDITOR_BEGIN = "citizens.editors.text.begin";
    public static final String TEXT_EDITOR_EDIT_BEGIN_PROMPT = "citizens.editors.text.edit-begin-prompt";
    public static final String TEXT_EDITOR_EDIT_PROMPT = "citizens.editors.text.edit-prompt";
    public static final String TEXT_EDITOR_EDITED_TEXT = "citizens.editors.text.edited-text";
    public static final String TEXT_EDITOR_END = "citizens.editors.text.end";
    public static final String TEXT_EDITOR_INVALID_EDIT_TYPE = "citizens.editors.text.invalid-edit-type";
    public static final String TEXT_EDITOR_INVALID_INDEX = "citizens.editors.text.invalid-index";
    public static final String TEXT_EDITOR_INVALID_INPUT = "citizens.editors.text.invalid-input";
    public static final String TEXT_EDITOR_INVALID_PAGE = "citizens.editors.text.invalid-page";
    public static final String TEXT_EDITOR_PAGE_PROMPT = "citizens.editors.text.change-page-prompt";
    public static final String TEXT_EDITOR_REMOVE_PROMPT = "citizens.editors.text.remove-prompt";
    public static final String TEXT_EDITOR_REMOVED_ENTRY = "citizens.editors.text.removed-entry";
    public static final String TEXT_EDITOR_START_PROMPT = "citizens.editors.text.start-prompt";
    public static final String TRAIT_LOAD_FAILED = "citizens.notifications.trait-load-failed";
    public static final String TRAIT_NOT_CONFIGURABLE = "citizens.commands.traitc.not-configurable";
    public static final String TRAIT_NOT_FOUND = "citizens.commands.traitc.missing";
    public static final String TRAIT_NOT_FOUND_ON_NPC = "citizens.commands.traitc.not-on-npc";
    public static final String TRAITS_ADDED = "citizens.commands.trait.added";
    public static final String TRAITS_FAILED_TO_ADD = "citizens.commands.trait.failed-to-add";
    public static final String TRAITS_FAILED_TO_CHANGE = "citizens.commands.trait.failed-to-change";
    public static final String TRAITS_REMOVED = "citizens.commands.trait.removed";
    public static final String UNKNOWN_COMMAND = "citizens.commands.unknown-command";
    public static final String VULNERABLE_SET = "citizens.commands.npc.vulnerable.set";
    public static final String VULNERABLE_STOPPED = "citizens.commands.npc.vulnerable.stopped";
    public static final String WAYPOINT_PROVIDER_SET = "citizens.waypoints.set-provider";
    public static final String WRITING_DEFAULT_SETTING = "citizens.settings.writing-default";

    private static Properties getDefaultBundleProperties() {
        Properties defaults = new Properties();
        InputStream in = null;
        try {
            in = Messages.class.getResourceAsStream("/" + Translator.PREFIX + "_en.properties");
            defaults.load(in);
        } catch (IOException e) {
        } finally {
            Closeables.closeQuietly(in);
        }
        return defaults;
    }

    public static ResourceBundle getDefaultResourceBundle(File resourceDirectory, String fileName) {
        if (defaultBundle != null)
            return defaultBundle;
        resourceDirectory.mkdirs();

        File bundleFile = new File(resourceDirectory, fileName);
        if (!bundleFile.exists()) {
            try {
                bundleFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        populateDefaults(bundleFile);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(bundleFile);
            defaultBundle = new PropertyResourceBundle(stream);
        } catch (Exception e) {
            e.printStackTrace();
            defaultBundle = getFallbackResourceBundle();
        } finally {
            Closeables.closeQuietly(stream);
        }
        return defaultBundle;
    }

    private static ResourceBundle getFallbackResourceBundle() {
        return new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[0][0];
            }
        };
    }

    private static void populateDefaults(File bundleFile) {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(bundleFile);
            properties.load(in);
        } catch (IOException e) {
        } finally {
            Closeables.closeQuietly(in);
        }
        Properties defaults = getDefaultBundleProperties();
        for (Entry<Object, Object> entry : defaults.entrySet()) {
            if (!properties.containsKey(entry.getKey()))
                properties.put(entry.getKey(), entry.getValue());
        }
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(bundleFile);
            properties.store(stream, "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Closeables.closeQuietly(stream);
        }
    }
}
