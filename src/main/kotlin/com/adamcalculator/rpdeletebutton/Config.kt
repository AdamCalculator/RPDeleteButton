package com.adamcalculator.rpdeletebutton

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.client.Minecraft
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class Config {
    companion object {
        private var config: Config? = null;

        fun getConfig(): Config {
            if (config == null) {
                config = loadFromFile()
            }

            return config!!
        }

        fun save() {
            getConfig().save()
        }

        /**
         * Create a Config from file
         */
        private fun loadFromFile(): Config {
            val location = location()

            val fileContent = if (location.exists()) {
                Files.readString(location)

            } else {
                "{}"
            }

            return Gson().fromJson(fileContent, Config::class.java)
        }

        private fun location(): Path {
            return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("respackdeletebutton.json")
        }
    }

    private fun save() {
        val location = location()

        Files.deleteIfExists(location)
        val toWrite = GsonBuilder().setPrettyPrinting().create().toJson(this)
        Files.writeString(location, toWrite)
    }

    var noConfirmationDelete: Boolean = false
    var alwaysShowButton = false
    var isUseTrashFolder: Boolean = true
}