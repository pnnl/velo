package gov.pnnl.velo.tif.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.velo.tif.model.Code;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.model.Queue;
import gov.pnnl.velo.tif.service.JobConfigService;
import gov.pnnl.velo.tif.service.MachineRegistry;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tif.service.impl.JobConfigServiceDefault;
import gov.pnnl.velo.tif.util.RegistryUtils;


//TODO 
// support memory limits (low priority)
// rollover handling on time fields (low priority)
// display queue name and show time limit info accordingly
// finally search this file for TODO!
/**
 * Generic panel that allows user to create a job configuration
 * for their simulation run.
 *
 */
public class JobLauncherPanel extends JPanel {

  private static final long serialVersionUID = -8331698992558002694L;

  // UI Variables
  protected MainPanel mainPanel;
  protected CustomCodeParametersUI customCodeParametersSection;
  protected boolean showSchedulerDetails = false;
  protected boolean showCodeDetails = false;
  protected boolean comboListenersEnabled = true;
  
  // Context variables
  protected String context; // alfresco path
  protected File localWorkingDir;
  
  // Registry variables
  protected String codeID;
  protected String codeVersion;
  protected String currentMachineId;
  protected Code code; // code registry section read from
  protected MachineRegistry machineRegistry; // machine Registry
  protected JobConfigService jobConfigService;
  protected boolean jobConfigModifiedOnLoad = false; // true if machine id from config no longer exists

  protected File jobConfigFile;
  protected Map<String, JobConfig> cachedConfigs = new HashMap<String, JobConfig>();
  
  public JobLauncherPanel(String codeID, String codeVersion, String context, File localWorkingDir,
      CustomCodeParametersUI customCodeParametersSection) {
    this(codeID, codeVersion, context, localWorkingDir, new File(localWorkingDir, JobConfigServiceDefault.FILE_NAME_SAVED_CONFIG),
        customCodeParametersSection);
  }

  /**
   * @wbp.parser.constructor
   */
  public JobLauncherPanel(String codeID, String codeVersion, String context, File localWorkingDir, File jobConfigFile,
      CustomCodeParametersUI customCodeParametersSection) {
    this.customCodeParametersSection = customCodeParametersSection;
    this.localWorkingDir = localWorkingDir;
    this.jobConfigFile = jobConfigFile;
    this.context = context;
    //String toolrestartFileName = toolsetDM.getRestartFileName();
    //this.createRemoteDir = toolrestartFileName==null;
    this.codeID = codeID;
    this.codeVersion = codeVersion;
    machineRegistry = TifServiceLocator.getMachineRegistry();
    code = TifServiceLocator.getCodeRegistry().get(codeID, codeVersion);
    jobConfigService = TifServiceLocator.getJobConfigService();

    initUI();
    restoreState();  // TODO: make restoreState(JobConfig) a separate method so we don't need jobConfigModifiedOnLoad
  }

  public String getCurrentMachineFullDomainName(){
    return mainPanel.fullDomainName.getText();
  }
  
  //todo - where i left off
  public void setUsername(String username){
    mainPanel.machineSettings.username.setText(username);
  }

  public void setRunDir(String runDir){
    mainPanel.machineSettings.runDirectory.setText(runDir);
  }
  
  public void setAllocationAccount(String allocAccount){
    mainPanel.machineSettings.allocationAccount.setText(allocAccount);
  }
   
  protected String getMachineSectionTitle() {
    return "Machine Settings:";
  }

  protected String getCodeSectionTitle() {
    return "Code Settings:";
  }
  
  protected String getCodeExecutableLabel() {
    return "Executable Path:";
  }
  
  protected String getDefaultRunDir(String machineId) {
    String runDir = machineRegistry.getRemoteUserHomeDirectory(machineId);
    return runDir;
  }

  protected void initUI() {
    mainPanel = new MainPanel();

    JScrollPane scroll = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    setLayout(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints gridBagConstraints;

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
    add(scroll, gridBagConstraints);
  }

  protected void restoreState() {
    
    // init the machine list model
    // TODO: only list the machines that support the given Code ID, since it's confusing
    // to users to have machines listed that don't have the code installed
    Collection<String> origMachNames = machineRegistry.getMachineIDs();
    Collection<String> machNames = new ArrayList<String>();
    
    // Remove any machine that has no job launching section (i.e., globus-only endpoints)
    for(String machName : origMachNames) {
      Machine machine = machineRegistry.get(machName);
      if(machine.getProcsPerNode() > 0) {
        //Only add if machine has the code that we are interested in
        if (machine.getCodes().contains(code)){
          machNames.add(machName);
        }
      }
    }
    
    String[] machineNamesArray = machNames.toArray(new String[machNames.size()]);
    DefaultComboBoxModel model = new DefaultComboBoxModel(machineNamesArray);
    mainPanel.machineID.setModel(model);

    String machineId = null;
    JobConfig config;

    if(jobConfigFile.exists()) {

      // load this config
      config = jobConfigService.loadConfigFromFile(jobConfigFile);
      Machine machine = config.getMachine();
      
      if(machine == null) { // if machine no longer exists
        // we need to pick a new machine and convert the config
        machineId = machineNamesArray[0];
        config = convertJobConfigToNewMachine(config, machineId);
        jobConfigModifiedOnLoad = true;
      }
          
      cachedConfigs.put(machineId, config);

    } else {
      machineId = machineNamesArray[0];
    }
    
    loadMachine(machineId);
  }
  
  protected JobConfig convertJobConfigToNewMachine(JobConfig oldConfig, String machineId) {
    JobConfig config = jobConfigService.getRegistryDefault(machineId, codeID, codeVersion);
    
    // migrate over parameters from old config to new config
    config.setAccount(oldConfig.getAccount());
    config.setCmsUser(oldConfig.getCmsUser());
    config.setRemoteDir(oldConfig.getRemoteDir());
    config.setJobHandlerParameters(oldConfig.getJobHandlerParameters());
    config.setNodes(oldConfig.getNodes());
    config.setProcessors(oldConfig.getProcessors());
    config.setProcsPerTask(oldConfig.getProcsPerTask());
    config.setTasksPerNode(oldConfig.getTasksPerNode());
    config.setNumberOfTasks(oldConfig.getNumberOfTasks());
    
    return config;
  }
  
  public boolean isJobConfigModifiedOnLoad() {
    return jobConfigModifiedOnLoad;
  }

  protected void loadConfig(String profileName) {
    String machineId = (String)mainPanel.machineID.getSelectedItem();
    boolean namedConfig = true;
    if(profileName.equals(JobConfigService.CONFIG_NAME_REGISTRY_DEFAULTS) ||
        profileName.equals(JobConfigService.CONFIG_NAME_SAVED) ) {
      namedConfig = false;
    }
    JobConfig config;
    if(profileName.equals(JobConfigService.CONFIG_NAME_SAVED)) {
      config = jobConfigService.loadConfigFromFile(jobConfigFile);
    } else {
      config = jobConfigService.getNamedConfig(profileName, machineId, codeID);
    }
    loadConfig(config);
  }

  protected void clearFields() {
    mainPanel.fullDomainName.setText("");
    mainPanel.machineSettings.username.setText("");
    mainPanel.machineSettings.runDirectory.setText("");

    mainPanel.codeSettings.executable.removeAllItems();
    mainPanel.machineSettings.queueName.removeAllItems();
    mainPanel.machineSettings.allocationAccount.setText("");
    mainPanel.machineSettings.wallTimeLimitHours.setValue(0);
    mainPanel.machineSettings.wallTimeLimitMinutes.setValue(30);
    mainPanel.machineSettings.wallTimeLimitSeconds.setValue(0);
    
    // queue info
    mainPanel.machineSettings.queueInfoPanel.lblNodesinfo.setText("");
    mainPanel.machineSettings.queueInfoPanel.lblProcsPerNodeInfo.setText("");
    mainPanel.machineSettings.queueInfoPanel.lblProcessorsinfo.setText("");
    mainPanel.machineSettings.queueInfoPanel.lblWalltimeinfo.setText("");
    
    // nodes
    setSpin(1, -1, 1, mainPanel.machineSettings.nodes);
    formatRange(1, -1, mainPanel.machineSettings.nodesRange);
    
    // processors
    setSpin(1, -1, 1, mainPanel.machineSettings.processors);
    formatRange(1, -1, mainPanel.machineSettings.procsRange);
    
    // tasksPerNode
    setSpin(1, -1, 1, mainPanel.machineSettings.tasksPerNode);
    formatRange(1, -1, mainPanel.machineSettings.tasksPerNodeRange);

    // procsPerTask
    setSpin(1, -1, 1, mainPanel.machineSettings.procsPerTask);
    formatRange(1, -1, mainPanel.machineSettings.procsPerTaskRange);
  }
  
  protected void setComboListenersEnabled(boolean enabled) {
    this.comboListenersEnabled = enabled;
  }
  
  protected DefaultComboBoxModel getProfileModel(String machineId, String codeId) {
    // List of job configs
    // a) Registry Defaults
    // b) Saved Runtime Config
    // c) Named Runtime Config Presets
    List<String> items = new ArrayList<String>();
    items.add(JobConfigServiceDefault.CONFIG_NAME_SAVED);
    items.add(JobConfigServiceDefault.CONFIG_NAME_REGISTRY_DEFAULTS);
    
    Map<String, JobConfig> namedConfigs = jobConfigService.getNamedConfigs(machineId, codeId);
    for(String namedConfig : namedConfigs.keySet()) {
      items.add(namedConfig);
    }
    
    DefaultComboBoxModel model = new DefaultComboBoxModel(items.toArray(new String[items.size()]));  
    return model;
  }

  protected void loadMachine(String machineId) {
    setComboListenersEnabled(false);
    clearFields();
    
    currentMachineId = machineId; // remember last selected machine
    Machine machine = machineRegistry.get(machineId);
    List<Queue> queues  = machine.getScheduler().getQueues() ;
    if(machine != null) {
      mainPanel.machineID.setSelectedItem(machine.getName());
      mainPanel.fullDomainName.setText( machine.getFullDomainName());
     
      showSchedulerDetails = queues!= null && queues.size() > 0;
      showCodeDetails = true;
      showCodeDetails = codeID != null;
    }

    setSchedulerVisibility(machine, showSchedulerDetails);

    if (showSchedulerDetails) {

      //show queue in the same order as it is stored in the list
      //Order matters so use traditional for loop instead of enhanced loop (for(Queue queue:queues))
      //we will find selected queue based on selected index.
      for (int i=0;i<queues.size();i++) {
        mainPanel.machineSettings.queueName.addItem(queues.get(i).getName());
      }
    
    } else {

      // Set some defaults in this case. Otherwise, assume data can be
      // taken from queue info
      // Set nodes to 1 and processors to the number of processors per
      // node
      setSpin(1, 1, machine.getNodes(), mainPanel.machineSettings.nodes);
      formatRange(1, machine.getNodes(), mainPanel.machineSettings.nodesRange);

      setSpin(1, machine.getNodes() * machine.getProcsPerNode(), machine.getProcsPerNode(), mainPanel.machineSettings.processors);
      formatRange(1, machine.getNodes() * machine.getProcsPerNode(), mainPanel.machineSettings.procsRange);

      setSpin(1, machine.getNodes() * machine.getProcsPerNode(), machine.getProcsPerNode(), mainPanel.machineSettings.procsPerTask);

    }

    
    boolean serverSupported = machine.isServerSideMonitoringSupported();
    mainPanel.clientSideMonitoringCB.setVisible(serverSupported);
    mainPanel.clientSideMonitoringCB.setText("Connect to " + machineId + " from my computer.");
    
    // now get the cached config for that machine
    JobConfig config = cachedConfigs.get(machineId);
    if(config == null) {
      config = jobConfigService.getRegistryDefault(machineId, codeID, codeVersion);
      String defaultRunDir = getDefaultRunDir(machineId);
      if(defaultRunDir != null) {
        config.setRemoteDir(defaultRunDir);
      }
      cachedConfigs.put(machineId, config);
    } 
    loadConfig(config);
    
    setComboListenersEnabled(true);
  }

  protected void loadConfig(JobConfig config) {
    setComboListenersEnabled(false);
    if (config != null) {
    
      // user info from the config
      mainPanel.machineSettings.username.setText(config.getUserName());
      mainPanel.machineSettings.allocationAccount.setText(config.getAccount());
      mainPanel.machineSettings.runDirectory.setText(config.getRemoteDir());

      if(config.getQueueName() != null) {
        mainPanel.machineSettings.queueName.setSelectedItem(config.getQueueName());
        Machine machine = config.getMachine();
        Queue queue = machine.getScheduler().getQueues().get(mainPanel.machineSettings.queueName.getSelectedIndex());
        setQueueConstraints(machine, queue);
      }

      // Nodes and processors
      setValidRange(config.getNodes(), mainPanel.machineSettings.nodes);
      setValidRange(config.getProcessors(), mainPanel.machineSettings.processors);
      setValidRange(config.getProcsPerTask(), mainPanel.machineSettings.procsPerTask);
      
      // tasks per node
      if(config.getTasksPerNode() != null) {
        setValidRange(config.getTasksPerNode(), mainPanel.machineSettings.tasksPerNode);
      }
      
      if (config.hasTime()) {
        int[] time = config.getTimeParts();
        if (time[0] > 0 || time[1] > 0 || time[2] > 0) {
          // Don't override if we somehow have 0s in the config file
          mainPanel.machineSettings.wallTimeLimitHours.setValue(time[0]);
          mainPanel.machineSettings.wallTimeLimitMinutes.setValue(time[1]);
          mainPanel.machineSettings.wallTimeLimitSeconds.setValue(time[2]);
        }
      }

      // TODO set memory limit (not implemented)

      // Get the default value of the executable path for the given machine
      Machine machine = config.getMachine();
      String defaultCommand = null;
      Code code = config.getCode();
      defaultCommand = code.getJobLaunching().getCommand();
      mainPanel.codeSettings.executable.addItem(defaultCommand);
      
      // Add last saved executable path only if config has a valid entry
      if(config.getCommand() != null && !config.getCommand().equals(defaultCommand)) {
        mainPanel.codeSettings.executable.addItem(config.getCommand());
        mainPanel.codeSettings.executable.setSelectedItem(config.getCommand());
      }
      
      if(customCodeParametersSection != null){
        customCodeParametersSection.loadState(config);
      }

      // set the job monitoring preference
      boolean localMonitoring = config.isLocalMonitoring();
      mainPanel.clientSideMonitoringCB.setSelected(localMonitoring);
    }
    setComboListenersEnabled(true);
  }

  /**
   * Writes the state of the UI to the FILE_NAME_SAVED_CONFIG file in the 
   * local working directory.
   * @return - the saved JobConfig object
   */
  public JobConfig saveJobConfig() {
    JobConfig jobConfig = bindJobConfig();
    
    // make sure the cached config is written to the local file
    jobConfigService.writeConfigToFile(jobConfig, jobConfigFile);
    return jobConfig;
  }
  
  protected JobConfig getNewJobConfig() {
    String machineID = (String)mainPanel.machineID.getSelectedItem();
    JobConfig config = new JobConfig(JobConfigServiceDefault.CONFIG_NAME_SAVED);
    Machine machine = RegistryUtils.getMachine(machineID);
    config.setMachine(machine);
    config.setCode(RegistryUtils.getCodeForMachine(machine, codeID, null));
    return config;
  }

  /**
   * Creates a job config out of the current UI settings (data binding)
   * @return
   */
  public JobConfig bindJobConfig() {

    JobConfig config = getNewJobConfig();
    config.setContextPath(this.context);
    config.setLocalWorkingDir(this.localWorkingDir);

    // nodes 
    config.setNodes(Integer.parseInt(mainPanel.machineSettings.nodes.getValue()
        .toString()));
    
    // processors
    config.setProcessors(Integer.parseInt(mainPanel.machineSettings.processors.getValue()
        .toString()));
    
    // tasks per node
    config.setTasksPerNode(Integer.parseInt(mainPanel.machineSettings.tasksPerNode.getValue().toString()));

    // procsPerTask
    config.setProcsPerTask(Integer.parseInt(mainPanel.machineSettings.procsPerTask.getValue().toString()));
    
    
    config.setAccount(mainPanel.machineSettings.allocationAccount.getText());
    config.setTime(Integer.parseInt(mainPanel.machineSettings.wallTimeLimitHours
        .getValue().toString()), Integer
        .parseInt(mainPanel.machineSettings.wallTimeLimitMinutes.getValue().toString()),
        Integer.parseInt(mainPanel.machineSettings.wallTimeLimitSeconds.getValue()
            .toString()));

    config.setUserName(mainPanel.machineSettings.username.getText());
    config.setRemoteDir(mainPanel.machineSettings.runDirectory.getText());
    if(mainPanel.codeSettings.executable.getSelectedItem()!=null) {
      config.setCommand(mainPanel.codeSettings.executable.getSelectedItem().toString());
    }

    // Get queue name from Machine registry
    // Machine machineRegistry = R.getMachine(machineName.getText());
    // if (machineRegistry != null && !machineRegistry.queues.isEmpty())
    // config.queueName = ((Machine.Queue) machineRegistry.queues.get(0)).name;
    // Get queue name from UI instead
    config.setQueueName((String) mainPanel.machineSettings.queueName.getSelectedItem());
    
    // set whether to use client-side job monitoring or not
    config.setLocalMonitoring(mainPanel.clientSideMonitoringCB.isSelected());
    
    // the custom code section could push its state to the tool data model or to the
    // runtime config, depending upon the code and the deployment
    if(customCodeParametersSection != null){
      customCodeParametersSection.pushStateToDataModel(config);
    }
    
    // Make sure to add the currently logged in user if available
    // so we can pass to server for server-side launch
    ISecurityManager secMgr = CmsServiceLocator.getSecurityManager();
    if(secMgr != null) {
      config.setCmsUser(secMgr.getUsername());
    }

    return config;
  }

  protected void createNamedConfig(String name) {
    JobConfig config = bindJobConfig();
    jobConfigService.addNamedConfig(config);
  }

  public void setSchedulerVisibility(Machine machine, boolean visible) {
    mainPanel.machineSettings.allocationAccount.setVisible(visible);
    mainPanel.machineSettings.processors.setVisible(visible);
    mainPanel.machineSettings.procsPerTask.setVisible(visible);
    mainPanel.machineSettings.queueLabel.setVisible(visible);
    mainPanel.machineSettings.queueName.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitHours.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitMinutes.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitSeconds.setVisible(visible);
    
    mainPanel.machineSettings.allocationAccountLabel.setVisible(visible);
    mainPanel.machineSettings.processorsLabel.setVisible(visible);
    mainPanel.machineSettings.processorsPerTaskLabel.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitHoursLabel.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitMinutesLabel.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitSecondsLabel.setVisible(visible);
    mainPanel.machineSettings.wallTimeLimitLabel.setVisible(visible);
    
    // Some queued system require an account, some don't
    mainPanel.machineSettings.allocationAccountLabel
    .setVisible(visible && machine.getScheduler().isAllocation());
    
    mainPanel.machineSettings.allocationAccount
    .setVisible(visible && machine.getScheduler().isAllocation());

    mainPanel.machineSettings.queueInfoPanel.setVisible(visible);
    
    // for now we are hiding the range text, since we don't need it now that we have 
    // a queue info panel
    mainPanel.machineSettings.nodesRange.setVisible(false);
    mainPanel.machineSettings.procsRange.setVisible(false);
    mainPanel.machineSettings.procsPerTaskRange.setVisible(false);
  }

  private void setQueueConstraints(Machine machine, Queue queue) {
    // set time ranges
    if (queue.hasTimeConstraint()) {
      // TODO:Set spinner max based on max time
      int[] maxtime = queue.getMaxTimeParts();
      int[] defaulttime = queue.getDefaultTimeParts();

      // Always validate hours because that is our highest
      // unit of time for max and default time
      validateAndSetSpinner(0, maxtime[0], defaulttime[0],
          mainPanel.machineSettings.wallTimeLimitHours);

      // Not applying limit on seconds - queues generally restrict
      // mins and hours not seconds
      setSpin(0, 59,
          (Integer) mainPanel.machineSettings.wallTimeLimitSeconds.getValue(),
          mainPanel.machineSettings.wallTimeLimitSeconds);

      // if max hours is greater than 0, don't restrict mins
      if (maxtime[0] > 0) {
        setSpin(0, 59,
            (Integer) mainPanel.machineSettings.wallTimeLimitMinutes.getValue(),
            mainPanel.machineSettings.wallTimeLimitMinutes);
      }
      // restrict minutes spinner only if
      // max hours is zero but max minutes is not 0
      if (maxtime[1] > 0 && maxtime[0] == 0) {
        validateAndSetSpinner(0, maxtime[1], defaulttime[1],
            mainPanel.machineSettings.wallTimeLimitMinutes);
      }
      
      String walltimeDesc = "";
      if(maxtime[0] > 0) {
        walltimeDesc += String.valueOf(maxtime[0]) + " hours";
      } 
      if(!walltimeDesc.isEmpty() && maxtime[1] > 0) {
        walltimeDesc += ", ";
      }
      if(maxtime[1] > 0) {
        walltimeDesc += String.valueOf(maxtime[1]) + " minutes";
      }
      mainPanel.machineSettings.queueInfoPanel.lblWalltimeinfo.setText(walltimeDesc);
    }
    // showMemoryFields(queue.hasMemoryLimit());
    
    // nodes
    int minNodes = queue.getMinNodes();
    int maxNodes = queue.getMaxNodes();
    validateAndSetSpinner(minNodes, maxNodes, minNodes, mainPanel.machineSettings.nodes);
    formatRange(minNodes, maxNodes, mainPanel.machineSettings.nodesRange);
    mainPanel.machineSettings.queueInfoPanel.lblNodesinfo.setText(String.valueOf(minNodes) + " - " + String.valueOf(maxNodes));
    
    // procs per node (this is constant across all queues for the machine
    mainPanel.machineSettings.queueInfoPanel.lblProcsPerNodeInfo.setText(String.valueOf(machine.getProcsPerNode()));

    // processors
    int minprocs = minNodes * machine.getProcsPerNode();
    int maxprocs = maxNodes * machine.getProcsPerNode();
    validateAndSetSpinner(minprocs, maxprocs, minprocs,mainPanel.machineSettings.processors);
    formatRange(minprocs, maxprocs, mainPanel.machineSettings.procsRange);
    mainPanel.machineSettings.queueInfoPanel.lblProcessorsinfo.setText(String.valueOf(minprocs) + " - " + String.valueOf(maxprocs));

    
    // tasks per node
    validateAndSetSpinner(1, maxprocs, minprocs,mainPanel.machineSettings.tasksPerNode);
    formatRange(minprocs, maxprocs, mainPanel.machineSettings.tasksPerNodeRange);
    
    // procs per task
    validateAndSetSpinner(1, maxprocs, 1, mainPanel.machineSettings.procsPerTask);
    formatRange(1, maxprocs, mainPanel.machineSettings.procsPerTaskRange);
    
  }

  private void validateAndSetSpinner(int minvalue, int maxvalue,
      int defaultValue, JSpinner spinner) {
    if (((Integer) spinner.getValue()) < minvalue
        || ((Integer) spinner.getValue()) > maxvalue) {
      spinner.setValue(defaultValue);
    }
    setSpin(minvalue, maxvalue, (Integer) spinner.getValue(), spinner);
  }

  /**
   * Set the values for the specified spinner.
   */
  protected void setSpin(int min, int max, int value, JSpinner spin) {
    SpinnerNumberModel model = (SpinnerNumberModel) spin.getModel();
    model.setMinimum(min);
    if (max == -1) 
      model.setMaximum(null);
    else
      model.setMaximum(max);
    model.setValue(value);
    spin.setValue(value);
  }

  /**
   * Sets the spinner value but first makes sure its in the valid range.
   */
  protected void setValidRange(Integer value, JSpinner spin) {
    SpinnerNumberModel model = (SpinnerNumberModel) spin.getModel();
    int min = ((Integer) model.getMinimum()).intValue();
    Integer max = (Integer) model.getMaximum();
    
    if (value != null && value >= min && (max == null || value <= max)  ) {
      model.setValue(value);
    } else {
      model.setValue(min);
    }

  }

  /**
   * Format a nice range lanbel.
   */
  protected void formatRange(int min, int max, JLabel jlabel) {
    if (max == -1) {
      jlabel.setText(" [" + min + "..]   ");
    } else {
      jlabel.setText(" [" + min + ".." + max + "]   ");
      // jlabel.setText(" [1..12288]    ");
    }
  }

  protected void formatRange(SpinnerNumberModel model, JLabel jlabel) {
    int min = ((Integer) model.getMinimum()).intValue();
    int max = ((Integer) model.getMaximum()).intValue();
    jlabel.setText(" [" + min + ".." + max + "]   ");
    // jlabel.setText(" [1..12288]    ");
  }

  /**
   * Returns a list of string messages if there are any errors
   * associated with the entries in this form.  It's up to containing
   * class to handle the errors.
   * @return
   */
  public List<String> validateUserInputs() {
    List<String> errors = new ArrayList<String>();
    if ("".equals(mainPanel.machineID.getSelectedItem())) {
      errors.add("Machine name is required.");
    }
    if ("".equals(mainPanel.fullDomainName.getText())) {
      errors.add("Machine name with full domain name is required.");
    }
    if ("".equals(mainPanel.machineSettings.username.getText())) {
      errors.add("User Name Required");
    }
    if ("".equals(mainPanel.machineSettings.runDirectory.getText())) {
      errors.add("Run Directory Required");
    }

    if ("".equals(mainPanel.codeSettings.executable.getSelectedItem())) {
      errors.add("Executable Path Required");
    }
    if (mainPanel.machineSettings.allocationAccount.isVisible()
        && "".equals(mainPanel.machineSettings.allocationAccount.getText())) {
      errors.add("Allocation Account Required");
    }

    if (mainPanel.machineSettings.runDirectory.getText().contains("$")
        || !mainPanel.machineSettings.runDirectory.getText().startsWith("/")) {
      errors.add(
          "Run directory should be an absolute path without any environment variables such as $SCRATCH");
    }
    int processors = Integer.parseInt(mainPanel.machineSettings.processors.getValue()
        .toString());
    int procsPerTask = Integer.parseInt(mainPanel.machineSettings.procsPerTask
        .getValue().toString());
    if (procsPerTask>processors) {
      errors.add(
          "Processors per task should be less than or equal to total number of processors");
    }
    
    if(customCodeParametersSection != null){
      customCodeParametersSection.validateUserInputs(errors);
    }
    
    return errors;
  }




  /**************************************
   * Helper Functions: Change these to adjust layout.
   * 
   * @author port091
   * 
   */

  @SuppressWarnings("unused")
  private JTextField largeField() {
    return new javax.swing.JTextField(40);
  }

  private JTextField mediumField() {
    return new javax.swing.JTextField(16);
  }

  private JSpinner getSmallSpinner(Integer min, Integer max) {
    JSpinner spinner = new JSpinner();
    spinner.setPreferredSize(new Dimension(80, 22));
    if (min != null)
      ((SpinnerNumberModel) spinner.getModel()).setMinimum(min);
    if (max != null)
      ((SpinnerNumberModel) spinner.getModel()).setMaximum(max);
    return spinner;
  }

  public class MainPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    JLabel lblMachine;
    JComboBox machineID;
    JLabel fullDomainName;
    JCheckBox clientSideMonitoringCB;
    //JButton addButton;
    //JButton loadButton;
    //JButton manageButton;
    CodeSettingsPanel codeSettings;
    MachineSettingsPanel machineSettings;

    /**
     * Creates new form LaunchConfigPanel
     */
    public MainPanel() {
      initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
      lblMachine = new JLabel("Machine:");

      machineID = new JComboBox();
      machineID.addActionListener(new ActionListener() {
        
        @Override
        public void actionPerformed(ActionEvent e) {
          if(comboListenersEnabled) {
            // save the current config to the cache
            String newMachineId = (String)machineID.getSelectedItem();
            JobConfig config = bindJobConfig();
            cachedConfigs.put(currentMachineId, config);
            loadMachine(newMachineId);
          }
        }
      });
      fullDomainName = new JLabel("");     
      
      clientSideMonitoringCB = new JCheckBox();
      
//      File location = ClasspathUtils.getFileFromClassFolder(JobLauncherPanel.class, "icons/16x16");
//      addButton = Utilities.newButton(location, "add", "Add current configuration as a Named Job Config.");
//      addButton.setText("Add Named Job Config");
//      addButton.addActionListener(new ActionListener() {        
//        @Override
//        public void actionPerformed(ActionEvent e) {
//          addNamedConfig();
//        }
//      });
//      
//      loadButton = Utilities.newButton(location, "load","Populate the form based on a Named Job Config.");
//      loadButton.setText("Load Named Job Config");
//      
//      manageButton = Utilities.newButton(location, "load","Manage my Named Job Configs.");
//      manageButton.setText("Manage Named JobConfigs");   

      machineSettings = new MachineSettingsPanel();
      codeSettings = new CodeSettingsPanel(false);

      resetLayout();
    }
    
    private void addNamedConfig() {
      // prompt for the name of the config
      String name = JOptionPane.showInputDialog(JobLauncherPanel.this, "Enter the name for the saved Job Config profile:", "Save Job Config", JOptionPane.QUESTION_MESSAGE);
      
      if(name == null || name.isEmpty()) {
        return;
      }
      String machineId = (String)mainPanel.machineID.getSelectedItem();
      boolean doit = true;
      if(jobConfigService.getNamedConfig(name, machineId, codeID) != null) {
        int result = JOptionPane.showConfirmDialog(JobLauncherPanel.this, "A Job Config with this name already exists. Do you want to overwrite?", "Confirm Replace", JOptionPane.OK_CANCEL_OPTION);
        if(result != JOptionPane.OK_OPTION){
          doit = false;
        }
      }
      if(doit) {
        JobConfig namedConfig = bindJobConfig();
        namedConfig.setName(name);
        jobConfigService.addNamedConfig(namedConfig);
      }

    }

    public void resetLayout() {
      this.removeAll();
      machineSettings.resetLayout();

      setLayout(new java.awt.GridBagLayout());
      java.awt.GridBagConstraints gridBagConstraints;

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(14, 15, 0, 4);
      add(lblMachine, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;      
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(14, 4, 0, 4);
      add(machineID, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;      
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(14, 4, 0, 4);
      add(fullDomainName, gridBagConstraints);  
      
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 3;
      gridBagConstraints.gridy = 0;      
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(13, 40, 0, 4);
      add(clientSideMonitoringCB, gridBagConstraints); 
      
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(14, 0, 0, 0);
      add(new JLabel(""), gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.gridwidth = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(20, 15, 0, 15);
      add(machineSettings, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(20, 15, 0, 15);
      add(codeSettings, gridBagConstraints);

      // adjustable blank section for spacing
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridwidth = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(14, 4, 0, 4);
      add(new JLabel(""), gridBagConstraints);

    }// </editor-fold>

  }

  public class CodeSettingsPanel extends CollapsiblePanel {

    private static final long serialVersionUID = -7360835327668356077L;

    public JComboBox executable;
    public javax.swing.JTextField pollingInterval;

    private javax.swing.JLabel executablePathLabel;
    private javax.swing.JLabel pollingIntervalLabel;
    private javax.swing.JLabel pollingIntervalUnits;

    private boolean showPollingInterval;


    public CodeSettingsPanel(boolean showPollingInterval) {
      super(getCodeSectionTitle());
      this.showPollingInterval = showPollingInterval;
      initComponents();
    }

    private void initComponents() {

      executablePathLabel = new JLabel(getCodeExecutableLabel());

      pollingIntervalLabel = new JLabel("Polling interval");
      pollingIntervalLabel.setVisible(showPollingInterval);
      pollingIntervalUnits = new javax.swing.JLabel("seconds");
      pollingIntervalUnits.setVisible(showPollingInterval);

      executable = new JComboBox();
      executable.setEditable(true);

      pollingInterval = mediumField();
      pollingInterval.setVisible(showPollingInterval);

      pollingInterval.setText("60");

      resetLayout();
    }

    public void resetLayout() {
      removeAll();

      setLayout(new java.awt.GridBagLayout());
      java.awt.GridBagConstraints gridBagConstraints;

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(executablePathLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(executable, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 15);
      add(pollingIntervalLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 15);
      add(pollingInterval, gridBagConstraints);     

      if(customCodeParametersSection != null) {
        customCodeParametersSection.resetLayout(this, 2);
      }
    }

  }

  public class MachineSettingsPanel extends CollapsiblePanel {

    private static final long serialVersionUID = -4069351684911539082L;
    JTextField allocationAccount;
    // Nodes
    JLabel nodesLabel;
    JLabel nodesRange;
    JSpinner nodes;
    
    // Processors
    JLabel processorsLabel;
    JLabel procsRange;
    JSpinner processors;
    
    // Tasks per node
    JLabel lblTasksPerNode;
    JSpinner tasksPerNode;
    JLabel tasksPerNodeRange;

    // Processors per task
    JLabel processorsPerTaskLabel;
    JSpinner procsPerTask;
    JLabel procsPerTaskRange;
    
    JLabel queueLabel;
    JComboBox queueName;
    QueueInfoPanel queueInfoPanel;
    JSpinner wallTimeLimitHours;
    JSpinner wallTimeLimitMinutes;
    JSpinner wallTimeLimitSeconds;

    JLabel allocationAccountLabel;
    JLabel wallTimeLimitHoursLabel;
    JLabel wallTimeLimitMinutesLabel;
    JLabel wallTimeLimitSecondsLabel;
    JLabel wallTimeLimitLabel;

    JLabel lblUsername;
    JTextField username;
    JTextField runDirectory;
    JLabel lblRunDirectory;

    public MachineSettingsPanel() {
      super(getMachineSectionTitle());
      initComponents();
    }    

    private void initComponents() {

      lblUsername = new javax.swing.JLabel();
      lblRunDirectory = new javax.swing.JLabel();
      runDirectory = new javax.swing.JTextField();
      username = new javax.swing.JTextField();
      lblUsername.setText("Username:");
      lblRunDirectory.setText("Run Directory:");
      runDirectory.setText("");
      username.setText("");

      allocationAccountLabel = new JLabel("Allocation Account:");
      allocationAccount = new JTextField();

      queueLabel = new JLabel("Job Queue:");
      queueName = new javax.swing.JComboBox();
      queueName.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if(comboListenersEnabled) {
              // Update time limit and processors and node limit
              Machine machine = machineRegistry.get((String)mainPanel.machineID.getSelectedItem());
              Queue qobject = machine.getScheduler().getQueues().get(queueName
                  .getSelectedIndex());
              setQueueConstraints(machine, qobject);
            }
          }
        }
      });

      wallTimeLimitLabel = new JLabel("Wall Time:");

      wallTimeLimitHours = getSmallSpinner(0, null);
      wallTimeLimitHoursLabel = new javax.swing.JLabel("hours");

      wallTimeLimitMinutes = getSmallSpinner(0, 59);
      wallTimeLimitMinutesLabel = new javax.swing.JLabel("minutes");

      wallTimeLimitSeconds = getSmallSpinner(0, 59);
      wallTimeLimitSecondsLabel = new javax.swing.JLabel("seconds");

      processorsLabel = new JLabel("Cores:");
      processors = getSmallSpinner(null, null);
      processors.getModel().addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          // do not react if listeners are disabled
          if(comboListenersEnabled) {
            // update nodes to be consistent with processors
            Integer procs = Integer.parseInt(processors.getValue().toString());
            
            String machineID = (String)mainPanel.machineID.getSelectedItem();
            Machine machine = machineRegistry.get(machineID);
            int procsPerNode = machine.getProcsPerNode();

            int nodeCount = new BigDecimal(procs).divide(new BigDecimal(procsPerNode), RoundingMode.UP).intValue();
            
            // the user typed in a number of processors that isn't a multiple of the number of nodes
            // so we must correct (TODO: is this necessary?)
            if(procs % procsPerNode != 0) {
              procs = nodeCount * procsPerNode;
            }
            nodes.setValue(nodeCount);
            processors.setValue(procs);
          }
        }
      });

      procsRange = new javax.swing.JLabel("[]");
      processorsPerTaskLabel = new javax.swing.JLabel("Cores per Task:");
      procsPerTask = getSmallSpinner(null, null);
      procsPerTaskRange = new javax.swing.JLabel("[]");
      
      nodesLabel = new javax.swing.JLabel("Nodes:");
      nodes = getSmallSpinner(null, null);
      nodes.getModel().addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          // do not react if listeners are disabled
          if(comboListenersEnabled) {
            // update processors to be consistent with nodes
            Integer nodeCount = Integer.parseInt(nodes.getValue().toString());
            String machineID = (String)mainPanel.machineID.getSelectedItem();
            Machine machine = machineRegistry.get(machineID);
            int procsPerNode = machine.getProcsPerNode();
            processors.setValue(nodeCount * procsPerNode);
          }
        }
      });
      nodesRange = new javax.swing.JLabel("[]");
      
      lblTasksPerNode = new javax.swing.JLabel("Tasks per Node:");
      tasksPerNode = getSmallSpinner(null, null);
      tasksPerNodeRange = new javax.swing.JLabel("[]");

    }
    
    public void resetLayout() {
      removeAll();

      setLayout(new java.awt.GridBagLayout());
      java.awt.GridBagConstraints gridBagConstraints;

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(lblRunDirectory, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.gridwidth = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(runDirectory, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(lblUsername, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 0.4;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(username, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
      gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
      add(allocationAccountLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 3;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 0.40;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(allocationAccount, gridBagConstraints);
      
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
      gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
      add(queueLabel, gridBagConstraints);

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 5;
      gridBagConstraints.gridy = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
      gridBagConstraints.weightx = 0.20;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(queueName, gridBagConstraints); 

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(wallTimeLimitLabel, gridBagConstraints);

      JPanel wallTimePanel = getWallTimeSection();
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(wallTimePanel, gridBagConstraints); 
      
      // queue info
      queueInfoPanel = new QueueInfoPanel();
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 2;
      gridBagConstraints.gridwidth = 2;
      gridBagConstraints.gridheight = 5;
      gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.weighty = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 5);
      add(queueInfoPanel, gridBagConstraints); 
      
      
      // Nodes
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(nodesLabel, gridBagConstraints); 
      
      JPanel nodesPanel = getSpinnerSection(nodes, nodesRange);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 3;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);     
      add(nodesPanel, gridBagConstraints);

      // Processors
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(processorsLabel, gridBagConstraints);  
      
      JPanel procsPanel = getSpinnerSection(processors, procsRange);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 4;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);     
      add(procsPanel, gridBagConstraints);
      
      // Tasks per node
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 5;
      gridBagConstraints.gridwidth = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(lblTasksPerNode, gridBagConstraints);  
      
      JPanel tasksPerNodePanel = getSpinnerSection(tasksPerNode, new JLabel(""));
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 5;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);     
      add(tasksPerNodePanel, gridBagConstraints);   

      // Procs per task
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 6;
      gridBagConstraints.gridwidth = 1;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      add(processorsPerTaskLabel, gridBagConstraints);  
      
      JPanel procsPerTaskPanel = getSpinnerSection(procsPerTask, procsPerTaskRange);
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 6;
      gridBagConstraints.gridwidth = 3;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);     
      add(procsPerTaskPanel, gridBagConstraints);

    }
    
    private JPanel getSpinnerSection(JComponent spinner, JLabel rangeInfo) {
      JPanel panel = new JPanel(new GridBagLayout());
      java.awt.GridBagConstraints gridBagConstraints;
      panel.setBackground(this.getBackground());
      
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(spinner, gridBagConstraints);     

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.weightx = 1.0;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(rangeInfo, gridBagConstraints);   
      
      return panel;
    }
    
    private JPanel getWallTimeSection() {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBackground(this.getBackground());
      java.awt.GridBagConstraints gridBagConstraints;
      
      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 0;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(wallTimeLimitHours, gridBagConstraints);     

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 1;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(wallTimeLimitHoursLabel, gridBagConstraints);     

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 2;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(wallTimeLimitMinutes, gridBagConstraints);     

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 3;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
      gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
      panel.add(wallTimeLimitMinutesLabel, gridBagConstraints);     

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 4;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
      gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
      panel.add(wallTimeLimitSeconds, gridBagConstraints);        

      gridBagConstraints = new java.awt.GridBagConstraints();
      gridBagConstraints.gridx = 5;
      gridBagConstraints.gridy = 0;
      gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
      gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 5);
      panel.add(wallTimeLimitSecondsLabel, gridBagConstraints);     


      return panel;
    }

  }
  
  public class QueueInfoPanel extends JPanel {
    JLabel lblNodesinfo;
    JLabel lblProcsPerNodeInfo;
    JLabel lblProcessorsinfo;
    JLabel lblWalltimeinfo;
    
    /**
     * Create the panel.
     */
    public QueueInfoPanel() {
      setBackground(UIManager.getColor("info"));
      setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
      GridBagLayout gridBagLayout = new GridBagLayout();
      gridBagLayout.columnWidths = new int[]{97, 120, 0};
      gridBagLayout.rowHeights = new int[]{30, 30, 30, 30, 30};
      gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
      gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
      setLayout(gridBagLayout);
      
      JLabel lblNodes = new JLabel("Nodes:");
      lblNodes.setForeground(new Color(0, 102, 255));
      lblNodes.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
      GridBagConstraints gbc_lblNodes = new GridBagConstraints();
      gbc_lblNodes.anchor = GridBagConstraints.WEST;
      gbc_lblNodes.insets = new Insets(0, 4, 0, 5);
      gbc_lblNodes.gridx = 0;
      gbc_lblNodes.gridy = 0;
      add(lblNodes, gbc_lblNodes);
      
      lblNodesinfo = new JLabel("");
      lblNodesinfo.setForeground(SystemColor.inactiveCaptionText);
      lblNodesinfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
      GridBagConstraints gbc_lblNodesinfo = new GridBagConstraints();
      gbc_lblNodesinfo.anchor = GridBagConstraints.WEST;
      gbc_lblNodesinfo.insets = new Insets(0, 0, 0, 0);
      gbc_lblNodesinfo.gridx = 1;
      gbc_lblNodesinfo.gridy = 0;
      add(lblNodesinfo, gbc_lblNodesinfo);
      
      JLabel lblProcessors = new JLabel("Cores:");
      lblProcessors.setForeground(new Color(0, 102, 255));
      lblProcessors.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
      GridBagConstraints gbc_lblProcessors = new GridBagConstraints();
      gbc_lblProcessors.anchor = GridBagConstraints.WEST;
      gbc_lblProcessors.insets = new Insets(0, 4, 0, 5);
      gbc_lblProcessors.gridx = 0;
      gbc_lblProcessors.gridy = 1;
      add(lblProcessors, gbc_lblProcessors);
      
      lblProcessorsinfo = new JLabel("");
      lblProcessorsinfo.setForeground(SystemColor.inactiveCaptionText);
      lblProcessorsinfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
      GridBagConstraints gbc_lblProcessorsinfo = new GridBagConstraints();
      gbc_lblProcessorsinfo.insets = new Insets(0, 0, 0, 0);
      gbc_lblProcessorsinfo.anchor = GridBagConstraints.WEST;
      gbc_lblProcessorsinfo.gridx = 1;
      gbc_lblProcessorsinfo.gridy = 1;
      add(lblProcessorsinfo, gbc_lblProcessorsinfo);
      
      JLabel lblProcsPerNode = new JLabel("Cores per Node:");
      lblProcsPerNode.setForeground(new Color(0, 102, 255));
      lblProcsPerNode.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
      GridBagConstraints gbc_lblProcsPerNode = new GridBagConstraints();
      gbc_lblProcsPerNode.anchor = GridBagConstraints.WEST;
      gbc_lblProcsPerNode.insets = new Insets(0, 4, 0, 5);
      gbc_lblProcsPerNode.gridx = 0;
      gbc_lblProcsPerNode.gridy = 2;
      add(lblProcsPerNode, gbc_lblProcsPerNode);
      
      lblProcsPerNodeInfo = new JLabel("");
      lblProcsPerNodeInfo.setForeground(SystemColor.inactiveCaptionText);
      lblProcsPerNodeInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
      GridBagConstraints gbc_lblProcsPerNodeInfo = new GridBagConstraints();
      gbc_lblProcsPerNodeInfo.anchor = GridBagConstraints.WEST;
      gbc_lblProcsPerNodeInfo.insets = new Insets(0, 0, 0, 0);
      gbc_lblProcsPerNodeInfo.gridx = 1;
      gbc_lblProcsPerNodeInfo.gridy = 2;
      add(lblProcsPerNodeInfo, gbc_lblProcsPerNodeInfo);

      
      JLabel lblWalltime = new JLabel("Max Walltime:");
      lblWalltime.setForeground(new Color(0, 102, 255));
      lblWalltime.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
      GridBagConstraints gbc_lblWalltime = new GridBagConstraints();
      gbc_lblWalltime.anchor = GridBagConstraints.WEST;
      gbc_lblWalltime.insets = new Insets(0, 4, 0, 5);
      gbc_lblWalltime.gridx = 0;
      gbc_lblWalltime.gridy = 3;
      add(lblWalltime, gbc_lblWalltime);
      
      lblWalltimeinfo = new JLabel("");
      lblWalltimeinfo.setForeground(SystemColor.inactiveCaptionText);
      lblProcsPerNodeInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
      GridBagConstraints gbc_lblWalltimeinfo = new GridBagConstraints();
      gbc_lblWalltimeinfo.anchor = GridBagConstraints.WEST;
      gbc_lblWalltimeinfo.insets = new Insets(0, 0, 0, 0);
      gbc_lblWalltimeinfo.gridx = 1;
      gbc_lblWalltimeinfo.gridy = 3;
      add(lblWalltimeinfo, gbc_lblWalltimeinfo);

    }

  }
}
