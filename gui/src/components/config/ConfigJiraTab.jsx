import {
  Button,
  Card,
  CardActions,
  CardContent,
  Dialog, DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Switch, TextField,
  Typography
} from "@mui/material";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";
import {useEffect, useState} from "react";
import ConfirmationModal from "../general/ConfirmationModal.jsx";

export default function ConfigJiraTab({isJiraEnabled, onSave, onDelete, setIsJiraEnabled, updateJiraConfig, jiraInstance}) {
  const {forceRender} = useAppContext();

  const [isConfigDialogOpen, setIsConfigDialogOpen] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [email, setEmail] = useState("");
  const [baseUrl, setBaseUrl] = useState("");
  const [token, setToken] = useState("");

  useEffect(() => {
    setEmail(jiraInstance.email || "");
    setBaseUrl(jiraInstance.baseUrl || "");
    setToken(jiraInstance.token || "");
  }, [jiraInstance]);

  return (
    <>
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
        <div className="flex flex-col">
          <div className="mb-4">
            <Button variant="contained" color="primary"
                    onClick={async () => {
                      await updateJiraConfig({isJiraEnabled});
                      await initializeConfig();
                      forceRender();
                    }}
            >Save</Button>
          </div>
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                setIsConfigDialogOpen(true);
              }}
            >
              {jiraInstance.id ? "Edit" : "Add"} Jira Instance
            </Button>

            {jiraInstance.id && <Button
              variant="outlined"
              color="error"
              className="ml-2"
              onClick={() => setShowDeleteModal(true)}
            >
              Delete Jira Instance
            </Button>
            }
          </div>
        </div>
      </CardActions>
    </Card>
      <Dialog
        closeAfterTransition={false}
        open={isConfigDialogOpen}
        onClose={() => setIsConfigDialogOpen(false)}
      >
        <DialogTitle>
          Jira Instance
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            margin="dense"
            label="Jira Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <TextField
            fullWidth
            margin="dense"
            label="Base URL"
            value={baseUrl}
            onChange={(e) => setBaseUrl(e.target.value)}
          />
          <TextField
            fullWidth
            margin="dense"
            label="API Token"
            type="password"
            value={token}
            onChange={(e) => setToken(e.target.value)}

          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIsConfigDialogOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={() => {
              onSave({id: jiraInstance.id, email, baseUrl, token});
              setIsConfigDialogOpen(false);
            }}
            color="primary"
            variant="contained"
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
      <ConfirmationModal
        open={showDeleteModal}
        type="error"
        actionText="Delete"
        onConfirm={() => onDelete(jiraInstance.id)}
        onClose={() => {
          setShowDeleteModal(false);
        }}
      >
        Are you sure you want to delete jira instance?
      </ConfirmationModal>
    </>
  )
}