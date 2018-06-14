public class ConsoleUIServant {

    private static final Logger LOGGER = LogManager.getLogger(ConsoleUIServant.class);
    private LidarServiceContract lidarServiceContract;
    private Set<ConsoleUIServiceContract> uiInstances;
    private Set<LidarServiceContract> tim55xInstances;
    private final GatewayClient<ConsoleUIServantContract> gatewayClient;
    private MessageReceiver keyPressReceiver, uiConnectionStatusReceiver,
            measurementReceiver, tim55xStateReceiver, lidarConnectionStatusReceiver;

    public ConsoleUIServant(URI mqttURI, String mqttClientName, String instanceName) throws MqttException {
        this.gatewayClient = new GatewayClient<>(mqttURI, mqttClientName, new ConsoleUIServantContract(instanceName));
        this.uiInstances = new HashSet<>();
        this.tim55xInstances = new HashSet<>();
        this.gatewayClient.connect();

        setupUIMessageReceivers();
        setupLidarMessageReceiver();

        this.gatewayClient.subscribe(new ConsoleUIServiceContract("+").STATUS_CONNECTION, this.uiConnectionStatusReceiver);
        this.gatewayClient.subscribe(new LidarServiceContract("+").STATUS_CONNECTION, this.lidarConnectionStatusReceiver);
    }

    // Message Receivers
    // ..
}
