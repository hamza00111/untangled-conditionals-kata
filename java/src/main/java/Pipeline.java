import dependencies.Config;
import dependencies.Emailer;
import dependencies.Logger;
import dependencies.Project;

public class Pipeline {
    private final Config config;
    private final Emailer emailer;
    private final Logger log;

    public Pipeline(Config config, Emailer emailer, Logger log) {
        this.config = config;
        this.emailer = emailer;
        this.log = log;
    }

    public void run(Project project) {

        if (!runTests(project)) return;

        deploy(project.deploy());
    }

    private boolean runTests(Project project) {
        String testResult = project.runTests();
        return handleTestsResult(testResult);
    }

    private boolean handleTestsResult(String testResult) {
        if ("failure".equals(testResult)) {
            log.error("Tests failed");
            sendEmail("Tests failed");
            return false;
        }
        if ("success".equals(testResult)) {
            log.info("Tests passed");
            return true;
        }
        log.info(testResult);
        return true;
    }

    private void sendEmail(String emailContent) {
        if (!config.sendEmailSummary()) {
            log.info("Email disabled");
            return;
        }
        log.info("Sending email");
        emailer.send(emailContent);
    }

    private void deploy(String deploymentResult) {
        if (!"success".equals(deploymentResult)) {
            log.error("Deployment failed");
            sendEmail("Deployment failed");
            return;
        }
        log.info("Deployment successful");
        sendEmail("Deployment completed successfully");
    }

}
