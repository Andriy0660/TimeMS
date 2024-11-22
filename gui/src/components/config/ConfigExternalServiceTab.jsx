import {Button, Card, CardActions, CardContent, FormControlLabel, Switch, TextField, Typography} from "@mui/material";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";

export default function ConfigExternalServiceTab({isExternalServiceEnabled, setIsExternalServiceEnabled,
  isExternalServiceIncludeDescription, setIsExternalServiceIncludeDescription, externalServiceTimeCf,
  setExternalServiceTimeCf, updateExternalServiceConfig}) {

  const {forceRender} = useAppContext();

  return (
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
          control={<Switch checked={isExternalServiceIncludeDescription}
                           onChange={() => setIsExternalServiceIncludeDescription(!isExternalServiceIncludeDescription)} />}
          label="Include description"
        />

        <TextField
          label="External Service Time Configuration Factor"
          type="number"
          value={externalServiceTimeCf}
          onChange={(e) => setExternalServiceTimeCf(Number(e.target.value))}
          fullWidth
          className="mt-4"
        />
      </CardContent>
      <CardActions className="ml-2">
        <Button variant="contained" color="primary"
                onClick={async () => {
                  await updateExternalServiceConfig({isExternalServiceEnabled, externalServiceTimeCf, isExternalServiceIncludeDescription});
                  await initializeConfig();
                  forceRender();
                }}
        >Save</Button>
      </CardActions>
    </Card>
  )
}