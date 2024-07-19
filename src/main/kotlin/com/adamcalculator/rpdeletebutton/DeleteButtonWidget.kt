package com.adamcalculator.rpdeletebutton

import io.gitlab.jfronny.libjf.entrywidgets.api.v0.ResourcePackEntryWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.client.gui.screens.ConfirmScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.packs.PackSelectionModel.Entry
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
        const val TRASH_DIR_NAME = "_deleted_resourcepacks"
        const val LOG = "RPDeleteButton"

        private val CONFIG = Config.getConfig()
        private val BUTTON_TEXTURE = ResourceLocation.tryParse("respackdeletebutton:delete.png")!!
    }

    override fun isVisible(pack: Entry, selectable: Boolean): Boolean {
        return !pack.isRequired && isEntryFile(pack) && (CONFIG.alwaysShowButton || Screen.hasShiftDown())
    }

    override fun getWidth(entry: Entry): Int {
        return 16
    }

    override fun getHeight(entry: Entry, i: Int): Int {
        return 16
    }

    override fun getXMargin(pack: Entry?): Int {
        return 2
    }

    override fun getY(pack: Entry?, rowHeight: Int): Int {
        return 16
    }

    override fun render(entry: Entry, guiGraphics: GuiGraphics, x: Int, y: Int, hovered: Boolean, tickDelta: Float) {
        guiGraphics.blit(BUTTON_TEXTURE, x, y, 0.0F, (if (hovered) 16f else 0f), 16, 16, 16, 32)
    }

    override fun onClick(entry: Entry) {
        if (isEntryFile(entry)) {
            val filename = entry.id.substring(5)
            val packFile = Minecraft.getInstance().resourcePackDirectory.resolve(filename)
            if (packFile.exists()) {
                deleteClicked(packFile)
            }
        }
    }

    private fun isEntryFile(entry: Entry): Boolean {
        return entry.id.startsWith("file/")
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
                if (accepted) {
                    deleteAction(file, parent)
                }
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
            val trash = mc.resourcePackDirectory.resolve(TRASH_DIR_NAME)
            if (!trash.exists()) {
                trash.createDirectory()
            }

            try {
                val to = trash.resolve(file.name)
                Files.move(file, to)
                println("[$LOG] Moved ${file.name} to $to")


            } catch (e: FileAlreadyExistsException) {
                try {
                    val to = trash.resolve(file.nameWithoutExtension + "-" + (UUID.randomUUID()).toString().substring(0, 4) + "." + file.extension)
                    Files.move(file, to)
                    println("[$LOG] Moved with rename ${file.name} to $to as ${to.name}")


                } catch (e: Exception) {
                    e.printStackTrace()
                    SystemToast.onPackCopyFailure(mc, e::class.java.name)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                SystemToast.onPackCopyFailure(mc, e::class.java.name)
            }


        } else {
            println("[$LOG] deleted a resourcepack: ${file.name}")
            Files.delete(file)
        }

        (parent as PackSelectionScreen).reload()
        CONFIG.save()
    }
}
