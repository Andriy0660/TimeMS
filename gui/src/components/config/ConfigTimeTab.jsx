import {Card, CardActions, CardContent, TextField, Typography} from "@mui/material";
import Button from "@mui/material/Button";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";
import {useState} from "react";

export default function ConfigTimeTab({
  dayOffsetHour, setDayOffsetHour, workingDayStartHour, setWorkingDayStartHour,
  workingDayEndHour, setWorkingDayEndHour, updateTimeConfig
}) {
  const {forceRender} = useAppContext();

  const [dayOffsetHourError, setDayOffsetHourError] = useState(false);
  const [workingDayStartHourError, setWorkingDayStartHourError] = useState(false);
  const [workingDayEndHourError, setWorkingDayEndHourError] = useState(false);

  return (
    <Card className="mb-4">
      <CardContent>
        <Typography variant="h6">Time Configuration</Typography>
        <TextField
          error={dayOffsetHourError}
          label="Day Offset Hour (0-12)"
          type="number"
          value={dayOffsetHour}
          onChange={(e) => {
            const newDayOffsetHour = e.target.value;
            const isInteger = /^\d+$/.test(newDayOffsetHour);
            if (!isInteger || newDayOffsetHour < 0 || newDayOffsetHour > 12 || newDayOffsetHour > workingDayEndHour) {
              setDayOffsetHourError(true);
            } else {
              setDayOffsetHourError(false);
            }
            setDayOffsetHour(newDayOffsetHour);
          }}
          fullWidth
          className="my-2"
        />
        <TextField
          error={workingDayStartHourError}
          label="Working Day Start Hour (0-12)"
          type="number"
          value={workingDayStartHour}
          onChange={(e) => {
            const newWorkingDayStartHour = e.target.value;
            const isInteger = /^\d+$/.test(newWorkingDayStartHour);
            if (!isInteger || newWorkingDayStartHour < 0 || newWorkingDayStartHour > 12) {
              setWorkingDayStartHourError(true);
            } else {
              setWorkingDayStartHourError(false);
            }
            setWorkingDayStartHour(newWorkingDayStartHour);
          }}
          fullWidth
          className="my-2"
        />
        <TextField
          error={workingDayEndHourError}
          label="Working Day End Hour (12-23)"
          type="number"
          value={workingDayEndHour}
          onChange={(e) => {
            const newWorkingDayEndHour = e.target.value;
            const isInteger = /^\d+$/.test(newWorkingDayEndHour);
            if (!isInteger || newWorkingDayEndHour < 12 || newWorkingDayEndHour > 23 || newWorkingDayEndHour < workingDayStartHour) {
              setWorkingDayEndHourError(true);
            } else {
              setWorkingDayEndHourError(false);
            }
            setWorkingDayEndHour(newWorkingDayEndHour);
          }}
          fullWidth
          className="mt-2"
        />
      </CardContent>
      <CardActions className="ml-2">
        <Button
          disabled={dayOffsetHourError || workingDayStartHourError || workingDayEndHourError}
          variant="contained" color="primary"
          onClick={async () => {
            await updateTimeConfig({dayOffsetHour, workingDayStartHour, workingDayEndHour});
            await initializeConfig();
            forceRender();
          }}
        >Save</Button>
      </CardActions>
    </Card>
  )
}