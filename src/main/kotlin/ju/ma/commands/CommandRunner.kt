package ju.ma.commands

import javax.annotation.PostConstruct
import kotlin.system.exitProcess
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.ExitCodeGenerator
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import picocli.CommandLine

private val logger = KotlinLogging.logger {}

@Profile("console")
@Component
class CommandRunner(
    private val root: RootCommandLine,
    private val context: ConfigurableApplicationContext
) : CommandLineRunner, ExitCodeGenerator {
    private var exitCode: Int = 0

    override fun getExitCode() = exitCode

    override fun run(vararg args: String) {
        val arguments = mutableListOf<String>()
        var located = false
        args.filter { !it.trim().startsWith("--info.app.uuid") }.forEach {
            if (located) {
                arguments.add(it)
            } else {
                if (it == "execute") {
                    located = true
                }
            }
        }
        /* http://patorjk.com/software/taag/#p=testall&f=Wet+Letter&t=a-sights */
        println(
            """
     (`-')  _           (`-').->  _                (`-').->(`-')      (`-').-> 
     (OO ).-/           ( OO)_   (_)        .->    (OO )__ ( OO).->   ( OO)_   
     / ,---.   (`-')   (_)--\_)  ,-(`-') ,---(`-'),--. ,'-'/    '._  (_)--\_)  
     | \ /`.\  ( OO).->/    _ /  | ( OO)'  .-(OO )|  | |  ||'--...__)/    _ /  
     '-'|_.' |(,------.\_..`--.  |  |  )|  | .-, \|  `-'  |`--.  .--'\_..`--.  
    (|  .-.  | `------'.-._)   \(|  |_/ |  | '.(_/|  .-.  |   |  |   .-._)   \ 
     |  | |  |         \       / |  |'->|  '-'  | |  | |  |   |  |   \       / 
     `--' `--'          `-----'  `--'    `-----'  `--' `--'   `--'    `-----'
            """.trimIndent()
        )
        if (arguments.isEmpty() && !located) {
            arguments.add("--help")
        }

        if (arguments.isNotEmpty() || located) {
            root.commandLine.isToggleBooleanFlags = true
            exitCode = root.commandLine.execute(*arguments.toTypedArray())
            logger.info("Command completed with code: $exitCode")
            exitProcess(SpringApplication.exit(context))
        }
    }
}

/**
 * The root command line executable
 */
@CommandLine.Command(name = "execute", mixinStandardHelpOptions = true, subcommands = [])
class ExecuteCommand : Runnable {
    override fun run() {
        logger.info("Executes the specified sub-commands")
    }
}

/**
 * The root command line class. Applications can auto-inject this class to
 * manually add commands to the command line
 */
@Profile("console")
@Component
class RootCommandLine(
    private val factory: CommandLine.IFactory,
    private val commands: List<ConsoleCommand>
) {
    val commandLine = CommandLine(ExecuteCommand(), factory)

    @PostConstruct
    fun initCommands() {
        commands.forEach {
            commandLine.addSubcommand(it.command, it)
        }
    }
}

/**
 * Interface to be implemented for auto-configured commands. Any spring-component extending
 * this class will be automatically loaded as a sub-command.
 */
interface ConsoleCommand {
    /**
     * The name used to invoke the command.
     * java -jar /app/app.jar execute <command> s
     */
    val command: String
}
