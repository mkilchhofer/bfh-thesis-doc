public class TiM55xService {
    private IScan lidarSensor;
    private final GatewayClient<LidarServiceContract> gatewayClient;
    private MessageReceiver intentReceiver;
    private static final Logger LOGGER = LogManager.getLogger(TiM55xService.class);

    public TiM55xService(URI mqttURI, String mqttClientName, 
                   String instanceName, String lidarIp, int lidarPort) 
                   throws MqttException, IOException {
	this.gatewayClient = new GatewayClient<LidarServiceContract>(
		                     mqttURI,
		                     mqttClientName,
		                     new LidarServiceContract(instanceName)
	);
        IScanListener iScanListener = new IScanListener() {
            @Override
            public void newMeasData(IScanMeasData iScanMeasData) {
                IScanData scanData = ScanDataFact.create(iScanMeasData);
                List<IScanReflectData> scanMeasurementData = scanData.getScanMeasurementData();
                ArrayList<Measurement> measurements = new ArrayList<>();
                // abfuellen der Sensor-Daten in Variable <measurements>
                //..

                gatewayClient.readyToPublish(
                        gatewayClient.getContract().EVENT_MEASUREMENT,
                        new LidarMeasurementEvent(measurements)
                );
            }
        };

        IScanOperator iScanOperator = new IScanOperator() {
            @Override
            public void newStateActice(State state) {
                LOGGER.info("{}: New laser state active: {}", instanceName, state);
                gatewayClient.readyToPublish(gatewayClient.getContract().STATUS_STATE,
                        new LidarState(state)
                );
            }

            @Override
            public void errorOccured() {
                LOGGER.error("{}: Laser ERROR", instanceName);
            }
        };

        this.intentReceiver = new MessageReceiver() {
            @Override
            public void messageReceived(String topic, byte[] payload) throws Exception {
                try {
                    // take only the last Intent
                    LidarIntent intent = gatewayClient.toMessageSet(payload, LidarIntent.class).last();
                    switch (intent.command){
                        case CONT_MEAS_START:
                            lidarSensor.startContMeas();
                            break;
                        case CONT_MEAS_STOP:
                            lidarSensor.stopContMeas();
                            break;
                        case SINGLE_MEAS:
                            lidarSensor.runSingleMeas();
                            break;
                    }
                } catch (Exception e) {
                    // ...
                }
        };

        this.lidarSensor = new TiM55x(iScanListener, iScanOperator, lidarIp, lidarPort);
        this.gatewayClient.connect();
        this.gatewayClient.subscribe(gatewayClient.getContract().INTENT + "/#", this.intentReceiver);
    }
}
