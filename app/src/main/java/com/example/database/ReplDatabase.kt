package com.example.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val languageType: String,
    val templateIcon: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCloudSynced: Boolean = false
)

@Entity(tableName = "project_files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val path: String,
    val content: String
)

@Entity(tableName = "commits")
data class CommitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val commitMessage: String,
    val author: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "deployments")
data class DeploymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val domainUrl: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis(),
    val logs: String
)

@Dao
interface ReplDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Int): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Int)

    // File transactions
    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    fun getFilesByProjectFlow(projectId: Int): Flow<List<FileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId")
    suspend fun getFilesByProject(projectId: Int): List<FileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Update
    suspend fun updateFile(file: FileEntity)

    @Query("DELETE FROM project_files WHERE id = :fileId")
    suspend fun deleteFile(fileId: Int)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND path = :path")
    suspend fun deleteFileByPath(projectId: Int, path: String)

    // Commit transactions
    @Query("SELECT * FROM commits WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCommitsByProject(projectId: Int): Flow<List<CommitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommit(commit: CommitEntity): Long

    // Deployment transactions
    @Query("SELECT * FROM deployments WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getDeploymentsByProject(projectId: Int): Flow<List<DeploymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeployment(deployment: DeploymentEntity): Long
}

@Database(
    entities = [ProjectEntity::class, FileEntity::class, CommitEntity::class, DeploymentEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ReplDatabase : RoomDatabase() {
    abstract fun replDao(): ReplDao
}
