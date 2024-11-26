import {Button, Card, CardActions, CardContent, FormControlLabel, Switch, Typography} from "@mui/material";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";

export default function ConfigJiraTab({isJiraEnabled, setIsJiraEnabled, updateJiraConfig}) {
  const {forceRender} = useAppContext();

  return (
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
                onClick={async () => {
                  await updateJiraConfig({isJiraEnabled});
                  await initializeConfig();
                  forceRender();
                }}
        >Save</Button>
      </CardActions>
    </Card>
  )
}