package ju.ma.commands

import java.util.EnumSet
import java.util.concurrent.Callable
import javax.sql.DataSource
import mu.KotlinLogging
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.AvailableSettings
import org.hibernate.dialect.PostgreSQL95Dialect
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.hibernate.tool.schema.TargetType
import org.springframework.boot.autoconfigure.domain.EntityScanPackages
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
import org.springframework.context.annotation.Profile
import org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager
import org.springframework.stereotype.Component
import picocli.CommandLine

private val logger = KotlinLogging.logger {}

@Profile("console")
@Component
@CommandLine.Command(
    name = "schema:dump", mixinStandardHelpOptions = true, subcommands = [],
    description = ["Dumps the current hibernate schema"]
)
class DumpSchema(
    val jpaProperties: JpaProperties,
    val entityScanPackages: EntityScanPackages,
    val dataSource: DataSource
) : ConsoleCommand, Callable<Int> {

    @CommandLine.Option(
        names = ["-a", "--action"],
        defaultValue = "CREATE",
        description = ["The schema type to generate CREATE/DROP/BOTH"]
    )
    var action: SchemaExport.Action = SchemaExport.Action.CREATE

    override val command: String = "schema:dump"

    override fun call(): Int {
        val registry = StandardServiceRegistryBuilder()
            .applySettings(jpaProperties.properties)
            .applySetting(AvailableSettings.DATASOURCE, dataSource)
            .applySetting(AvailableSettings.PHYSICAL_NAMING_STRATEGY, SpringPhysicalNamingStrategy::class.java)
            .applySetting(AvailableSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy::class.java)
            .applySetting(AvailableSettings.DIALECT, PostgreSQL95Dialect::class.java)
            .build()

        val persistenceUnitManager = DefaultPersistenceUnitManager()
        val packagesToScanArr = entityScanPackages.packageNames.toList()
        persistenceUnitManager.setPackagesToScan(*packagesToScanArr.toTypedArray())
        persistenceUnitManager.afterPropertiesSet()
        val persistenceUnitInfo = persistenceUnitManager.obtainDefaultPersistenceUnitInfo()

        val metadataSources = MetadataSources(registry)
        persistenceUnitInfo.managedClassNames.forEach {
            metadataSources.addAnnotatedClassName(it)
        }

        val schemaExport = SchemaExport()
        schemaExport.setHaltOnError(true)
        schemaExport.setFormat(true)
        schemaExport.setDelimiter(";")

        println("======================================= START SCHEMA ===============================================")
        schemaExport.execute(EnumSet.of(TargetType.STDOUT), action, metadataSources.buildMetadata())
        println("======================================= END SCHEMA =================================================")

        return 0
    }
}
