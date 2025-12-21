package com.example.androidcrud.utils

object VersionUtils {
    data class SemanticVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<SemanticVersion> {
        override fun compareTo(other: SemanticVersion): Int {
            if (major != other.major) return major.compareTo(other.major)
            if (minor != other.minor) return minor.compareTo(other.minor)
            return patch.compareTo(other.patch)
        }
    }

    fun parseVersion(versionString: String): SemanticVersion {
        val parts = versionString.split(".")
        if (parts.size != 3) {
             throw IllegalArgumentException("Invalid version format. Expected Major.Minor.Patch, got: $versionString")
        }
        return try {
            SemanticVersion(
                major = parts[0].toInt(),
                minor = parts[1].toInt(),
                patch = parts[2].toInt()
            )
        } catch (e: NumberFormatException) {
             throw IllegalArgumentException("Invalid version format. Version parts must be integers.", e)
        }
    }

    @Throws(IllegalArgumentException::class)
    fun verifyVersionCompatibility(backupVersionString: String, currentVersionString: String) {
        val backup = parseVersion(backupVersionString)
        val current = parseVersion(currentVersionString)

        if (backup.major < current.major) {
            throw IllegalArgumentException("Backup from older major version (${backup.major}) is not supported (Current: ${current.major}).")
        }
        if (backup.major > current.major) {
             throw IllegalArgumentException("Backup from newer major version (${backup.major}) is not supported (Current: ${current.major}).")
        }
    }
}