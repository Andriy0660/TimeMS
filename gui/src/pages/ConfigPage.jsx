import {useState} from "react";
import {
  TextField,
  Button,
  Switch,
  Typography,
  Card,
  CardContent,
  FormControlLabel,
  Tabs,
  Tab, CardActions,
} from "@mui/material";

export default function ConfigPage(){
  const [tabIndex, setTabIndex] = useState(0);

  const [dayOffsetHour, setDayOffsetHour] = useState(0);
  const [workingDayStartHour, setWorkingDayStartHour] = useState(9);
  const [workingDayEndHour, setWorkingDayEndHour] = useState(18);

  const [jiraEnabled, setJiraEnabled] = useState(false);

  const [externalServiceEnabled, setExternalServiceEnabled] = useState(false);
  const [externalServiceTimeCF, setExternalServiceTimeCF] = useState(1);

  return (
    <div className="mx-auto p-4 w-3/4">
      <Typography variant="h4" className="mb-4 text-center font-bold text-blue-500">Configuration</Typography>
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
            <Typography variant="h6" >Time Configuration</Typography>
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
            <Button variant="contained" color="primary">Save</Button>
          </CardActions>
        </Card>
      )}

      {tabIndex === 1 && (
        <Card className="mb-4">
          <CardContent>
            <Typography variant="h6">Jira Integration</Typography>
            <FormControlLabel
              className="mt-2"
              control={<Switch checked={jiraEnabled} onChange={() => {
                setJiraEnabled(!jiraEnabled);
              }} />}
              label="Enable Jira Synchronization"
            />

          </CardContent>
        </Card>
      )}

      {tabIndex === 2 && (
        <Card className="mb-4">
          <CardContent>
            <Typography variant="h6">External Service Integration</Typography>
            <FormControlLabel
              className="mt-2"
              control={<Switch checked={externalServiceEnabled} onChange={() => setExternalServiceEnabled(!externalServiceEnabled)} />}
              label="Enable External Service Synchronization"
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
            <Button variant="contained" color="primary">Save</Button>
          </CardActions>
        </Card>
      )}
    </div>
  );
}