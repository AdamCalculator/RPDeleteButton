package com.adamcalculator.rpdeletebutton

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.Util
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component


class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            return@ConfigScreenFactory if (FabricLoader.getInstance().isModLoaded("cloth-config")) {
                createClothConfigScreen(parent)
            } else {
                println("[${DeleteButtonWidget.LOG}] cloth-config is missing :(")
                null
            }
        }
    }

    private fun createClothConfigScreen(parent: Screen): Screen? {
        val config = Config.getConfig()
        val defConfig = Config() // if u save it, config restored to defaults

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("rpdeletebutton.modname.full"))
            .setSavingRunnable(Config.Companion::save)

        val entryBuilder: ConfigEntryBuilder = builder.entryBuilder()
        val general = builder.getOrCreateCategory(Component.literal(""))
        general.addEntry(
            entryBuilder.startBooleanToggle(Component.translatable("rpdeletebutton.config.always_show_button"), config.alwaysShowButton)
                .setTooltip(Component.translatable("rpdeletebutton.config.always_show_button.tooltip"))
                .setDefaultValue(defConfig.alwaysShowButton)
                .setSaveConsumer { newValue ->
                    config.alwaysShowButton = newValue
                }
                .build()
        )

        general.addEntry(
            entryBuilder.startBooleanToggle(Component.translatable("rpdeletebutton.config.use_trash_folder"), config.isUseTrashFolder)
                .setTooltip(Component.translatable(if (Util.getPlatform() == Util.OS.WINDOWS) "rpdeletebutton.config.use_trash_folder.tooltip.windows" else "rpdeletebutton.config.use_trash_folder.tooltip", config.trashPath.replace("%GAME_DIR%", ".minecraft")))
                .setDefaultValue(defConfig.isUseTrashFolder)
                .setSaveConsumer { newValue ->
                    config.isUseTrashFolder = newValue
                }
                .build()
        )

        general.addEntry(
            entryBuilder.startBooleanToggle(Component.translatable("rpdeletebutton.config.delete_without_confirming").withStyle(ChatFormatting.RED), config.noConfirmationDelete)
                .setTooltip(Component.translatable("rpdeletebutton.config.delete_without_confirming.tooltip"))
                .setDefaultValue(defConfig.noConfirmationDelete)
                .setSaveConsumer { newValue ->
                    config.noConfirmationDelete = newValue
                }
                .build()
        )

        return builder.build()
    }
}