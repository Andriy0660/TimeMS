import {useEffect, useState} from "react";
import {
  Button,
  Card,
  CardActions,
  CardContent,
  CircularProgress,
  FormControlLabel, LinearProgress,
  Switch,
  Tab,
  Tabs,
  TextField,
  Typography,
} from "@mui/material";
import {useQuery} from "@tanstack/react-query";
import configApi from "../api/configApi.js";
import useAppContext from "../context/useAppContext.js";
import useConfigMutations from "../hooks/useConfigMutations.js";
import useAsyncCall from "../hooks/useAsyncCall.js";

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
  const [externalServiceTimeCF, setExternalServiceTimeCF] = useState(1);
  const [isExternalServiceIncludeDescription, setIsExternalServiceIncludeDescription] = useState(true);

  useEffect(() => {
    if (!config) return;
    setDayOffsetHour(config.dayOffsetHour);
    setWorkingDayStartHour(config.workingDayStartHour);
    setWorkingDayEndHour(config.workingDayEndHour);
    setIsJiraEnabled(config.isJiraEnabled);
    setIsExternalServiceEnabled(config.isExternalServiceEnabled);
    setExternalServiceTimeCF(config.externalServiceTimeCf);
  }, [config])

  if (isConfigFetching) {
    return <div className="text-center">
      <CircularProgress />
    </div>
  }

  return (
    <div className="mx-auto p-4 w-3/4">
      <Typography variant="h4" className="mb-4 text-center font-bold text-blue-500">Configuration</Typography>
      {isTimeConfigUpdating || isJiraConfigUpdating || isExternalServiceConfigUpdating && <LinearProgress /> }
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
        <Card className="mb-4">
          <CardContent>
            <Typography variant="h6">Time Configuration</Typography>
            <TextField
              label="Day Offset Hour (0-12)"
              type="number"
              value={dayOffsetHour}
              onChange={(e) => {
                const value = Math.max(0, Math.min(12, Number(e.target.value)));
                setDayOffsetHour(value);
              }}
              fullWidth
              className="my-2"
            />
            <TextField
              label="Working Day Start Hour (0-23)"
              type="number"
              value={workingDayStartHour}
              onChange={(e) => {
                const value = Math.max(0, Math.min(23, Number(e.target.value)));
                setWorkingDayStartHour(value);
              }}
              fullWidth
              className="my-2"
            />
            <TextField
              label="Working Day End Hour (0-23)"
              type="number"
              value={workingDayEndHour}
              onChange={(e) => {
                const value = Math.max(0, Math.min(23, Number(e.target.value)));
                setWorkingDayEndHour(value);
              }}
              fullWidth
              className="mt-2"
            />
          </CardContent>
          <CardActions className="ml-2">
            <Button variant="contained" color="primary"
              onClick={() => updateTimeConfig({dayOffsetHour, workingDayStartHour, workingDayEndHour})}
            >Save</Button>
          </CardActions>
        </Card>
      )}

      {tabIndex === 1 && (
        <Card className="mb-4">
          <CardContent>
            <Typography variant="h6">Jira Integration</Typography>
            <FormControlLabel
              className="mt-2"
              control={<Switch checked={isJiraEnabled} onChange={() => {
                setIsJiraEnabled(!isJiraEnabled);
              }} />}
              label="Enable Jira Synchronization"
            />
          </CardContent>
          <CardActions className="ml-2">
            <Button variant="contained" color="primary"
              onClick={() => updateJiraConfig({isJiraEnabled})}
            >Save</Button>
          </CardActions>
        </Card>
      )}

      {tabIndex === 2 && (
        <Card className="mb-4">
          <CardContent>
            <Typography variant="h6">External Service Integration</Typography>
            <FormControlLabel
              className="mt-2"
              control={<Switch checked={isExternalServiceEnabled} onChange={() => setIsExternalServiceEnabled(!isExternalServiceEnabled)} />}
              label="Enable External Service Synchronization"
            />

            <FormControlLabel
              className="mt-2"
              control={<Switch checked={isExternalServiceIncludeDescription} onChange={() => setIsExternalServiceIncludeDescription(!isExternalServiceIncludeDescription)} />}
              label="Include description"
            />

            <TextField
              label="External Service Time Configuration Factor"
              type="number"
              value={externalServiceTimeCF}
              onChange={(e) => setExternalServiceTimeCF(Number(e.target.value))}
              fullWidth
              className="mt-4"
            />
          </CardContent>
          <CardActions className="ml-2">
            <Button variant="contained" color="primary"
              onClick={() => updateExternalServiceConfig({isExternalServiceEnabled, externalServiceTimeCF, isExternalServiceIncludeDescription})}
            >Save</Button>
          </CardActions>
        </Card>
      )}
    </div>
  );
}