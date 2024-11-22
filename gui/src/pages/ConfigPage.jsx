import {useEffect, useState} from "react";
import {CircularProgress, LinearProgress, Tab, Tabs, Typography,} from "@mui/material";
import {useQuery} from "@tanstack/react-query";
import configApi from "../api/configApi.js";
import useAppContext from "../context/useAppContext.js";
import useConfigMutations from "../hooks/useConfigMutations.js";
import useAsyncCall from "../hooks/useAsyncCall.js";
import ConfigTimeTab from "../components/config/ConfigTimeTab.jsx";
import ConfigJiraTab from "../components/config/ConfigJiraTab.jsx";
import ConfigExternalServiceTab from "../components/config/ConfigExternalServiceTab.jsx";

export default function ConfigPage() {
  const {addAlert} = useAppContext();

  const {onTimeConfigUpdate, onJiraConfigUpdate, onExternalServiceConfigUpdate} = useConfigMutations();

  const {execute: updateTimeConfig, isExecuting: isTimeConfigUpdating} = useAsyncCall({
    fn: onTimeConfigUpdate,
  })

  const {execute: updateJiraConfig, isExecuting: isJiraConfigUpdating} = useAsyncCall({
    fn: onJiraConfigUpdate,
  })

  const {execute: updateExternalServiceConfig, isExecuting: isExternalServiceConfigUpdating} = useAsyncCall({
    fn: onExternalServiceConfigUpdate,
  })

  const {
    data: config,
    isPending: isConfigFetching,
    error: configFetchingError
  } = useQuery({
    queryKey: [configApi.key],
    queryFn: () => configApi.getConfig(),
    retryDelay: 300,
  });

  useEffect(() => {
    if (configFetchingError) {
      addAlert({
        text: `${configFetchingError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [configFetchingError]);

  const [tabIndex, setTabIndex] = useState(0);

  const [dayOffsetHour, setDayOffsetHour] = useState(0);
  const [workingDayStartHour, setWorkingDayStartHour] = useState(9);
  const [workingDayEndHour, setWorkingDayEndHour] = useState(18);

  const [isJiraEnabled, setIsJiraEnabled] = useState(false);

  const [isExternalServiceEnabled, setIsExternalServiceEnabled] = useState(false);
  const [externalServiceTimeCf, setExternalServiceTimeCf] = useState(1);
  const [isExternalServiceIncludeDescription, setIsExternalServiceIncludeDescription] = useState(true);

  useEffect(() => {
    if (!config) return;
    setDayOffsetHour(config.dayOffsetHour);
    setWorkingDayStartHour(config.workingDayStartHour);
    setWorkingDayEndHour(config.workingDayEndHour);
    setIsJiraEnabled(config.isJiraEnabled);
    setIsExternalServiceEnabled(config.isExternalServiceEnabled);
    setExternalServiceTimeCf(config.externalServiceTimeCf);
    setIsExternalServiceIncludeDescription(config.isExternalServiceIncludeDescription)
  }, [config])

  if (isConfigFetching) {
    return <div className="text-center">
      <CircularProgress />
    </div>
  }


  return (
    <div className="mx-auto p-4 w-3/4">
      <Typography variant="h4" className="mb-4 text-center font-bold text-blue-500">Configuration</Typography>
      {isTimeConfigUpdating || isJiraConfigUpdating || isExternalServiceConfigUpdating && <LinearProgress />}
      <Tabs
        value={tabIndex}
        onChange={(event, newValue) => setTabIndex(newValue)}
        variant="fullWidth"
        className="mb-4"
      >
        <Tab label="Time Configuration" />
        <Tab label="Jira" />
        <Tab label="External Service" />
      </Tabs>

      {tabIndex === 0 && (
        <ConfigTimeTab dayOffsetHour={dayOffsetHour} setDayOffsetHour={setDayOffsetHour}
                       workingDayStartHour={workingDayStartHour} setWorkingDayStartHour={setWorkingDayStartHour}
                       workingDayEndHour={workingDayEndHour} setWorkingDayEndHour={setWorkingDayEndHour}
                       updateTimeConfig={updateTimeConfig} />
      )}

      {tabIndex === 1 && (
        <ConfigJiraTab isJiraEnabled={isJiraEnabled} setIsJiraEnabled={setIsJiraEnabled} updateJiraConfig={updateJiraConfig} />
      )}

      {tabIndex === 2 && (
        <ConfigExternalServiceTab isExternalServiceEnabled={isExternalServiceEnabled} setIsExternalServiceEnabled={setIsExternalServiceEnabled}
                                  isExternalServiceIncludeDescription={isExternalServiceIncludeDescription}
                                  setIsExternalServiceIncludeDescription={setIsExternalServiceIncludeDescription}
                                  externalServiceTimeCf={externalServiceTimeCf} setExternalServiceTimeCf={setExternalServiceTimeCf}
                                  updateExternalServiceConfig={updateExternalServiceConfig}
        />
      )}
    </div>
  );
}