import {Card, CardActions, CardContent, TextField, Typography} from "@mui/material";
import Button from "@mui/material/Button";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";
import {useEffect, useState} from "react";

export default function ConfigTimeTab({
  dayOffsetHour, setDayOffsetHour, workingDayStartHour, setWorkingDayStartHour,
  workingDayEndHour, setWorkingDayEndHour, updateTimeConfig
}) {
  const {forceRender} = useAppContext();

  const [dayOffsetHourError, setDayOffsetHourError] = useState("");
  const [workingDayStartHourError, setWorkingDayStartHourError] = useState("");
  const [workingDayEndHourError, setWorkingDayEndHourError] = useState("");

  const getIsInteger = (value) => /^\d+$/.test(value);

  const validateDayOffset = (newDayOffsetHour) => {
    const isInteger = getIsInteger(newDayOffsetHour);
    if (!isInteger || newDayOffsetHour < 0 || newDayOffsetHour > 12 || newDayOffsetHour > workingDayEndHour) {
      setDayOffsetHourError("Day offset hour must be in 0 - 12 range");
    } else {
      setDayOffsetHourError("");
    }
  };

  const validateStartHourOfWorkingDay = (newWorkingDayStartHour) => {
    const isInteger = getIsInteger(newWorkingDayStartHour);
    if (!isInteger || newWorkingDayStartHour < 0 || newWorkingDayStartHour > 12) {
      setWorkingDayStartHourError("Start hour of working day must be in 0 - 12 range");
    } else {
      setWorkingDayStartHourError("");
    }
  };

  const validateEndHourOfWorkingDay = (newWorkingDayEndHour) => {
    const isInteger = getIsInteger(newWorkingDayEndHour);
    if (!isInteger || newWorkingDayEndHour < 12 || newWorkingDayEndHour > 23 || newWorkingDayEndHour < workingDayStartHour) {
      setWorkingDayEndHourError("End hour of working day must be in 12 - 23 range");
    } else {
      setWorkingDayEndHourError("");
    }
  };

  useEffect(() => {
    validateDayOffset(dayOffsetHour)
  }, [dayOffsetHour])

  useEffect(() => {
    validateStartHourOfWorkingDay(workingDayStartHour)
  }, [workingDayStartHour])

  useEffect(() => {
    validateEndHourOfWorkingDay(workingDayEndHour)
  }, [workingDayEndHour])

  return (
    <Card className="mb-4">
      <CardContent>
        <Typography variant="h6">Time Configuration</Typography>
        <TextField
          error={!!dayOffsetHourError}
          helperText={dayOffsetHourError}
          label="Day Offset Hour (0-12)"
          type="number"
          value={dayOffsetHour}
          onChange={(e) => setDayOffsetHour(e.target.value)}
          fullWidth
          className="my-2"
        />
        <TextField
          error={!!workingDayStartHourError}
          helperText={workingDayStartHourError}
          label="Working Day Start Hour (0-12)"
          type="number"
          value={workingDayStartHour}
          onChange={(e) => setWorkingDayStartHour(e.target.value)}
          fullWidth
          className="my-2"
        />
        <TextField
          error={!!workingDayEndHourError}
          helperText={workingDayEndHourError}
          label="Working Day End Hour (12-23)"
          type="number"
          value={workingDayEndHour}
          onChange={(e) => setWorkingDayEndHour(e.target.value)}
          fullWidth
          className="mt-2"
        />
      </CardContent>
      <CardActions className="ml-2">
        <Button
          disabled={!!dayOffsetHourError || !!workingDayStartHourError || !!workingDayEndHourError}
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