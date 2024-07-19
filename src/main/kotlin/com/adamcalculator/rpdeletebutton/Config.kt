package com.adamcalculator.rpdeletebutton

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.client.Minecraft
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Config of a RPDeleteButton
 */
class Config {
    companion object {
        private const val MOD_ID = "respackdeletebutton"

        // instance of config (singleton)
        private var config: Config? = null

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

        /**
         * Return a Path of config file
         */
        private fun location(): Path {
            return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("$MOD_ID.json")
        }
    }



    // Properties
    /**
     * No snow confirmation screen before delete
     */
    var noConfirmationDelete: Boolean = false

    /**
     * Show button without need a Shift key hold
     */
    var alwaysShowButton = false

    /**
     * Move to mod trash folder instead of Files.delete()
     */
    var isUseTrashFolder: Boolean = true

    var trashPath = "%GAME_DIR%/resourcepacks/_deleted"


    /**
     * Save a config to file
     */
    fun save() {
        val location = location()

        Files.deleteIfExists(location)
        val toWrite = GsonBuilder().setPrettyPrinting().create().toJson(this)
        Files.writeString(location, toWrite)
    }
}