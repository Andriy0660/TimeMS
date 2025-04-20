import {Button, Card, CardActions, CardContent, FormControlLabel, Switch, TextField, Typography} from "@mui/material";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";
import {useState} from "react";

export default function ConfigExternalServiceTab({isExternalServiceEnabled, setIsExternalServiceEnabled,
  isExternalServiceIncludeDescription, setIsExternalServiceIncludeDescription, externalServiceTimeCf,
  setExternalServiceTimeCf, updateExternalServiceConfig}) {

  const {forceRender} = useAppContext();
  const [externalServiceTimeCfError, setExternalServiceTimeCfError] = useState(false);

  return (
    <Card className="mb-4">
      <CardContent>
        <Typography variant="h6">External Service Integration</Typography>
        <FormControlLabel
          className="mt-2"
          control={<Switch checked={isExternalServiceEnabled} onChange={() => setIsExternalServiceEnabled(!isExternalServiceEnabled)} />}
          label="Enable External Service Synchronization"
        />

        {/*<FormControlLabel*/}
        {/*  className="mt-2"*/}
        {/*  control={<Switch checked={isExternalServiceIncludeDescription}*/}
        {/*                   onChange={() => setIsExternalServiceIncludeDescription(!isExternalServiceIncludeDescription)} />}*/}
        {/*  label="Include description"*/}
        {/*/>*/}

        {/*<TextField*/}
        {/*  error={externalServiceTimeCfError}*/}
        {/*  label="External Service Time Configuration Factor"*/}
        {/*  value={externalServiceTimeCf}*/}
        {/*  onChange={(e) => {*/}
        {/*    const newExternalServiceTimeCf = e.target.value;*/}
        {/*    const isValidNumber = /^\d+(\.\d+)?$/.test(newExternalServiceTimeCf);*/}
        {/*    if (!isValidNumber || newExternalServiceTimeCf <= 0) {*/}
        {/*      setExternalServiceTimeCfError(true);*/}
        {/*    } else {*/}
        {/*      setExternalServiceTimeCfError(false);*/}
        {/*    }*/}
        {/*    setExternalServiceTimeCf(newExternalServiceTimeCf);*/}
        {/*  }}*/}
        {/*  fullWidth*/}
        {/*  className="mt-4"*/}
        {/*/>*/}
      </CardContent>
      <CardActions className="ml-2">
        <Button
          disabled={externalServiceTimeCfError}
          variant="contained" color="primary"
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