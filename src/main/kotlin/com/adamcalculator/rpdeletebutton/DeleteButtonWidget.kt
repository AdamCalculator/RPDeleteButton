package com.adamcalculator.rpdeletebutton

import io.gitlab.jfronny.libjf.entrywidgets.api.v0.ResourcePackEntryWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.packs.PackSelectionModel
import net.minecraft.client.gui.screens.packs.PackSelectionScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.*

class DeleteButtonWidget : ResourcePackEntryWidget {
    companion object {
        private val CONFIG = Config.getConfig()
        private val BUTTON_TEXTURE = ResourceLocation.tryParse("respackdeletebutton:delete.png")!!
    }

    override fun isVisible(pack: PackSelectionModel.Entry, selectable: Boolean): Boolean {
        return !pack.isRequired && pack.id.startsWith("file/") && (CONFIG.alwaysShowButton || Screen.hasShiftDown())
    }

    override fun getWidth(entry: PackSelectionModel.Entry): Int {
        return 16
    }

    override fun getHeight(entry: PackSelectionModel.Entry, i: Int): Int {
        return 16
    }

    override fun getXMargin(pack: PackSelectionModel.Entry?): Int {
        return 2
    }

    override fun getY(pack: PackSelectionModel.Entry?, rowHeight: Int): Int {
        return 16
    }

    override fun render(
        entry: PackSelectionModel.Entry,
        guiGraphics: GuiGraphics,
        x: Int,
        y: Int,
        hovered: Boolean,
        tickDelta: Float
    ) {
        guiGraphics.blit(BUTTON_TEXTURE, x, y, 0.0F, (if (hovered) 16f else 0f), 16, 16, 16, 32);
    }

    override fun onClick(entry: PackSelectionModel.Entry) {
        if (entry.id.startsWith("file/")) {
            val filename = entry.id.replace("file/", "")
            val packFile = Minecraft.getInstance().resourcePackDirectory.resolve(filename)
            if (packFile.exists()) {
                deleteClicked(packFile)
            }
        }
    }

    private fun deleteClicked(file: Path) {
        // confirm    no-trash
        //    0        0         0
        //    0        1         0
        //    1        0         0
        //    1        1         1
        val mc = Minecraft.getInstance()
        if (!CONFIG.noConfirmationDelete && !CONFIG.isUseTrashFolder) {
            val parent = mc.screen
            val confirmScreen = ConfirmScreen({ accepted ->
                if (accepted) deleteAction(file, parent)
                mc.setScreen(parent)

            }, Component.translatable("selectWorld.deleteButton"), Component.translatable("selectWorld.deleteWarning", file.name))
            mc.setScreen(confirmScreen)

        } else {
            deleteAction(file, mc.screen)
        }
    }

    private fun deleteAction(file: Path, parent: Screen?) {
        val mc = Minecraft.getInstance()

        if (CONFIG.isUseTrashFolder) {
            val trash = mc.resourcePackDirectory.resolve("_deleted_resourcepacks")
            if (!trash.exists()) {
                trash.createDirectory()
            }

            try {
                Files.move(file, trash.resolve(file.name))

            } catch (e: FileAlreadyExistsException) {
                Files.move(file, trash.resolve(file.nameWithoutExtension + (UUID.randomUUID()).toString().substring(0, 4) + "." + file.extension))

            } catch (e: Exception) {
                e.printStackTrace()
                SystemToast.onPackCopyFailure(mc, e::class.java.name)
            }


        } else {
            Files.delete(file)
        }

        (parent as PackSelectionScreen).reload()
        Config.save()
    }
}
